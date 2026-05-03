package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;

import java.util.List;

/**
 * Represents a capture performed AFTER the main movement phase.
 *
 * @param actions The list of engine-level capture primitives to execute.
 */
public record PostCaptureAction(
        List<CaptureAction> actions
) implements TurnAction {
    public static PostCaptureAction from(PostCaptureOption option) {
        return new PostCaptureAction(List.copyOf(option.captures()));
    }
}
