package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

public record PreCaptureAction(
        List<CaptureAction> actions,
        Position landingOption
) implements TurnAction {}
