package eu.nicosworld.rithmo.core.game.dto.board;

import eu.nicosworld.rithmo.engine.capture.model.InvolvedPiece;
import eu.nicosworld.rithmo.engine.model.Position;

public record InvolvedPieceDTO(
    PieceDTO parentPiece, // La pièce sur le board (Pyramid ou SimplePiece)
    Position position, // Sa position
    PieceDTO specificComponent // Le composant précis (ex: Triangle 36)
    ) {
  public static InvolvedPieceDTO from(InvolvedPiece involvedPiece) {
    return new InvolvedPieceDTO(
        PieceDTO.from(involvedPiece.parentPiece(), involvedPiece.position()),
        involvedPiece.position(),
        PieceDTO.from(involvedPiece.specificComponent(), involvedPiece.position()));
  }
}
