package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.helper.PieceRepresentationHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public final class DecisionAssertions extends NestedStatusAssertions {
    public DecisionAssertions(GameStatusDTO actual, StatusDTOAssertion parent) {
        super(actual, parent);
    }

    public DecisionAssertions hasOnlyMoveDecisions() {
        assertThat(actual.possibleDecisions())
                .allMatch(d ->
                        !d.skip()
                        && d.capturedIdList().isEmpty()
                        && d.landing() != null
                );

        return this;
    }

    public DecisionAssertions hasSkipDecision() {
        assertThat(actual.possibleDecisions())
                .anyMatch(DecisionDTO::skip);

        return this;
    }

    public DecisionAssertions hasCaptureDecisionCount(int n) {
        long captureDecisionCounted = this.actual.possibleDecisions()
                .stream()
                .filter(d -> !d.skip())
                .filter(d ->
                        d.capturedIdList() != null
                                && !d.capturedIdList().isEmpty()
                )
                .count();

        assertThat(captureDecisionCounted)
                .isEqualTo(n);

        return this;
    }

    public DecisionAssertions hasDecisionCount(int n) {
        assertThat(actual.possibleDecisions())
                .hasSize(n);

        return this;
    }

    public DecisionAssertions hasDecisionCountFor(
            String pieceRepresentation,
            int n
    ) {
        PieceDTO piece = PieceRepresentationHelper.findPieceOrComponent(
                actual,
                pieceRepresentation
        );

        long decisionCount = actual.possibleDecisions()
                .stream()
                .filter(d -> !d.skip())
                .filter(d -> piece.id().equals(d.actorId()))
                .count();

        if (decisionCount != n) {
            throw new AssertionError(StatusAssertionMessages.incorrectDecisionCount(
                    pieceRepresentation,
                    n,
                    decisionCount
            ));
        }

        return this;
    }

    public DecisionAssertions hasStrictMoveDecisionTo(
            String... expectedLandingPositions
    ) {
        return hasMoveDecisionTo(true, expectedLandingPositions);
    }

    public DecisionAssertions hasMoveDecisionTo(
            String... expectedLandingPositions
    ) {
        return hasMoveDecisionTo(false, expectedLandingPositions);
    }

    private DecisionAssertions hasMoveDecisionTo(
            boolean strict,
            String... expectedLandingPositions
    ) {

        Set<String> actualLandings = actual.possibleDecisions()
                .stream()
                .map(DecisionDTO::landing)
                .filter(Objects::nonNull)
                .map(Position::toString)
                .map(this::normalize)
                .collect(Collectors.toSet());

        Set<String> expectedLandings = Arrays.stream(expectedLandingPositions)
                .map(this::normalize)
                .collect(Collectors.toSet());

        Set<String> missing = new HashSet<>(expectedLandings);
        missing.removeAll(actualLandings);

        if (!missing.isEmpty()) {
            throw new AssertionError(StatusAssertionMessages.missingMoveDestinations(
                    missing,
                    actualLandings
            ));
        }

        if (strict) {
            Set<String> extras = new HashSet<>(actualLandings);
            extras.removeAll(expectedLandings);

            if (!extras.isEmpty()) {
                throw new AssertionError(StatusAssertionMessages.unexpectedMoveDestinations(extras));
            }
        }

        return this;
    }
}
