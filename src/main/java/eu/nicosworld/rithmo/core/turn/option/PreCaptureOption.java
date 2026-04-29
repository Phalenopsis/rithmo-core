package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

public record PreCaptureOption(
        List<CaptureAction> actions,
        Position landing
) implements TurnOption {}
