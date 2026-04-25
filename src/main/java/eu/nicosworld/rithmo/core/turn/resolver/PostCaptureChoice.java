package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.engine.capture.CaptureAction;

import java.util.List;

import static eu.nicosworld.rithmo.core.turn.resolver.CaptureChoiceFormatter.formatActions;

public record PostCaptureChoice(
        List<CaptureAction> actions
) implements CaptureChoice {
    @Override
    public String toString() {
        return "PostCaptureChoice[" +
                formatActions(actions) +
                "]";
    }
}
