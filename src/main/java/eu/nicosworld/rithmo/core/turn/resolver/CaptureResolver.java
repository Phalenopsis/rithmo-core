package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.model.CaptureContext;
import eu.nicosworld.rithmo.engine.model.*;

import java.util.*;
import java.util.stream.Collectors;

public class CaptureResolver {

    private final CaptureEngine captureEngine;

    public CaptureResolver(CaptureEngine captureEngine) {
        this.captureEngine = captureEngine;
    }

    public List<PreCaptureOption> resolvePreCaptures(GameState state) {

        List<PreCaptureOption> options = new ArrayList<>();

        Board board = state.board();
        List<PieceAtPosition> pieces =
                board.getPiecesForPlayer(state.currentPlayer());

        for (PieceAtPosition piece : pieces) {

            CaptureContext ctx = new CaptureContext(state, piece);

            List<CaptureAction> actions =
                    captureEngine.findCaptures(ctx);

            if (actions.isEmpty()) {
                continue;
            }

            // =====================================================
            // GROUP CAPTURES BY LOGICAL ACTOR
            // =====================================================

            Map<ActorKey, List<CaptureAction>> actionsByActor =
                    groupByActor(actions);

            // =====================================================
            // GENERATE COMBINATIONS PER ACTOR
            // =====================================================

            for (List<CaptureAction> actorActions : actionsByActor.values()) {

                List<List<CaptureAction>> subsets =
                        generateSubsets(actorActions);

                for (List<CaptureAction> subset : subsets) {

                    if (subset.isEmpty() || !isValidSubset(subset)) {
                        continue;
                    }

                    Set<Position> landings = subset.stream()
                            .map(CaptureAction::targetPosition)
                            .collect(Collectors.toCollection(LinkedHashSet::new));

                    options.add(
                            new PreCaptureOption(
                                    List.copyOf(subset),
                                    List.copyOf(landings)
                            )
                    );
                }
            }
        }

        return options;
    }

    public List<PostCaptureOption> resolvePostCaptures(
            GameState state,
            Position attackerPos
    ) {

        Board board = state.board();

        Piece piece = board.getPieceAt(attackerPos);

        if (piece == null) {
            return List.of();
        }

        PieceAtPosition movedPiece =
                new PieceAtPosition(piece, attackerPos);

        CaptureContext ctx =
                new CaptureContext(state, movedPiece);

        List<CaptureAction> actions =
                captureEngine.findCaptures(ctx);

        if (actions.isEmpty()) {
            return List.of();
        }

        // =====================================================
        // GROUP CAPTURES BY LOGICAL ACTOR
        // =====================================================

        Map<ActorKey, List<CaptureAction>> actionsByActor =
                groupByActor(actions);

        List<PostCaptureOption> options = new ArrayList<>();

        for (List<CaptureAction> actorActions : actionsByActor.values()) {

            List<List<CaptureAction>> subsets =
                    generateSubsets(actorActions);

            subsets.stream()
                    .filter(subset ->
                            !subset.isEmpty()
                                    && isValidSubset(subset)
                    )
                    .map(subset ->
                            new PostCaptureOption(List.copyOf(subset))
                    )
                    .forEach(options::add);
        }

        return options;
    }

    // =====================================================
    // GROUPING
    // =====================================================

    private Map<ActorKey, List<CaptureAction>> groupByActor(
            List<CaptureAction> actions
    ) {

        Map<ActorKey, List<CaptureAction>> grouped =
                new HashMap<>();

        for (CaptureAction action : actions) {

            ActorKey key = new ActorKey(
                    action.actor().parentPiece(),
                    action.actor().specificComponent()
            );

            grouped
                    .computeIfAbsent(key, k -> new ArrayList<>())
                    .add(action);
        }

        return grouped;
    }

    // =====================================================
    // SUBSETS (POWERSET)
    // =====================================================

    private List<List<CaptureAction>> generateSubsets(
            List<CaptureAction> actions
    ) {

        List<List<CaptureAction>> subsets =
                new ArrayList<>();

        int n = actions.size();

        int max = 1 << n; // 2^n

        for (int mask = 0; mask < max; mask++) {

            List<CaptureAction> subset =
                    new ArrayList<>();

            for (int i = 0; i < n; i++) {

                if ((mask & (1 << i)) != 0) {
                    subset.add(actions.get(i));
                }
            }

            subsets.add(subset);
        }

        return subsets;
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private boolean isValidSubset(List<CaptureAction> subset) {

        Set<Position> targets = new HashSet<>();

        for (CaptureAction action : subset) {

            // impossible de capturer deux fois la même case
            if (!targets.add(action.targetPosition())) {
                return false;
            }
        }

        return true;
    }

    // =====================================================
    // ACTOR IDENTITY
    // =====================================================

    private record ActorKey(
            Piece parentPiece,
            Piece specificComponent
    ) {
    }
}