package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.SkipPreCaptureOption;

public record SkipPreCaptureAction()
        implements TurnAction {
    public static SkipPreCaptureAction from(SkipPreCaptureOption option) {
        return new SkipPreCaptureAction();
    }
}
