package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.SkipPostCaptureOption;

public record SkipPostCaptureAction()
        implements TurnAction {
    public static SkipPostCaptureAction from(SkipPostCaptureOption option) {
        return new SkipPostCaptureAction();
    }
}
