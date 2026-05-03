package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;

import java.util.List;

/**
 * A proposal for a capture sequence available after the main move.
 *
 * @param captures The list of captures available.
 */
public record PostCaptureOption(
        List<CaptureAction> captures
) implements TurnOption {}
