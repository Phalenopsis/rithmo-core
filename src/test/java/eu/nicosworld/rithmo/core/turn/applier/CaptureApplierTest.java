package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.Position;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.move.MoveNature;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CaptureApplierTest {

    @Test
    void applyCapture() {
        // Arrange
        Position capturedAt = new Position(1, 1);

        BoardBuilder builder = new BoardBuilder(4,4);

        Board board = builder.blackCircle(4)
                .at(1,1)
                .build();

        // Act
        CaptureApplier applier = new CaptureApplier();
        Board newBoard = applier.applyCapture(board, capturedAt);

        Piece emptyCase = newBoard.getPieceAt(capturedAt);
        boolean isEmpty = newBoard.isEmpty(capturedAt);

        // Assert
        assertNull(emptyCase);
        assertTrue(isEmpty);
    }
}