package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

/**
 * Represents a capture performed BEFORE the main movement phase.
 *
 * @param actions The list of engine-level capture primitives to execute.
 * @param landing The resulting position of the capturing piece after this action.
 */
public record PreCaptureAction(
        List<CaptureAction> actions,
        Position landing
) implements TurnAction {
    public static PreCaptureAction from(PreCaptureOption option) {
        return new PreCaptureAction(List.copyOf(option.actions()), option.landing());
    }
}
