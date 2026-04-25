package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.model.*;

import java.util.List;

public class CaptureApplier {

    public GameState applyCapture(GameState state, CaptureAction action) {

        Board board = state.board();

        // =========================
        // 1. REMOVE FROM BOARD
        // =========================

        if (action.isWholeCapture()) {
            board = board.removePiece(action.targetPosition());
        } else {
            // partial capture (pyramid)
            Piece target = action.target();

            if (target instanceof Pyramid pyramid) {
                Pyramid updated = pyramid.removeComponent(action.capturedPiece());

                board = board.removePiece(action.targetPosition());

                // if pyramid still has components → re-add it
                if (!updated.getComponents().isEmpty()) {
                    board = board.addPiece(updated, action.targetPosition());
                }
            }
        }

        // =========================
        // 2. UPDATE PLAYER ASSETS
        // =========================

        Player currentPlayer = state.currentPlayer();

        PlayerAssets assets = state.assetsOf(currentPlayer);
        PlayerAssets updatedAssets;
        if(action.isWholeCapture()) {
            updatedAssets =
                    assets.captureAndStore(action.capturedPiece());
        } else {
            updatedAssets = assets.addCaptured(action.capturedPiece());
        }


        state = state.withAssets(currentPlayer.getColor(), updatedAssets);

        // =========================
        // 3. UPDATE BOARD
        // =========================

        return state.withBoard(board);
    }

    public GameState applyCaptures(GameState state, List<CaptureAction> actions) {
        for (CaptureAction action : actions) {
            state = applyCapture(state, action);
        }
        return state;
    }
}