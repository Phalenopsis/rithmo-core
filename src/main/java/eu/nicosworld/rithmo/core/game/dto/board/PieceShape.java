package eu.nicosworld.rithmo.core.game.dto.board;

import eu.nicosworld.rithmo.engine.model.PieceType;

public enum PieceShape {
    CIRCLE,
    TRIANGLE,
    SQUARE,
    PYRAMID;

    public static PieceShape mapShape(PieceType type) {
        return switch (type) {
            case CIRCLE -> PieceShape.CIRCLE;
            case TRIANGLE -> PieceShape.TRIANGLE;
            case SQUARE -> PieceShape.SQUARE;
            case PYRAMID -> PieceShape.PYRAMID;
        };
    }
}
