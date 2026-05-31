package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.BoardDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceShape;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PreCaptureOptionDTO;
import eu.nicosworld.rithmo.engine.model.Position;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FindDecisionHelper {

    public static UUID findAnyNonSkipDecision(
            GameStatusDTO statusDTO
    ) {

        return findNonSkipDecision(statusDTO)
                .map(DecisionDTO::id)
                .findFirst()
                .orElse(null);
    }

    public static UUID findSkipDecision(
            GameStatusDTO statusDTO
    ) {

        return statusDTO.possibleDecisions()
                .stream()
                .filter(DecisionDTO::skip)
                .map(DecisionDTO::id)
                .findFirst()
                .orElse(null);
    }

    public static UUID findDecisionWithCaptures(
            GameStatusDTO statusDTO,
            int nbCaptures
    ) {

        return statusDTO.possibleDecisions()
                .stream()
                .filter(decision ->
                        decision.capturedIdList() != null
                                && decision.capturedIdList().size() == nbCaptures
                )
                .map(DecisionDTO::id)
                .findFirst()
                .orElseThrow();
    }

    /**
     * Find a move decision by actor representation and landing position.
     * <p>
     * Examples:
     * <pre>
     * findMoveDecisionId(status, "BP91(0,0)", "(2,0)");
     * findMoveDecisionId(status, "WC4(3,1)", "(3,2)");
     * </pre>
     *
     * Only decisions without captures are considered.
     */
    public static UUID findMoveDecisionId(
            GameStatusDTO statusDTO,
            String actor,
            String landingString
    ) {

        String actorId =
                PieceRepresentationHelper.findId(
                        statusDTO,
                        actor
                );

        Position landing =
                parsePosition(landingString);

        return findDecisionsFor(statusDTO, actorId)
                .filter(decision ->
                        decision.capturedIdList() == null
                                || decision.capturedIdList().isEmpty()
                )
                .filter(decision ->
                        Objects.equals(
                                decision.landing(),
                                landing
                        )
                )
                .map(DecisionDTO::id)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "No matching move decision found for actor "
                                        + actor
                                        + " to "
                                        + landingString
                        )
                );
    }

    private static Position parsePosition(String value) {

        try {

            String cleaned =
                    value.replace("(", "")
                            .replace(")", "")
                            .trim();

            String[] split =
                    cleaned.split(",");

            int x =
                    Integer.parseInt(split[0].trim());

            int y =
                    Integer.parseInt(split[1].trim());

            return new Position(x, y);

        } catch (Exception e) {

            throw new IllegalArgumentException(
                    "Invalid position format: "
                            + value
                            + ". Expected format: (x,y)"
            );
        }
    }

    public static UUID findMoveDecisionId(
            GameStatusDTO status,
            Position targetPosition
    ) {

        return status.possibleDecisions()
                .stream()
                .filter(decision ->
                        targetPosition.equals(decision.landing())
                )
                .map(DecisionDTO::id)
                .findFirst()
                .orElseThrow();
    }

    public static UUID findMoveId(
            GameStatusDTO status,
            Position actorPosition,
            Position targetPosition
    ) {

        PieceDTO actor =
                find(status.board(), actorPosition);

        return findDecisionsFor(status, actor.id())
                .filter(decision ->
                        targetPosition.equals(decision.landing())
                )
                .map(DecisionDTO::id)
                .findFirst()
                .orElseThrow();
    }

    private static List<PreCaptureOptionDTO> findPreCaptureOptions(
            GameStatusDTO statusDTO
    ) {

        return statusDTO.possibleOptions()
                .values()
                .stream()
                .flatMap(Set::stream)
                .filter(PreCaptureOptionDTO.class::isInstance)
                .map(PreCaptureOptionDTO.class::cast)
                .toList();
    }

    public static UUID findCaptureDecisionId(
            GameStatusDTO statusDTO,
            PieceDTO actor,
            Position expectedLanding,
            Position... targetPositions
    ) {

        Set<PreCaptureOptionDTO> matchingOptions =
                findPreCaptureOptions(statusDTO)
                        .stream()
                        .filter(option -> {

                            Position targetPos =
                                    option.target().position();

                            for (Position expected : targetPositions) {

                                if (expected.equals(targetPos)) {
                                    return true;
                                }
                            }

                            return false;
                        })
                        .collect(Collectors.toSet());

        return findPreCaptureDecisionId(
                statusDTO,
                actor,
                matchingOptions,
                expectedLanding
        );
    }

    /**
     * Examples:
     * <ul>
     *     <li>BC4(0,0)</li>
     *     <li>WC5(2,1)</li>
     *     <li>BP91(4,4)</li>
     * </ul>
     */
    public static UUID findCaptureDecisionId(
            GameStatusDTO statusDTO,
            String actor,
            Position landing,
            String... targets
    ) {

        String actorDTOId =
                PieceRepresentationHelper.findId(
                        statusDTO,
                        actor
                );

        Set<String> expectedCapturedIds =
                java.util.Arrays.stream(targets)
                        .map(target ->
                                PieceRepresentationHelper.findId(
                                        statusDTO,
                                        target
                                )
                        )
                        .collect(Collectors.toSet());

        return findDecisionsFor(statusDTO, actorDTOId)
                .filter(decision ->
                        decision.capturedIdList() != null
                                && decision.capturedIdList()
                                .equals(expectedCapturedIds)
                                && Objects.equals(
                                decision.landing(),
                                landing
                        )
                )
                .map(DecisionDTO::id)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "No matching capture decision found for actor "
                                        + actor
                                        + " with targets "
                                        + java.util.Arrays.toString(targets)
                        )
                );
    }

    public static UUID findCaptureDecisionIdWithLanding(
            GameStatusDTO statusDTO,
            String actor,
            String landing,
            String... targets
    ) {

        return findCaptureDecisionId(
                statusDTO,
                actor,
                parsePosition(landing),
                targets
        );
    }

    public static UUID findCaptureDecisionId(
            GameStatusDTO statusDTO,
            String actor,
            String... targets
    ) {

        return findCaptureDecisionId(
                statusDTO,
                actor,
                null,
                targets
        );
    }

    public static UUID findPreCaptureDecisionId(
            GameStatusDTO statusDTO,
            PieceDTO pieceDTO,
            Set<PreCaptureOptionDTO> optionList,
            Position expectedLanding
    ) {
        Set<String> capturedIds =
                optionList.stream()
                        .map(option -> option.target().id())
                        .collect(Collectors.toSet());

        return findDecisionsFor(statusDTO, pieceDTO.id())
                .filter(decision ->
                        expectedLanding.equals(decision.landing())
                )
                .filter(decision ->
                        decision.capturedIdList() != null
                                && decision.capturedIdList()
                                .equals(capturedIds)
                )
                .map(DecisionDTO::id)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "No matching decision found"
                        )
                );
    }

    public static PieceDTO find(
            BoardDTO boardDTO,
            Position position
    ) {

        return boardDTO.pieces()
                .stream()
                .filter(p -> p.position().equals(position))
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "No PieceDTO found at " + position
                        )
                );
    }

    public static PieceDTO findComponent(
            BoardDTO boardDTO,
            Position position,
            int value
    ) {

        return find(boardDTO, position)
                .components()
                .stream()
                .filter(c -> c.value() == value)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "No Component found with value " + value
                        )
                );
    }

    /**
     * @return decision actor if it's a piece
     * or pyramid if actor is a pyramid component
     */
    public static PieceDTO findActor(
            GameStatusDTO statusDTO,
            DecisionDTO decision
    ) {

        String actorId =
                decision.actorId();

        Optional<PieceDTO> directPiece =
                statusDTO.board().pieces()
                        .stream()
                        .filter(p -> p.id().equals(actorId))
                        .findFirst();

        if (directPiece.isPresent()) {
            return directPiece.get();
        }

        return statusDTO.board().pieces()
                .stream()
                .filter(p -> p.shape() == PieceShape.PYRAMID)
                .filter(p ->
                        p.components()
                                .stream()
                                .anyMatch(c ->
                                        c.id().equals(actorId)
                                )
                )
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "No actor found for id : " + actorId
                        )
                );
    }

    public static UUID findReintroductionIdByDestination(
            GameStatusDTO statusDTO,
            String pieceRepresentation,
            Position expectedLanding
    ) {
        PieceDTO pieceDTO = PieceRepresentationHelper
                .findPieceOrComponent(statusDTO, pieceRepresentation);

        return findDecisionsFor(statusDTO, pieceDTO.id())
                .filter(decision ->
                        expectedLanding.equals(decision.landing())
                )
                .map(DecisionDTO::id)
                .findFirst()
                .orElseThrow(() ->
                        new RuntimeException(
                                "No reintroduction decision found for "
                                        + pieceRepresentation
                                        + " at "
                                        + expectedLanding
                        )
                );
    }

    public static Stream<DecisionDTO> findDecisionsFor(GameStatusDTO statusDTO, String actorId) {
        return findNonSkipDecision(statusDTO)
                .filter(d -> actorId.equals(d.actorId()));
    }

    public static Stream<DecisionDTO> findNonSkipDecision(GameStatusDTO statusDTO) {
        return statusDTO.possibleDecisions().stream()
                .filter(d -> !d.skip());
    }
}