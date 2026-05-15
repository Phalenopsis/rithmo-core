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

        for (PieceAtPosition piece : board.getPiecesForPlayer(state.currentPlayer())) {
            List<CaptureAction> actions = captureEngine.findCaptures(new CaptureContext(state, piece));

            List<List<CaptureAction>> subsets = resolveCaptureSubsets(actions);

            for (List<CaptureAction> subset : subsets) {
                List<Position> validLandings = calculateValidLandings(subset);

                options.add(new PreCaptureOption(List.copyOf(subset), validLandings));
            }
        }

        return options;
    }

    public List<PostCaptureOption> resolvePostCaptures(GameState state, Position attackerPos) {
        Piece piece = state.board().getPieceAt(attackerPos);
        if (piece == null) return List.of();

        List<CaptureAction> actions = captureEngine.findCaptures(
                new CaptureContext(state, new PieceAtPosition(piece, attackerPos))
        );

        return resolveCaptureSubsets(actions).stream()
                .map(PostCaptureOption::new)
                .toList();
    }

    private List<List<CaptureAction>> resolveCaptureSubsets(List<CaptureAction> actions) {
        Map<ActorKey, List<CaptureAction>> actionsByActor = groupByActor(actions);
        List<List<CaptureAction>> allValidSubsets = new ArrayList<>();

        for (List<CaptureAction> actorActions : actionsByActor.values()) {
            generateSubsets(actorActions).stream()
                    .filter(subset -> !subset.isEmpty() && isValidSubset(subset))
                    .forEach(allValidSubsets::add);
        }
        return allValidSubsets;
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

    private List<Position> calculateValidLandings(List<CaptureAction> subset) {
        Set<Position> validLandings = new LinkedHashSet<>();

        Map<Position, List<CaptureAction>> byPos = subset.stream()
                .collect(Collectors.groupingBy(CaptureAction::targetPosition));

        for (Map.Entry<Position, List<CaptureAction>> entry : byPos.entrySet()) {
            Position pos = entry.getKey();
            List<CaptureAction> actionsAtPos = entry.getValue();

            Piece parentTarget = actionsAtPos.getFirst().target().parentPiece();

            if (parentTarget instanceof Pyramid pyramid) {
                int totalCurrentComponents = pyramid.getComponents().size();
                int capturedInSubset = actionsAtPos.size();

                if (capturedInSubset >= totalCurrentComponents) {
                    validLandings.add(pos);
                }
            } else {
                validLandings.add(pos);
            }
        }
        return List.copyOf(validLandings);
    }

    // =====================================================
    // VALIDATION
    // =====================================================

    private boolean isValidSubset(List<CaptureAction> subset) {
        Set<Piece> targetComponents = new HashSet<>();

        for (CaptureAction action : subset) {
            if (!targetComponents.add(action.target().specificComponent())) {
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