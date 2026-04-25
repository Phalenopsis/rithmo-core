package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

import static eu.nicosworld.rithmo.core.turn.resolver.CaptureChoiceFormatter.formatActions;

public record PreCaptureChoice(
        List<CaptureAction> actions,
        List<Position> landingOptions
) implements CaptureChoice {
    @Override
    public String toString() {
        return "PreCaptureChoice[" +
                formatActions(actions) +
                " => landingOption " + landingOptions +
                "]";
    }
}
