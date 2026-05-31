package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.PieceRepresentationHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import java.util.Arrays;
import java.util.List;

public final class BoardAssertions extends NestedStatusAssertions {
  public BoardAssertions(GameStatusDTO actual, StatusDTOAssertion parent) {
    super(actual, parent);
  }

  public BoardAssertions hasPiece(String pieceRepresentation) {
    boolean found =
        actual.board().pieces().stream()
            .map(PieceRepresentationHelper::toRepresentation)
            .anyMatch(pieceRepresentation::equals);

    if (!found) {
      throw new AssertionError(StatusAssertionMessages.pieceNotFound(pieceRepresentation));
    }

    return this;
  }

  public BoardAssertions hasPyramidComposedBy(PlayerColorDTO color, String... expectedComponents) {

    PieceDTO pyramid = PieceRepresentationHelper.findPyramidFor(actual, color);

    List<String> actualComponents =
        pyramid.components().stream()
            .map(PieceRepresentationHelper::toShortRepresentation)
            .sorted()
            .toList();

    List<String> expectedComponentsList = Arrays.stream(expectedComponents).sorted().toList();

    if (!actualComponents.equals(expectedComponentsList)) {
      throw new AssertionError(
          StatusAssertionMessages.pyramidCompositionMismatch(
              color, expectedComponentsList, actualComponents));
    }

    return this;
  }

  public BoardAssertions hasPyramidValue(PlayerColorDTO color, int expectedValue) {
    PieceDTO pyramid = PieceRepresentationHelper.findPyramidFor(actual, color);
    int actualValue = pyramid.value();
    if (actualValue != expectedValue) {
      throw new AssertionError(
          StatusAssertionMessages.incorrectPyramidValue(color, expectedValue, actualValue));
    }

    return this;
  }
}
