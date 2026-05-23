package eu.nicosworld.rithmo.core.game.dto.decision;

import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Public representation of a fully validated player decision.
 *
 * <p>A {@code DecisionDTO} represents a complete playable intent reconstructed
 * from one or several player-side option selections.</p>
 *
 * <p>Unlike atomic {@code TurnOption}s produced by the engine,
 * a decision corresponds to a fully executable choice that can be:
 * <ul>
 *     <li>selected and reconstructed by the UI</li>
 *     <li>matched against available executable decisions</li>
 *     <li>identified by a stable public identifier returned for execution</li>
 * </ul>
 * </p>
 *
 * <p>Examples:
 * <ul>
 *     <li>a simple movement generates one decision</li>
 *     <li>a pre-capture sequence may generate several composite decisions
 *     depending on selected captures and landing position</li>
 * </ul>
 * </p>
 *
 * <p>The associated executable {@link eu.nicosworld.rithmo.core.turn.action.TurnAction}
 * remains an internal execution object and is not exposed directly to the UI.</p>
 *
 * @param id
 *         Stable public identifier of the decision.
 *
 * @param actorId
 *         Identifier of the acting piece or pyramid component responsible
 *         for the action, when applicable.
 *
 * @param capturedIdList
 *         Identifiers of captured pieces and/or pyramid components involved
 *         in the decision.
 *
 * @param landing
 *         Target landing position associated with the decision, when applicable.
 *
 * @param skip
 *         Indicates that the player voluntarily skips the optional action
 *         associated with the current phase.
 */
public record DecisionDTO(
        UUID id,
        String actorId,
        Set<String> capturedIdList,
        Position landing,
        boolean skip
) {
    /**
     * Creates a {@link DecisionDTO} representing a simple move action.
     *
     * <p>This method maps an internal {@link MoveAction} (engine-level execution intent)
     * into a public decision representation that can be exposed to the UI.</p>
     *
     * <p>The resulting decision is atomic: it corresponds to a single movement
     * with no captured pieces and a defined destination position.</p>
     *
     * @param id
     *         Stable identifier assigned to this decision.
     *
     * @param moveAction
     *         Internal engine action describing the piece movement to execute.
     *
     * @return a UI-facing {@link DecisionDTO} representing the move.
     */
    public static DecisionDTO from(UUID id, MoveAction moveAction) {
        return new DecisionDTO(
                id,
                moveAction.actor().getId(),
                Set.of(),
                moveAction.move().to(),
                false
        );
    }

    /**
     * Creates a {@link DecisionDTO} representing a pre-capture composite action.
     *
     * <p>This method maps an internal {@link PreCaptureAction} (engine-level capture sequence)
     * into a public decision that represents a fully composed capture intent.</p>
     *
     * <p>A pre-capture decision may involve:
     * <ul>
     *     <li>multiple captured pieces or components</li>
     *     <li>a final landing position selected later in the turn flow</li>
     * </ul>
     * </p>
     *
     * @param id stable identifier assigned to this decision
     * @param action internal engine action describing the capture sequence
     * @return UI-facing decision representing the capture intent
     */
    public static DecisionDTO from(UUID id, PreCaptureAction action) {
        return new DecisionDTO(
                id,
                actorIdFrom(action.actions()),
                targetsIdFrom(action.actions()),
                action.landing(),
                false
        );
    }

    /**
     * Creates a {@link DecisionDTO} representing a post-capture action.
     *
     * <p>This method maps an internal {@link PostCaptureAction} into a public decision
     * corresponding to a capture sequence performed after a movement.</p>
     *
     * <p>The resulting decision may include multiple captured elements,
     * but does not include a landing position, as the movement has already been resolved.</p>
     *
     * @param id stable identifier assigned to this decision
     * @param action internal engine action describing the capture sequence
     * @return UI-facing decision representing the post-move capture intent
     */
    public static DecisionDTO from(UUID id, PostCaptureAction action) {
        return new DecisionDTO(
                id,
                actorIdFrom(action.actions()),
                targetsIdFrom(action.actions()),
                null,
                false
                );
    }

    /**
     * Creates a {@link DecisionDTO} representing a reintroduction action.
     *
     * <p>This method maps an internal {@link ReintroductionAction} into a public decision
     * corresponding to the re-entry of a previously captured piece onto the board.</p>
     *
     * <p>This is an atomic decision: it involves a single piece and a single destination
     * without additional composition.</p>
     *
     * @param id stable identifier assigned to this decision
     * @param action internal engine action describing the reintroduction
     * @return UI-facing decision representing the reintroduction intent
     */
    public static DecisionDTO from(UUID id, ReintroductionAction action) {
        return new DecisionDTO(
                id,
                action.reintroduction().piece().getId(),
                Set.of(),
                action.reintroduction().position(),
                false
        );
    }

    /**
     * Creates a {@link DecisionDTO} representing a skipped optional action.
     *
     * <p>This decision indicates that the player explicitly chooses not to perform
     * an optional phase action (such as capture phases).</p>
     *
     * <p>Skip decisions are used to transition the turn flow to the next phase
     * without executing any engine-level action.</p>
     *
     * @param id stable identifier assigned to this decision
     * @return UI-facing decision representing the skip choice
     */
    public static DecisionDTO skipFrom(UUID id) {
        return new DecisionDTO(
                id,
                null,
                null,
                null,
                true
        );
    }

    private static String actorIdFrom(List<CaptureAction> actions) {
        return actions.getFirst().actor().specificComponent().getId();
    }

    private static Set<String> targetsIdFrom(List<CaptureAction> actions) {
        return actions.stream()
                .map(a -> a.capturedPiece().getId())
                .collect(Collectors.toSet());
    }

    public static DecisionDTO preCaptureDecision(
            UUID id,
            String actorId,
            List<String> capturedIdList,
            Position landing
    ) {
        return new DecisionDTO(
                id,
                actorId,
                new HashSet<>(capturedIdList),
                landing,
                false
        );
    }
}
