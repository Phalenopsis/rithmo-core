package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;

import java.util.List;

/**
 * A proposal for a capture sequence available after the main move.
 *
 * @param captures The list of captures available.
 */
public record PostCaptureOption(
        PieceAtPosition actor,
        List<CaptureAction> captures
) implements TurnOption {
    public static PostCaptureOption from(List<CaptureAction> captures) {
        return new PostCaptureOption(
                new PieceAtPosition(
                        captures.getFirst().actor().specificComponent(),
                        captures.getFirst().actor().position()),
                captures
                );
    }
}
