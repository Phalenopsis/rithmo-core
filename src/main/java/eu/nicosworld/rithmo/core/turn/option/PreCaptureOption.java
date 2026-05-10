package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

public record PreCaptureOption(
        List<CaptureAction> captures,
        List<Position> possibleLandings
) implements TurnOption {}
