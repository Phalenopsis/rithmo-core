package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.MoveOption;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.move.Move;

/**
 * Internal execution action representing a validated movement to be applied to the game state.
 *
 * <p>A {@code MoveAction} is part of the engine execution layer and represents a concrete movement
 * derived from a {@link MoveOption}, which itself is produced by the resolver layer based on the
 * current game state.
 *
 * <p>It is not exposed to the UI layer and cannot be directly selected by the player. It is only
 * executed by the {@code TurnProcessor} through the application phase.
 *
 * @param actor The piece performing the movement at engine level.
 * @param move The movement definition (origin and destination) computed by the engine.
 */
public record MoveAction(Piece actor, Move move) implements TurnAction {
  /**
   * Creates a {@link MoveAction} from a {@link MoveOption}.
   *
   * <p>This method bridges the resolver layer and the execution layer by converting a validated
   * engine-level option into an executable action.
   *
   * <p>The option has already been validated by the resolver and represents a legal movement in the
   * current game state.
   *
   * @param option The engine-resolved movement option.
   * @return an executable action to be processed by the turn engine.
   */
  public static MoveAction from(MoveOption option) {
    return new MoveAction(option.actor().piece(), option.move());
  }
}
