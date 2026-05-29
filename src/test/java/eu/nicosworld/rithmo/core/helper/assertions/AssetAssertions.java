package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.helper.PieceRepresentationHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;

import java.util.Arrays;
import java.util.List;

public final class AssetAssertions extends NestedStatusAssertions {
    public AssetAssertions(GameStatusDTO actual, StatusDTOAssertion parent) {
        super(actual, parent);
    }

    public AssetAssertions capturedContains(
            String... expectedRepresentations
    ) {
        List<String> actual = this.actual.assets()
                .values()
                .stream()
                .flatMap(a -> a.captured().stream())
                .map(PieceRepresentationHelper::toShortRepresentation)
                .toList();

        List<String> missing = Arrays.stream(expectedRepresentations)
                .filter(expected -> !actual.contains(expected))
                .toList();

        if (!missing.isEmpty()) {
            throw new AssertionError(StatusAssertionMessages.missingInCaptures(missing, actual));
        }

        return this;
    }

    public AssetAssertions hasInReserve(
            String... expectedRepresentations
    ) {
        List<String> actual = this.actual.assets()
                .values()
                .stream()
                .flatMap(a -> a.reserve().stream())
                .map(PieceRepresentationHelper::toShortRepresentation)
                .toList();

        List<String> missing = Arrays.stream(expectedRepresentations)
                .filter(expected -> !actual.contains(expected))
                .toList();

        if (!missing.isEmpty()) {
            throw new AssertionError(StatusAssertionMessages.missingInReserve(missing, actual));
        }

        return this;
    }
}
