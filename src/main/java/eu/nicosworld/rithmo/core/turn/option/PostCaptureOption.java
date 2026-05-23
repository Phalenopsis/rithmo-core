package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;

import java.util.List;

/**
 * Represents a set of executable post-move capture possibilities
 * available for a single acting piece.
 *
 * <p>A {@code PostCaptureOption} is produced by the engine after a movement
 * has been applied and additional captures become available according
 * to the game rules.</p>
 *
 * <p>Unlike {@link MoveOption}, this option may contain several possible
 * capture actions for the same actor.</p>
 *
 * <p>The acting piece context is embedded directly in the option in order
 * to avoid external board lookups during application-layer projection
 * and UI assembly.</p>
 *
 * @param actor    The acting piece together with its current board position.
 * @param captures The executable capture possibilities available to the actor.
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
