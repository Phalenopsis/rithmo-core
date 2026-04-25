package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.Pyramid;

import java.util.List;

/**
 * debug helper
 */
public class CaptureChoiceFormatter {

    public static String formatActions(List<CaptureAction> actions) {
        return actions.stream()
                .map(CaptureChoiceFormatter::formatAction)
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    public static String formatAction(CaptureAction action) {

        return formatPiece(action.attacker()) +
                "@" + action.attackerPosition() +
                " -> " +
                formatCaptured(action) +
                "@" + action.targetPosition();
    }

    private static String formatCaptured(CaptureAction action) {
        if (action.isWholeCapture()) {
            return formatPiece(action.target());
        }
        return formatPiece(action.capturedPiece()) + " (partial)";
    }

    private static String formatPiece(Piece piece) {
        String base = piece.getType().name();
        int value = piece.getValue();

        if (piece instanceof Pyramid pyramid) {
            String components = pyramid.getComponents().stream()
                    .map(p -> p.getType().name() + "(" + p.getValue() + ")")
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            return base + "(" + value + ")[" + components + "]";
        }

        return base + "(" + value + ")";
    }
}
