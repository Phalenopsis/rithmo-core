package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.model.CaptureContext;
import eu.nicosworld.rithmo.engine.model.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Resolves all valid capture combinations available during a turn.
 * <p>
 * This resolver is responsible for:
 * <ul>
 *     <li>finding all capture actions available to a player or piece</li>
 *     <li>grouping captures by logical acting component</li>
 *     <li>generating all valid subsets of compatible captures</li>
 *     <li>determining valid landing positions for captured-piece redeployment</li>
 * </ul>
 * <p>
 * Capture subsets are validated to ensure that the same target component
 * cannot be captured multiple times within a single option.
 */
public class CaptureResolver {

    private final CaptureEngine captureEngine;

    /**
     * Creates a new capture resolver.
     *
     * @param captureEngine
     *         engine responsible for computing raw capture actions
     */
    public CaptureResolver(CaptureEngine captureEngine) {
        this.captureEngine = captureEngine;
    }

    /**
     * Resolves all pre-move capture options available for the current player.
     * <p>
     * Each option contains:
     * <ul>
     *     <li>a valid subset of compatible capture actions</li>
     *     <li>the list of positions where captured pieces may be redeployed</li>
     * </ul>
     *
     * @param state
     *         current game state
     *
     * @return all available pre-capture options
     */
    public List<PreCaptureOption> resolvePreCaptures(GameState state) {
        List<PreCaptureOption> options = new ArrayList<>();
        Board board = state.board();

        for (PieceAtPosition piece : board.getPiecesForPlayer(state.currentPlayer())) {
            List<CaptureAction> actions = captureEngine.findCaptures(new CaptureContext(state, piece));

            List<List<CaptureAction>> subsets = resolveCaptureSubsets(actions);

            for (List<CaptureAction> subset : subsets) {
                List<Position> validLandings = calculateValidLandings(subset);
                Piece realActor = subset.getFirst().actor().specificComponent();
                PieceAtPosition realActorAtPosition = new PieceAtPosition(realActor, piece.position());

                options.add(new PreCaptureOption(realActorAtPosition, List.copyOf(subset), validLandings));
            }
        }

        return options;
    }

    /**
     * Resolves all post-move capture options available for a moved piece.
     *
     * @param state
     *         current game state
     *
     * @param attackerPos
     *         position of the attacking piece
     *
     * @return all available post-capture options
     */
    public List<PostCaptureOption> resolvePostCaptures(GameState state, Position attackerPos) {
        Piece piece = state.board().getPieceAt(attackerPos);
        if (piece == null) return List.of();

        List<CaptureAction> actions = captureEngine.findCaptures(
                new CaptureContext(state, new PieceAtPosition(piece, attackerPos))
        );

        return resolveCaptureSubsets(actions).stream()
                .map(PostCaptureOption::from)
                .toList();
    }

    /**
     * Resolves every valid subset of compatible capture actions.
     * <p>
     * Capture actions are first grouped by acting component,
     * then all possible subsets are generated and validated.
     *
     * @param actions
     *         raw capture actions
     *
     * @return all valid capture subsets
     */
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

    /**
     * Groups capture actions by logical actor identity.
     * <p>
     * Two actions belong to the same actor group if they originate
     * from the same parent piece and the same acting component.
     *
     * @param actions
     *         capture actions to group
     *
     * @return grouped capture actions by actor identity
     */
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

    /**
     * Generates the powerset of the provided capture actions.
     * <p>
     * The returned list includes the empty subset.
     *
     * @param actions
     *         source actions
     *
     * @return all possible subsets
     */
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

    /**
     * Computes all valid landing positions for a capture subset.
     * <p>
     * A landing position is valid if:
     * <ul>
     *     <li>the target is not a pyramid</li>
     *     <li>or the subset captures every remaining component of the pyramid</li>
     * </ul>
     *
     * @param subset
     *         capture subset to evaluate
     *
     * @return ordered list of valid landing positions
     */
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

    /**
     * Validates a capture subset.
     * <p>
     * A subset is considered invalid if the same target component
     * is captured more than once.
     *
     * @param subset
     *         subset to validate
     *
     * @return {@code true} if the subset is valid
     */
    private boolean isValidSubset(List<CaptureAction> subset) {
        Set<Piece> targetComponents = new HashSet<>();

        for (CaptureAction action : subset) {
            if (!targetComponents.add(action.target().specificComponent())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Identifies a logical capture actor.
     *
     * @param parentPiece
     *         parent piece owning the acting component
     *
     * @param specificComponent
     *         specific component performing the capture
     */
    private record ActorKey(
            Piece parentPiece,
            Piece specificComponent
    ) {
    }
}