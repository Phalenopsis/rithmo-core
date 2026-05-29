package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.helper.PieceRepresentationHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;

public final class BoardAssertions extends NestedStatusAssertions {
    public BoardAssertions(GameStatusDTO actual, StatusDTOAssertion parent) {
        super(actual, parent);
    }

    public BoardAssertions hasPiece(
            String pieceRepresentation
    ) {
        boolean found = actual.board().pieces()
                .stream()
                .map(PieceRepresentationHelper::toRepresentation)
                .anyMatch(pieceRepresentation::equals);

        if (!found) {
            throw new AssertionError(StatusAssertionMessages.pieceNotFound(pieceRepresentation));
        }

        return this;
    }
}
