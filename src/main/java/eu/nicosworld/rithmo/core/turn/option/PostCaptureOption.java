package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.capture.CaptureAction;

import java.util.List;

public record PostCaptureOption(
        List<CaptureAction> captures
) implements TurnOption {}
