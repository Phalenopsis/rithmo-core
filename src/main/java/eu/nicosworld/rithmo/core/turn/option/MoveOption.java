package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;
import eu.nicosworld.rithmo.engine.move.Move;

/**
 * A proposal for a movement on the board.
 *
 * @param move The engine-level move details.
 */
public record MoveOption(
        PieceAtPosition actor,
        Move move
) implements TurnOption {
    public static MoveOption from(Piece actor, Move move) {
        return new MoveOption(
                new PieceAtPosition(actor, move.from()),
                move
        );
    }
}