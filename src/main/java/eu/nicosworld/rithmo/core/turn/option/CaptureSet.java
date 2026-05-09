package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;

import java.util.List;

public record CaptureSet(List<CaptureAction> captures) {
}
