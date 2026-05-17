package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.BoardDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceShape;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PreCaptureOptionDTO;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.*;

public class FindDecisionHelper {

    public static UUID findAnyNonSkipDecision(GameStatusDTO statusDTO) {
        return statusDTO.possibleDecisions()
                .stream()
                .filter(decision -> !decision.skip())
                .map(DecisionDTO::id)
                .findFirst()
                .orElse(null);
    }

    public static UUID findSkipDecision(GameStatusDTO statusDTO) {
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
     *
     * <p>
     * Examples:
     * <pre>
     * findMoveDecisionId(status, "BP91(0,0)", "(2,0)");
     * findMoveDecisionId(status, "WC4(3,1)", "(3,2)");
     * </pre>
     *
     * <p>
     * Only decisions without captures are considered.
     *
     * @param statusDTO current game status
     * @param actor actor representation
     * @param landingString landing position formatted like "(x,y)"
     * @return matching decision UUID
     */
    public static UUID findMoveDecisionId(
            GameStatusDTO statusDTO,
            String actor,
            String landingString
    ) {

        String actorId =
                findPieceOrComponentIdByRepresentation(
                        statusDTO,
                        actor
                );

        Position landing =
                parsePosition(landingString);

        return statusDTO.possibleDecisions()
                .stream()

                // not skip
                .filter(decision -> !decision.skip())

                // same actor
                .filter(decision ->
                        actorId.equals(decision.actorId())
                )

                // no capture
                .filter(decision ->
                        decision.capturedIdList() == null
                                || decision.capturedIdList().isEmpty()
                )

                // same landing
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
        PieceDTO actor = find(status.board(), actorPosition);

        return status.possibleDecisions()
                .stream()
                .filter(decisionDTO ->
                        decisionDTO.actorId().equals(actor.id()))
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

        Set<PreCaptureOptionDTO> matchingOptions = findPreCaptureOptions(statusDTO)
                .stream()
                .filter(option -> {
                    Position targetPos = option.target().position();

                    for (Position expected : targetPositions) {
                        if (expected.equals(targetPos)) {
                            return true;
                        }
                    }

                    return false;
                })
                .collect(java.util.stream.Collectors.toSet());

        return findPreCaptureDecisionId(
                statusDTO,
                actor,
                matchingOptions,
                expectedLanding
        );
    }

    /**
     * actor and targets are String formatting like :
     * <ul>
     *     <li>Color Shape Value</li>
     *     <li>Position</li>
     * </ul>
     * For example :
     * <ul>
     *     <li>a Black Circle with a value of 4, in Position (0,0) should be formatted as "BC4(0,0)"</li>
     *     <li>a component should have same format than a single piece</li>
     *     <li>BP91(4,4) represent a full pyramid as a target</li>
     *     <li>BC16(4,4) could represent a component of the previous pyramid or a single piece </li>
     * </ul>
     *
     *
     * @param statusDTO current statusDTO
     * @param actor String representation of actor who will do capture
     * @param targets String representation of pieces or components who will be captured
     * @return Decision UUID
     */
    public static UUID findCaptureDecisionId(
            GameStatusDTO statusDTO,
            String actor,
            Position landing,
            String ...targets
    ) {

        String actorDTOId = findPieceOrComponentIdByRepresentation(statusDTO, actor);

        Set<String> expectedCapturedIds =
                java.util.Arrays.stream(targets)
                        .map(target ->
                                findPieceOrComponentIdByRepresentation(
                                        statusDTO,
                                        target
                                )
                        )
                        .collect(java.util.stream.Collectors.toSet());

        return statusDTO.possibleDecisions()
                .stream()
                .filter(decision -> !decision.skip())
                .filter(decision ->
                        actorDTOId.equals(decision.actorId())
                )
                .filter(decision ->
                        decision.capturedIdList() != null
                                && decision.capturedIdList().equals(expectedCapturedIds)
                                && Objects.equals(decision.landing(), landing)
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

    public static UUID findCaptureDecisionId(
            GameStatusDTO statusDTO,
            String actor,
            String ...targets
    ) {
        return findCaptureDecisionId(
                statusDTO,
                actor,
                null,
                targets
    );
    }

    private static String findPieceOrComponentIdByRepresentation(
            GameStatusDTO statusDTO,
            String representation
    ) {

        for (PieceDTO piece : statusDTO.board().pieces()) {

            // Full piece match
            if (matchesRepresentation(piece, representation)) {
                return piece.id();
            }

            // Pyramid components
            if (piece.shape().equals(PieceShape.PYRAMID)) {

                for (PieceDTO component : piece.components()) {

                    if (matchesRepresentation(component, representation)) {
                        return component.id();
                    }
                }
            }
        }

        throw new RuntimeException(
                "No piece or component found for representation: "
                        + representation
        );
    }

    private static boolean matchesRepresentation(
            PieceDTO piece,
            String representation
    ) {

        String expected =
                TestDebugger.getStringRepresentation(piece)
                        + formatPosition(piece.position());

        return expected.equals(representation);
    }

    private static String formatPosition(Position position) {

        return "("
                + position.getX()
                + ","
                + position.getY()
                + ")";
    }

    public static UUID findPreCaptureDecisionId(
            GameStatusDTO statusDTO,
            PieceDTO pieceDTO,
            Set<PreCaptureOptionDTO> optionList,
            Position expectedLanding
    ) {

        String actorId = pieceDTO.id();

        Set<String> capturedIds = optionList.stream()
                .map(option -> option.target().id())
                .collect(java.util.stream.Collectors.toSet());

        return statusDTO.possibleDecisions()
                .stream()
                .filter(decision -> !decision.skip())
                .filter(decision -> actorId.equals(decision.actorId()))
                .filter(decision -> expectedLanding.equals(decision.landing()))
                .filter(decision ->
                        decision.capturedIdList() != null
                                && decision.capturedIdList().equals(capturedIds)
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
                        new RuntimeException("No PieceDTO found")
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
                        new RuntimeException("No Component found")
                );
    }

    /**
     *
     * @param statusDTO GameStatusDTO
     * @param decision DecisionDTO
     * @return decision actor if it's a piece or main piece if it's a pyramid
     */
    public static PieceDTO findActor(GameStatusDTO statusDTO, DecisionDTO decision) {
        String actorId = decision.actorId();
        try {
            return statusDTO.board().pieces()
                    .stream()
                    .filter(p->p.id().equals(actorId))
                    .findFirst()
                    .orElseThrow();
        } catch (NoSuchElementException e) {
            //ignored
        }
        // in this case, Actor should be a pyramid
        return statusDTO.board().pieces()
                .stream()
                .filter(p->p.shape().equals(PieceShape.PYRAMID))
                .filter(p -> p.components().stream().anyMatch(c-> c.id().equals(actorId)))
                .findFirst()
                .orElseThrow();
    }

    public static UUID findReintroductionIdByDestination(
            GameStatusDTO statusDTO,
            String pieceRepresentation,
            Position expectedLanding
    ) {
        return statusDTO.possibleDecisions()
                .stream()
                .filter(decision -> !decision.skip())
                .filter(decision -> expectedLanding.equals(decision.landing()))
                .filter(decision -> {

                    String actorId = decision.actorId();

                    return statusDTO.possibleOptions().keySet().stream()
                            .filter(piece -> piece.id().equals(actorId))
                            .anyMatch(piece ->
                                    TestDebugger.getStringRepresentation(piece)
                                            .equals(pieceRepresentation)
                            );
                })
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
}