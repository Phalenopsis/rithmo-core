package eu.nicosworld.rithmo.core.game.dto.board;

import eu.nicosworld.rithmo.engine.model.*;

import java.util.ArrayList;
import java.util.List;

public record PieceDTO(
        String id,
        Position position,
        PieceShape shape,
        int value,
        PlayerColor owner,
        List<ComponentPieceDTO> components // Vide sauf pour la PYRAMID
) {
    public static PieceDTO mapFrom(PieceAtPosition pieceAtPosition) {
        List<ComponentPieceDTO> components = new ArrayList<>();

        if (pieceAtPosition.piece() instanceof Pyramid pyramid) {
            for (Piece component : pyramid.getComponents()) {
                components.add(new ComponentPieceDTO(
                        PieceShape.mapShape(component.getType()),
                        component.getValue()
                ));
            }
        }

        return new PieceDTO(
                pieceAtPosition.piece().getId(),
                pieceAtPosition.position(),
                PieceShape.mapShape(pieceAtPosition.piece().getType()),
                pieceAtPosition.piece().getValue(),
                pieceAtPosition.piece().getPlayer().getColor(),
                components
        );
    }
}
