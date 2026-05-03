package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.model.CaptureContext;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.model.*;

import java.util.*;

public class CaptureResolver {

    private final CaptureEngine captureEngine;

    public CaptureResolver(CaptureEngine captureEngine) {
        this.captureEngine = captureEngine;
    }

    public List<PreCaptureOption> resolvePreCaptures(GameState state) {
        List<PreCaptureOption> options = new ArrayList<>();
        Board board = state.board();
        List<PieceAtPosition> pieces = board.getPiecesForPlayer(state.currentPlayer());

        for (PieceAtPosition piece : pieces) {
            CaptureContext ctx = new CaptureContext(state, piece);
            List<CaptureAction> actions = captureEngine.findCaptures(ctx);

            if (actions.isEmpty()) continue;

            List<List<CaptureAction>> subsets = generateSubsets(actions);

            for (List<CaptureAction> subset : subsets) {
                if (subset.isEmpty() || !isValidSubset(subset)) continue;

                subset.stream()
                        .map(CaptureAction::targetPosition)
                        .distinct()
                        .forEach(landing -> options.add(new PreCaptureOption(List.copyOf(subset), landing)));
            }
        }
        return options;
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

    public List<PostCaptureOption> resolvePostCaptures(GameState state, Position attackerPos) {
        Board board = state.board();
        Piece piece = board.getPieceAt(attackerPos);
        if (piece == null) return List.of();

        PieceAtPosition movedPiece = new PieceAtPosition(piece, attackerPos);
        CaptureContext ctx = new CaptureContext(state, movedPiece);
        List<CaptureAction> actions = captureEngine.findCaptures(ctx);

        if (actions.isEmpty()) return List.of();

        return generateSubsets(actions).stream()
                .filter(subset -> !subset.isEmpty() && isValidSubset(subset))
                .map(subset -> new PostCaptureOption(List.copyOf(subset)))
                .toList();
    }
}