package eu.nicosworld.rithmo.core.game.dto.board;

import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.engine.capture.model.InvolvedPiece;
import eu.nicosworld.rithmo.engine.model.*;

import java.util.ArrayList;
import java.util.List;

public record PieceDTO(
        String id,
        Position position,
        PieceShape shape,
        int value,
        PlayerColorDTO owner,
        List<PieceDTO> components // Vide sauf pour la PYRAMID
) {
    public static final PieceDTO GLOBAL_OPTION = PieceDTO.empty();

    public static PieceDTO empty() {
        return new PieceDTO(null, null, null, 0, null, null);
    }

    public static PieceDTO from(PieceAtPosition pieceAtPosition) {
        List<PieceDTO> components = new ArrayList<>();

        if (pieceAtPosition.piece() instanceof Pyramid pyramid) {
            for (Piece component : pyramid.getComponents()) {
                components.add(PieceDTO.componentFrom(component, pieceAtPosition.position()));
            }
        }

        return new PieceDTO(
                pieceAtPosition.piece().getId(),
                pieceAtPosition.position(),
                PieceShape.mapShape(pieceAtPosition.piece().getType()),
                pieceAtPosition.piece().getValue(),
                PlayerColorDTO.mapColor(pieceAtPosition.piece().getPlayer().getColor()),
                components
        );
    }

    public static PieceDTO componentFrom(Piece piece, Position position) {
        if(piece instanceof Pyramid) {
            throw new IllegalArgumentException("PieceDTO.mapComponentFrom() : A component can't be be pyramid.");
        }
        return new PieceDTO(
                piece.getId(),
                position,
                PieceShape.mapShape(piece.getType()),
                piece.getValue(),
                PlayerColorDTO.mapColor(piece.getPlayer().getColor()),
                List.of()
        );
    }

    public static PieceDTO from(Piece piece, Position position) {
        return from(new PieceAtPosition(piece, position));
    }

    public static PieceDTO from(InvolvedPiece dto) {
        return from(dto.specificComponent(), dto.position());
    }
}
