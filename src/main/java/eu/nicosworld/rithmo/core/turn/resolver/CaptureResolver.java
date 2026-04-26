package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.CaptureContext;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.move.Move;

import java.util.*;

import static eu.nicosworld.rithmo.engine.move.MoveNature.IRREGULAR;

public class CaptureResolver {

    private final CaptureEngine captureEngine;

    public CaptureResolver(CaptureEngine captureEngine) {
        this.captureEngine = captureEngine;
    }

    public List<PreCaptureChoice> resolvePreCaptures(GameState state) {

        List<PreCaptureChoice> results = new ArrayList<>();
        Board board = state.board();

        List<PieceAtPosition> pieces =
                board.getPiecesForPlayer(state.currentPlayer());

        for (PieceAtPosition piece : pieces) {

            CaptureContext ctx = new CaptureContext(state, piece);
            List<CaptureAction> actions = captureEngine.findCaptures(ctx);

            if (actions.isEmpty()) continue;

            List<List<CaptureAction>> subsets = generateSubsets(actions);

            for (List<CaptureAction> subset : subsets) {

                if (subset.isEmpty()) continue;

                if (!isValidSubset(subset)) continue;

                List<Position> landingOptions = subset.stream()
                        .map(CaptureAction::targetPosition)
                        .distinct()
                        .toList();

                results.add(new PreCaptureChoice(subset, landingOptions));
            }
        }

        return results;
    }

    // =========================
    // SUBSETS (powerset)
    // =========================

    private List<List<CaptureAction>> generateSubsets(List<CaptureAction> actions) {

        List<List<CaptureAction>> subsets = new ArrayList<>();
        int n = actions.size();

        int max = 1 << n; // 2^n

        for (int mask = 0; mask < max; mask++) {

            List<CaptureAction> subset = new ArrayList<>();

            for (int i = 0; i < n; i++) {
                if ((mask & (1 << i)) != 0) {
                    subset.add(actions.get(i));
                }
            }

            subsets.add(subset);
        }

        return subsets;
    }

    // =========================
    // VALIDATION (simple version)
    // =========================

    private boolean isValidSubset(List<CaptureAction> subset) {

        Set<Position> targets = new HashSet<>();

        for (CaptureAction action : subset) {
            // éviter capturer deux fois la même case
            if (!targets.add(action.targetPosition())) {
                return false;
            }
        }

        return true;
    }

    public List<PostCaptureChoice> resolvePostCaptures(GameState state, Position AttackerPos) {

        List<PostCaptureChoice> results = new ArrayList<>();
        Board board = state.board();

        PieceAtPosition movedPiece = new PieceAtPosition(board.getPieceAt(AttackerPos), AttackerPos);

        CaptureContext ctx = new CaptureContext(state, movedPiece);

        List<CaptureAction> actions =
                captureEngine.findCaptures(ctx);

        if (actions.isEmpty()) {
            return List.of();
        }

        List<List<CaptureAction>> subsets =
                generateSubsets(actions);

        for (List<CaptureAction> subset : subsets) {

            if (subset.isEmpty()) continue;

            if (!isValidSubset(subset)) continue;

            results.add(new PostCaptureChoice(subset));
        }

        return results;
    }
}