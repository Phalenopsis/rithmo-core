package eu.nicosworld.rithmo.core.helper.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.option.CaptureOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PreCaptureOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.ReintroductionOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.SkipOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.status.CaptureTypeDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.PieceRepresentationHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public final class OptionAssertions extends NestedStatusAssertions {
  public OptionAssertions(GameStatusDTO actual, StatusDTOAssertion parent) {
    super(actual, parent);
  }

  public OptionAssertions hasOptionCount(int n) {
    int optionCount = this.actual.possibleOptions().values().stream().mapToInt(Set::size).sum();

    assertThat(optionCount).isEqualTo(n);

    return this;
  }

  public OptionAssertions hasOptionCountFor(String pieceRepresentation, int n) {
    PieceDTO piece = PieceRepresentationHelper.findPieceOrComponent(actual, pieceRepresentation);

    long optionCount = actual.possibleOptions().getOrDefault(piece, Set.of()).size();

    if (optionCount != n) {
      throw new AssertionError(
          StatusAssertionMessages.incorrectOptionCount(pieceRepresentation, n, optionCount));
    }

    return this;
  }

  public OptionAssertions hasNoReintroductionOptions() {

    boolean exists =
        actual.possibleOptions().values().stream()
            .flatMap(Set::stream)
            .anyMatch(ReintroductionOptionDTO.class::isInstance);

    if (exists) {
      throw new AssertionError(StatusAssertionMessages.unexpectedReintroductionOptions());
    }

    return this;
  }

  public OptionAssertions hasReintroductionOptionsForActivePlayer() {
    PlayerColorDTO color = actual.currentPlayer();

    boolean exists =
        actual.possibleOptions().values().stream()
            .flatMap(Set::stream)
            .anyMatch(
                option ->
                    option instanceof ReintroductionOptionDTO ro
                        && ro.pieceDTO().owner().equals(color));

    if (!exists) {
      throw new AssertionError(StatusAssertionMessages.missingReintroductionOption(color));
    }

    return this;
  }

  public OptionAssertions allReintroductionOptionsComeFromReserve() {
    Map<String, Set<String>> reserveByPlayer =
        actual.assets().entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> e.getKey().name(),
                    e ->
                        e.getValue().reserve().stream()
                            .map(PieceDTO::id)
                            .collect(Collectors.toSet())));

    actual.possibleOptions().values().stream()
        .flatMap(Set::stream)
        .filter(ReintroductionOptionDTO.class::isInstance)
        .map(ReintroductionOptionDTO.class::cast)
        .forEach(
            option -> {
              String owner = option.pieceDTO().owner().name();
              String pieceId = option.pieceDTO().id();
              Set<String> reserveIds = reserveByPlayer.get(owner);

              if (reserveIds == null || !reserveIds.contains(pieceId)) {
                throw new AssertionError(StatusAssertionMessages.invalidReintroduction(option));
              }
            });

    return this;
  }

  public OptionAssertions hasSkipOption() {
    assertThat(actual.possibleOptions().get(PieceDTO.GLOBAL_OPTION))
        .anyMatch(o -> o instanceof SkipOptionDTO);

    return this;
  }

  private <T> void checkOption(
      PieceDTO actor, String targetId, Class<T> optionClass, Predicate<T> matcher) {
    boolean exists =
        actual.possibleOptions().get(actor).stream()
            .filter(optionClass::isInstance)
            .map(optionClass::cast)
            .anyMatch(matcher);

    if (!exists) {
      throw new AssertionError(StatusAssertionMessages.optionNotFound(actor.id(), targetId));
    }
  }

  private OptionAssertions canPreCaptureWithBy(
      String actorRepresentation, CaptureTypeDTO captureTypeDTO, String... targetRepresentations) {
    PieceDTO actor = PieceRepresentationHelper.findPieceOrComponent(actual, actorRepresentation);

    for (String targetRep : targetRepresentations) {
      String targetId = PieceRepresentationHelper.findId(actual, targetRep);
      checkOption(
          actor,
          targetId,
          PreCaptureOptionDTO.class,
          o -> o.target().id().equals(targetId) && o.type().equals(captureTypeDTO));
    }
    return this;
  }

  private OptionAssertions canPostCaptureWithBy(
      String actorRepresentation, CaptureTypeDTO captureTypeDTO, String... targetRepresentations) {
    PieceDTO actor = PieceRepresentationHelper.findPieceOrComponent(actual, actorRepresentation);

    for (String targetRep : targetRepresentations) {
      String targetId = PieceRepresentationHelper.findId(actual, targetRep);
      checkOption(
          actor,
          targetId,
          CaptureOptionDTO.class,
          o -> o.target().id().equals(targetId) && o.type().equals(captureTypeDTO));
    }
    return this;
  }

  public OptionAssertions canPostCaptureWithByEncounter(
      String actorRepresentation, String... targetRepresentations) {
    return canPostCaptureWithBy(
        actorRepresentation, CaptureTypeDTO.ENCOUNTER, targetRepresentations);
  }

  public OptionAssertions canPostCaptureWithByAssault(
      String actorRepresentation, String... targetRepresentations) {
    return canPostCaptureWithBy(actorRepresentation, CaptureTypeDTO.ASSAULT, targetRepresentations);
  }

  public OptionAssertions canPostCaptureWithByPower(
      String actorRepresentation, String... targetRepresentations) {
    return canPostCaptureWithBy(actorRepresentation, CaptureTypeDTO.POWER, targetRepresentations);
  }

  public OptionAssertions canPostCaptureWithByAmbush(
      String actorRepresentation, String... targetRepresentations) {
    return canPostCaptureWithBy(actorRepresentation, CaptureTypeDTO.AMBUSH, targetRepresentations);
  }

  public OptionAssertions canPreCaptureWithByEncounter(
      String actorRepresentation, String... targetRepresentations) {
    return canPreCaptureWithBy(
        actorRepresentation, CaptureTypeDTO.ENCOUNTER, targetRepresentations);
  }

  public OptionAssertions canPreCaptureWithByAssault(
      String actorRepresentation, String... targetRepresentations) {
    return canPreCaptureWithBy(actorRepresentation, CaptureTypeDTO.ASSAULT, targetRepresentations);
  }

  public OptionAssertions canPreCaptureWithByPower(
      String actorRepresentation, String... targetRepresentations) {
    return canPreCaptureWithBy(actorRepresentation, CaptureTypeDTO.POWER, targetRepresentations);
  }

  public OptionAssertions canPreCaptureWithByAmbush(
      String actorRepresentation, String... targetRepresentations) {
    return canPreCaptureWithBy(actorRepresentation, CaptureTypeDTO.AMBUSH, targetRepresentations);
  }
}
