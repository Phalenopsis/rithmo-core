package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

/**
 * A proposal for a capture sequence to be executed before moving.
 *
 * @param captures The list of captures included in this option.
 * @param landing The final position of the piece if this sequence is chosen.
 */
public record PreCaptureOption(
        List<CaptureAction> captures,
        Position landing
) implements TurnOption {}
