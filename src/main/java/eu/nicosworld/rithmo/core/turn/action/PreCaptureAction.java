package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.resolver.PreCaptureChoice;
import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

public record PreCaptureAction(
        List<CaptureAction> actions,
        Position landingOption
) implements TurnAction {

    public static PreCaptureAction from(PreCaptureChoice choice, Position landing) {
        if (choice.landingOptions().contains(landing))
            return new PreCaptureAction(List.copyOf(choice.actions()), landing);
        throw new RuntimeException("Creating PreCaptureAction from PreCaptureChoice : Landing is not in landing choices.");
    }
}
