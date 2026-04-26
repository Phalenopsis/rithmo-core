package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.engine.capture.CaptureAction;

import java.util.List;

public record PostCaptureAction(
        List<CaptureAction> actions
) implements TurnAction {
    public static PostCaptureAction from(PostCaptureOption option) {
        return new PostCaptureAction(List.copyOf(option.captures()));
    }
}
