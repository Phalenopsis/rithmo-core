package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.core.turn.resolver.CaptureChoice;

public record PreCaptureOption(
        CaptureChoice choice
) implements TurnOption {}
