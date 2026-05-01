package eu.nicosworld.rithmo.core.game.dto.board;

import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;

import java.util.List;

public record BoardDTO(List<PieceDTO> pieces) {
    public static BoardDTO mapFrom(Board board) {
        List<PieceAtPosition> piecesAtPosition = board.getPiecesWithPositions();
        List<PieceDTO> pieces = piecesAtPosition.stream()
                .map(PieceDTO::mapFrom)
                .toList();
        return new BoardDTO(pieces);
    }
}
