package eu.nicosworld.rithmo.core.turn;

import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.core.turn.applier.ActionApplier;
import eu.nicosworld.rithmo.core.turn.applier.AppliedResult;
import eu.nicosworld.rithmo.core.turn.option.TurnOption;
import eu.nicosworld.rithmo.core.turn.resolver.PhaseResolver;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;
import eu.nicosworld.rithmo.engine.victory.VictoryEngine;

import java.util.List;

public class TurnProcessor {

    private final ActionApplier actionApplier;
    private final PhaseResolver phaseResolver;
    private final VictoryEngine victoryEngine;

    public TurnProcessor(
            ActionApplier actionApplier,
            PhaseResolver phaseResolver,
            VictoryEngine victoryEngine
    ) {
        this.actionApplier = actionApplier;
        this.phaseResolver = phaseResolver;
        this.victoryEngine = victoryEngine;
    }

    public TurnState process(TurnState turnState, TurnAction action) throws VictoryException, PatException {
        TurnPhase actualPhase = turnState.phase();
        switch (actualPhase) {
            case START -> {
                // check Victory
                checkVictory(turnState.state());
                // run to PRE_CAPTURE_COMPUTATION
                return process(
                        TurnState.of(
                            turnState.state(),
                            TurnPhase.PRE_CAPTURE_COMPUTATION
                        ),
                        null);
            }

            case PRE_CAPTURE_COMPUTATION -> {
                // calcule les captures possibles
                List<TurnOption> options = phaseResolver.resolvePreCapture(turnState.state());
                // si capture possible
                // => avance à PRE_CAPTURE_APPLICATION
                // renvoie les options
                if(!options.isEmpty()) {
                    return TurnState.of(
                            turnState.state(),
                            TurnPhase.PRE_CAPTURE_APPLICATION,
                            options
                    );
                }

                // sinon, avance à MOVE_COMPUTATION
                return process(
                        TurnState.of(
                                turnState.state(),
                                TurnPhase.MOVE_COMPUTATION
                        ), null
                ) ;
            }

            case PRE_CAPTURE_APPLICATION -> {
                boolean hasCaptured = action instanceof PreCaptureAction;
                AppliedResult result = actionApplier.apply(turnState.state(), action);
                GameState state = result.gameState();
                checkVictory(state);
                return process(
                        new TurnState(
                                state,
                                TurnPhase.MOVE_COMPUTATION,
                                null,
                                hasCaptured,
                                false,
                                result.landingPosition()
                        ), null
                );
            }

            case MOVE_COMPUTATION -> {
                List<TurnOption> options;
                // calcule les moves possibles
                // avance à MOVE_APPLICATION
                // renvoie les options
                if(turnState.hasCaptured()) {
                    // si hasCaptured => moves limités à REGULAR, avec la pièce qui a capturé
                    Piece piece = turnState.state().board().getPieceAt(turnState.attackerPos());
                    PieceAtPosition pap = new PieceAtPosition(piece, turnState.attackerPos());
                    options = phaseResolver.resolveMove(turnState.state(), pap);
                } else {
                    options = phaseResolver.resolveMove(turnState.state());
                }

                if(options.isEmpty()) {
                    throw new PatException(turnState.state().currentPlayer());
                }

                return TurnState.of(
                        turnState.state(),
                        TurnPhase.MOVE_APPLICATION,
                        options
                );
            }

            case MOVE_APPLICATION -> {
                // applique le move
                AppliedResult result = actionApplier.apply(turnState.state(), action);
                // check victoire
                GameState state = result.gameState();
                checkVictory(state);

                // Si move est IRREGULAR
                if(result.wasMoveIrregular()) {
                    return process(TurnState.of(
                                        state,
                                        TurnPhase.END),
                            null);
                }
                // si move est REGULAR
                // fait avancer à POST_CAPTURE_COMPUTATION
                return process(TurnState.withPosition(
                                state,
                                TurnPhase.POST_CAPTURE_COMPUTATION,
                                null,
                                result.landingPosition()),
                        null);
            }

            case POST_CAPTURE_COMPUTATION -> {
                // calcule les captures possibles
                List<TurnOption> options = phaseResolver.resolvePostCapture(turnState.state(), turnState.attackerPos());
                // avance à POST_CAPTURE_APPLICATION
                // renvoie les options
                if(!options.isEmpty()) {
                    return TurnState.of(
                            turnState.state(),
                            TurnPhase.POST_CAPTURE_APPLICATION,
                            options
                    );
                }
                // si pas de capture, avance à END
                return process(
                        TurnState.of(
                                turnState.state(),
                                TurnPhase.END
                        ), null
                );
            }

            case POST_CAPTURE_APPLICATION -> {
                // applique le move
                AppliedResult result = actionApplier.apply(turnState.state(), action);
                // check victoire
                GameState state = result.gameState();
                checkVictory(state);
                return process(
                        TurnState.of(
                                state,
                                TurnPhase.END
                        ), null
                );
            }

            case END -> {
                // switch player
                // fait avancer à START
                return process(
                        TurnState.of(
                                turnState.state().switchPlayer(),
                                TurnPhase.START), null
                );
            }
            default -> throw new IllegalArgumentException("Unrecognized TurnPhase");

        }
    }

    public void checkVictory(GameState state) throws VictoryException {
        if(victoryEngine.check(state))
            throw new VictoryException(state.currentPlayer());
    }
}