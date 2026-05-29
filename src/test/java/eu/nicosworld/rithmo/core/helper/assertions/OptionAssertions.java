package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.option.ReintroductionOptionDTO;
import eu.nicosworld.rithmo.core.helper.PieceRepresentationHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public final class OptionAssertions extends NestedStatusAssertions {
    public OptionAssertions(GameStatusDTO actual, StatusDTOAssertion parent) {
        super(actual, parent);
    }

    public OptionAssertions hasOptionCount(int n) {
        int optionCount = this.actual.possibleOptions()
                .values()
                .stream()
                .mapToInt(Set::size)
                .sum();

        assertThat(optionCount)
                .isEqualTo(n);

        return this;
    }

    public OptionAssertions hasOptionCountFor(
            String pieceRepresentation,
            int n
    ) {
        PieceDTO piece = PieceRepresentationHelper.findPieceOrComponent(
                actual,
                pieceRepresentation
        );

        long optionCount = actual.possibleOptions()
                .getOrDefault(piece, Set.of())
                .size();

        if (optionCount != n) {
            throw new AssertionError(StatusAssertionMessages.incorrectOptionCount(
                    pieceRepresentation,
                    n,
                    optionCount
            ));
        }

        return this;
    }

    public OptionAssertions hasNoReintroductionOptions() {

        boolean exists = actual.possibleOptions()
                .values()
                .stream()
                .flatMap(Set::stream)
                .anyMatch(ReintroductionOptionDTO.class::isInstance);

        if (exists) {
            throw new AssertionError(StatusAssertionMessages.unexpectedReintroductionOptions());
        }

        return this;
    }
}
