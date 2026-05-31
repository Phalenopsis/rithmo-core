package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.move.MoveNature;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
import eu.nicosworld.rithmo.engine.testutils.GameStateAssertion;
import org.junit.jupiter.api.Test;

class MoveApplierTest {

  @Test
  void testApplyMove() {
    // Arrange
    Position from = new Position(1, 1);
    Position to = new Position(2, 2);
    Move move = new Move(from, to, MoveNature.REGULAR);

    BoardBuilder builder = new BoardBuilder(4, 4);

    Board board = builder.blackCircle(4).at(1, 1).build();

    Piece pieceToMove = board.getPieceAt(from);

    // Act
    MoveApplier applier = new MoveApplier();
    Board newBoard = applier.applyMove(board, move);

    GameState gameState = GameState.initial(newBoard, Player.BLACK);

    GameStateAssertion.assertThis(gameState)
        .isEmpty(from)
        .player(Player.BLACK)
        .hasOnBoard(pieceToMove)
        .at(to);
  }
}
