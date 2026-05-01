package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.SkipPreCaptureOption;

/**
 * Explicitly declines a possible capture before moving.
 */
public record SkipPreCaptureAction() implements TurnAction {
    public static SkipPreCaptureAction from(SkipPreCaptureOption option) {
        return new SkipPreCaptureAction();
    }
}
