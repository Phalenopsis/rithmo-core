package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceShape;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.Objects;

public final class PieceRepresentationHelper {

    private PieceRepresentationHelper() {
    }

    public static PieceDTO findPieceOrComponent(
            GameStatusDTO statusDTO,
            String representation
    ) {

        for (PieceDTO piece : statusDTO.board().pieces()) {

            // full piece
            if (matches(piece, representation)) {
                return piece;
            }

            // pyramid components
            if (piece.shape() == PieceShape.PYRAMID) {

                for (PieceDTO component : piece.components()) {

                    if (matches(component, representation)) {
                        return component;
                    }
                }
            }
        }

        throw new RuntimeException(
                "No piece or component found for representation: "
                        + representation
        );
    }

    public static String findId(
            GameStatusDTO statusDTO,
            String representation
    ) {

        return findPieceOrComponent(statusDTO, representation)
                .id();
    }

    public static boolean matches(
            PieceDTO piece,
            String representation
    ) {

        if (piece == null) {
            return false;
        }

        return toRepresentation(piece)
                .equals(representation);
    }

    /**
     * Representation without position.
     *
     * Examples:
     * WC5
     * BP91
     * BT6
     */
    public static String toShortRepresentation(PieceDTO piece) {

        if (piece == null) {
            return "null";
        }

        String owner =
                piece.owner() == null
                        ? "?"
                        : piece.owner()
                        .name()
                        .substring(0, 1);

        String shape =
                shapeCode(piece);

        String value =
                String.valueOf(piece.value());

        return owner
                + shape
                + value;
    }

    /**
     * Representation with position.
     *
     * Examples:
     * WC5(2,0)
     * BP91(4,4)
     */
    public static String toRepresentation(PieceDTO piece) {

        if (piece == null) {
            return "null";
        }

        return toShortRepresentation(piece)
                + (
                piece.position() == null
                        ? "(?,?)"
                        : formatPosition(piece.position())
        );
    }

    public static String formatPosition(Position position) {

        if (position == null) {
            return "(?,?)";
        }

        return "("
                + position.getX()
                + ","
                + position.getY()
                + ")";
    }

    private static String shapeCode(PieceDTO piece) {

        if (piece == null || piece.shape() == null) {
            return "?";
        }

        return switch (piece.shape()) {
            case CIRCLE -> "C";
            case SQUARE -> "S";
            case TRIANGLE -> "T";
            case PYRAMID -> "P";
        };
    }
}