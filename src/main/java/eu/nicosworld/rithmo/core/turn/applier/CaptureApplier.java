package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.*;

import java.util.List;

public class CaptureApplier {

    public GameState applyCapture(GameState state, CaptureAction action) {
        Board board = state.board();
        Piece capturedPiece = action.capturedPiece();

        // 1. Calcul du nouveau Board
        if (action.isWholeCapture()) {
            board = board.removePiece(action.targetPosition());
        } else if (action.target().parentPiece() instanceof Pyramid) {
            Pyramid pyramid = (Pyramid) board.getPieceAt(action.targetPosition());
            Pyramid updated = pyramid.removeComponent(capturedPiece);
            board = board.removePiece(action.targetPosition());
            if (!updated.getComponents().isEmpty()) {
                board = board.addPiece(updated, action.targetPosition());
            }
        }

        // 2. Calcul des nouveaux Assets
        PlayerAssets assets = state.assetsOfCurrentPlayer();
        PlayerAssets updatedAssets = (action.isWholeCapture() && !capturedPiece.isPyramid())
                ? assets.captureAndStore(capturedPiece)
                : assets.addCaptured(capturedPiece);

        // 3. Retour du nouvel état via ton API fluide
        return state.withBoard(board)
                .withAssets(state.currentPlayer().getColor(), updatedAssets);
    }

    public GameState applyCaptures(GameState state, List<CaptureAction> actions) {
        GameState result = state;
        for (CaptureAction action : actions) {
            result = applyCapture(result, action);
        }
        return result;
    }
}