package eu.nicosworld.rithmo.core.game.dto.status;

import eu.nicosworld.rithmo.core.exception.logical.NoPhaseException;
import eu.nicosworld.rithmo.core.turn.TurnPhase;

public enum PhaseDTO {
    PRE_CAPTURE, MOVE, POST_CAPTURE;

    public static PhaseDTO mapPhase(TurnPhase phase) throws NoPhaseException {
        return switch (phase) {
            case PRE_CAPTURE_APPLICATION -> PhaseDTO.PRE_CAPTURE;
            case MOVE_APPLICATION -> PhaseDTO.MOVE;
            case POST_CAPTURE_APPLICATION -> PhaseDTO.POST_CAPTURE;
            default -> throw new NoPhaseException(phase);
        };
    }
}
