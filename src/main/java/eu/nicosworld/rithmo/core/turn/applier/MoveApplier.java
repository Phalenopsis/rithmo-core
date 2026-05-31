package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.move.Move;

public class MoveApplier {
  public Board applyMove(Board board, Move move) {
    Piece piece = board.getPieceAt(move.from());

    Board boardWithoutMovingPiece = board.removePiece(move.from());

    return boardWithoutMovingPiece.addPiece(piece, move.to());
  }
}
