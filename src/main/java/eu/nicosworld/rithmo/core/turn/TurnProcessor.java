package eu.nicosworld.rithmo.core.turn;

import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.exception.logical.NoActionException;
import eu.nicosworld.rithmo.core.game.victory.VictoryConditionEvaluator;
import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.core.turn.applier.ActionApplier;
import eu.nicosworld.rithmo.core.turn.applier.AppliedResult;
import eu.nicosworld.rithmo.core.turn.option.TurnOption;
import eu.nicosworld.rithmo.core.turn.resolver.PhaseResolver;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;
import eu.nicosworld.rithmo.engine.model.victory.Victory;
import eu.nicosworld.rithmo.engine.victory.VictoryEngine;
import java.util.List;

/**
 * Orchestrates the game loop logic by transitioning between turn phases.
 *
 * <p>The {@code TurnProcessor} handles the state machine of a turn, moving through computation
 * phases (automatic) and application phases (requiring player input).
 */
public class TurnProcessor {

  private final ActionApplier actionApplier;
  private final PhaseResolver phaseResolver;
  private final VictoryEngine victoryEngine;
  private final VictoryConditionEvaluator evaluator;

  public TurnProcessor(
      ActionApplier actionApplier,
      PhaseResolver phaseResolver,
      VictoryEngine victoryEngine,
      VictoryConditionEvaluator evaluator) {
    this.actionApplier = actionApplier;
    this.phaseResolver = phaseResolver;
    this.victoryEngine = victoryEngine;
    this.evaluator = evaluator;
  }

  /** Overload for automatic phase transitions where no player action is required. */
  public TurnState process(TurnState turnState) throws VictoryException, PatException {
    return process(turnState, new NoAction());
  }

  /**
   * Processes a phase transition. If the phase is an "APPLICATION" phase, a valid {@link
   * TurnAction} must be provided.
   *
   * @param turnState The current state of the turn.
   * @param action The action to apply (should be {@link NoAction} for computation phases).
   * @return The resulting {@link TurnState}.
   * @throws VictoryException If victory conditions are met.
   * @throws PatException If a stalemate is detected.
   * @throws NoActionException If an application phase is reached without a valid player action.
   */
  public TurnState process(TurnState turnState, TurnAction action)
      throws VictoryException, PatException {
    TurnPhase actualPhase = turnState.phase();

    // Technical guard: ensure we don't try to apply "nothing" in a player-action phase.
    if (isApplicationPhase(actualPhase) && action instanceof NoAction) {
      throw new NoActionException(actualPhase);
    }

    switch (actualPhase) {
      case START -> {
        evaluateVictory(turnState.state());
        return process(TurnState.of(turnState.state(), TurnPhase.PRE_CAPTURE_COMPUTATION));
      }

      case PRE_CAPTURE_COMPUTATION -> {
        List<TurnOption> options = phaseResolver.resolvePreCapture(turnState.state());
        if (!options.isEmpty()) {
          return TurnState.of(turnState.state(), TurnPhase.PRE_CAPTURE_APPLICATION, options);
        }
        return process(TurnState.of(turnState.state(), TurnPhase.MOVE_COMPUTATION));
      }

      case PRE_CAPTURE_APPLICATION -> {
        boolean hasCaptured = action instanceof PreCaptureAction;
        AppliedResult result = actionApplier.apply(turnState.state(), action);
        GameState state = result.gameState();
        evaluateVictory(state);
        return process(
            new TurnState(
                state,
                TurnPhase.MOVE_COMPUTATION,
                null,
                hasCaptured,
                false,
                result.landingPosition()));
      }

      case MOVE_COMPUTATION -> {
        List<TurnOption> options;
        if (turnState.hasCaptured()) {
          Piece piece = turnState.state().board().getPieceAt(turnState.actorPos());
          PieceAtPosition pap = new PieceAtPosition(piece, turnState.actorPos());
          options = phaseResolver.resolveMove(turnState.state(), pap);
        } else {
          options = phaseResolver.resolveMove(turnState.state());
        }

        if (options.isEmpty()) {
          throw new PatException(turnState.state().currentPlayer());
        }

        return TurnState.of(turnState.state(), TurnPhase.MOVE_APPLICATION, options);
      }

      case MOVE_APPLICATION -> {
        AppliedResult result = actionApplier.apply(turnState.state(), action);
        GameState state = result.gameState();
        evaluateVictory(state);

        if (result.wasMoveIrregular()) {
          return process(TurnState.of(state, TurnPhase.END));
        }
        return process(
            TurnState.withPosition(
                state, TurnPhase.POST_CAPTURE_COMPUTATION, null, result.landingPosition()));
      }

      case POST_CAPTURE_COMPUTATION -> {
        List<TurnOption> options =
            phaseResolver.resolvePostCapture(turnState.state(), turnState.actorPos());
        if (!options.isEmpty()) {
          return TurnState.of(turnState.state(), TurnPhase.POST_CAPTURE_APPLICATION, options);
        }
        return process(TurnState.of(turnState.state(), TurnPhase.END));
      }

      case POST_CAPTURE_APPLICATION -> {
        AppliedResult result = actionApplier.apply(turnState.state(), action);
        GameState state = result.gameState();
        evaluateVictory(state);
        return process(TurnState.of(state, TurnPhase.END));
      }

      case END -> {
        return process(TurnState.of(turnState.state().switchPlayer(), TurnPhase.START));
      }

      default -> throw new IllegalArgumentException("Unrecognized TurnPhase: " + actualPhase);
    }
  }

  private boolean isApplicationPhase(TurnPhase phase) {
    return phase == TurnPhase.PRE_CAPTURE_APPLICATION
        || phase == TurnPhase.MOVE_APPLICATION
        || phase == TurnPhase.POST_CAPTURE_APPLICATION;
  }

  private void evaluateVictory(GameState state) throws VictoryException {
    List<Victory> victories = victoryEngine.evaluate(state);
    if (evaluator.isSatisfied(victories)) throw new VictoryException(state.currentPlayer());
  }
}
