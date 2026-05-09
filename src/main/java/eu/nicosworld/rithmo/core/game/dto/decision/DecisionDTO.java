package eu.nicosworld.rithmo.core.game.dto.decision;

import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @param actorId si applicable : la pièce qui doit jouer
 * @param capturedIdList si applicable : la liste des pièces capturées (ou des composants dans le cas de captures partielles)
 * @param landing si applicable : la position d'arrivée
 * @param skip représentation d'une action skip
 */
public record DecisionDTO(
        String actorId,
        Set<String> capturedIdList,
        Position landing,
        boolean skip
) {
    public static DecisionDTO from(Board board, MoveAction moveAction) {
        Piece piece = board.getPieceAt(moveAction.move().from());
        return new DecisionDTO(
                piece.getId(),
                Set.of(),
                moveAction.move().to(),
                false
        );
    }

    public static DecisionDTO from(PreCaptureAction action) {
        return new DecisionDTO(
                actorIdFrom(action.actions()),
                targetsIdFrom(action.actions()),
                action.landing(),
                false
        );
    }

    public static DecisionDTO from(PostCaptureAction action) {
        return new DecisionDTO(
                actorIdFrom(action.actions()),
                targetsIdFrom(action.actions()),
                null,
                false
                );
    }

    public static DecisionDTO from(SkipPostCaptureAction ignored) {
        return skipFrom();
    }

    public static DecisionDTO from(SkipPreCaptureAction ignored) {
        return skipFrom();
    }

    public static DecisionDTO skipFrom() {
        return new DecisionDTO(
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
            String actorId,
            List<String> capturedIdList,
            Position landing
    ) {
        return new DecisionDTO(
                actorId,
                new HashSet<>(capturedIdList),
                landing,
                false
        );
    }
}
