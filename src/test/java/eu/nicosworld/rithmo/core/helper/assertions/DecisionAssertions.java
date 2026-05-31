package eu.nicosworld.rithmo.core.helper.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.helper.FindDecisionHelper;
import eu.nicosworld.rithmo.core.helper.PieceRepresentationHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.engine.model.Position;
import java.util.*;
import java.util.stream.Collectors;

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
        long captureDecisionCounted = FindDecisionHelper
                .findNonSkipDecision(actual)
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

    public DecisionAssertions hasOnlyDecisionsFor(String actorRepresentation) {
        PieceDTO actor = PieceRepresentationHelper.findPieceOrComponent(
                actual,
                actorRepresentation
        );

        assertThat(actual.possibleDecisions())
                .isNotEmpty()
                .allMatch(d -> actor.id().equals(d.actorId()));

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

        long decisionCount = FindDecisionHelper
                .findDecisionsFor(actual, piece.id())
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
                .map(support::normalize)
                .collect(Collectors.toSet());

        Set<String> expectedLandings = Arrays.stream(expectedLandingPositions)
                .map(support::normalize)
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

    public DecisionAssertions canCaptureInOneDecision(
            String... pieceRepresentations
    ) {

        List<String> expected = Arrays.stream(pieceRepresentations)
                .sorted()
                .toList();

        boolean found = actual.possibleDecisions()
                .stream()
                .filter(d ->
                        d.capturedIdList() != null
                                && !d.capturedIdList().isEmpty()
                )
                .anyMatch(decision -> {

                    List<String> captured = decision.capturedIdList()
                            .stream()
                            .map(this.support::findPieceRepresentationById)
                            .filter(Objects::nonNull)
                            .sorted()
                            .toList();

                    return captured.equals(expected);
                });

        if (!found) {
            throw new AssertionError(StatusAssertionMessages.missingCaptureDecision(expected,
                    support.formatPossibleDecisionsForError()));
        }

        return this;
    }

    public DecisionAssertions hasCaptureCiblesFor(
            String actorRepresentation,
            String... expectedTargets
    ) {
        String actorId = PieceRepresentationHelper.findId(actual, actorRepresentation);

        Set<String> actualTargets = FindDecisionHelper
                .findDecisionsFor(actual, actorId)
                .flatMap(d -> d.capturedIdList().stream())
                .collect(Collectors.toSet());

        Set<String> expected = Arrays.stream(expectedTargets)
                .map(rep -> PieceRepresentationHelper.findId(actual, rep))
                .collect(Collectors.toSet());

        if (!actualTargets.equals(expected)) {
            throw new AssertionError(StatusAssertionMessages.incorrectCaptureTargets(
                    actorRepresentation,
                    Arrays.toString(expectedTargets),
                    actualTargets
            ));
        }

        return this;
    }

    public DecisionAssertions cannotCaptureWith(
            String actorRepresentation,
            String targetRepresentation
    ) {

        String actorId = PieceRepresentationHelper.findId(actual, actorRepresentation);
        String targetId = PieceRepresentationHelper.findId(actual, targetRepresentation);

        boolean exists = FindDecisionHelper
                .findDecisionsFor(actual, actorId)
                .anyMatch(d ->
                        d.capturedIdList() != null
                                && d.capturedIdList().contains(targetId)
                );

        if (exists) {
            throw new AssertionError(StatusAssertionMessages.forbiddenCapture(actorRepresentation, targetRepresentation));
        }

        return this;
    }

    public DecisionAssertions hasCaptureSourcesFor(
            String targetRepresentation,
            String... expectedActors
    ) {

        String targetId = PieceRepresentationHelper.findId(actual, targetRepresentation);

        Set<String> actualActors = FindDecisionHelper
                .findNonSkipDecision(actual)
                .filter(d -> d.capturedIdList() != null
                        && d.capturedIdList().contains(targetId))
                .map(DecisionDTO::actorId)
                .collect(Collectors.toSet());

        Set<String> expected = Arrays.stream(expectedActors)
                .map(rep -> PieceRepresentationHelper.findId(actual, rep))
                .collect(Collectors.toSet());

        if (!actualActors.equals(expected)) {
            throw new AssertionError(StatusAssertionMessages.incorrectCaptureSources(
                    targetRepresentation,
                    Arrays.toString(expectedActors),
                    actualActors
            ));
        }

        return this;
    }
}
