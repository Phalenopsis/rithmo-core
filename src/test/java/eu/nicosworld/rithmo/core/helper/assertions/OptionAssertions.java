package eu.nicosworld.rithmo.core.helper.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.game.dto.status.CaptureTypeDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.JustificationStringMapper;
import eu.nicosworld.rithmo.core.helper.PieceRepresentationHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class OptionAssertions extends NestedStatusAssertions {
  private List<CaptureOptionDTO> optionList = new ArrayList<>();

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

  private <T> T checkOption(
      PieceDTO actor, String targetId, Class<T> optionClass, Predicate<T> matcher) {
    return actual.possibleOptions().get(actor).stream()
        .filter(optionClass::isInstance)
        .map(optionClass::cast)
        .filter(matcher)
        .findFirst()
        .orElseThrow(
            () -> new AssertionError(StatusAssertionMessages.optionNotFound(actor.id(), targetId)));
  }

  private OptionAssertions canCaptureWithBy(
      String actorRepresentation, CaptureTypeDTO captureTypeDTO, String... targetRepresentations) {
    PieceDTO actor = PieceRepresentationHelper.findPieceOrComponent(actual, actorRepresentation);
    optionList.clear();

    for (String targetRep : targetRepresentations) {
      String targetId = PieceRepresentationHelper.findId(actual, targetRep);
      CaptureOptionDTO option =
          checkOption(
              actor,
              targetId,
              CaptureOptionDTO.class,
              o -> o.target().id().equals(targetId) && o.type().equals(captureTypeDTO));
      optionList.add(option);
    }
    return this;
  }

  public OptionAssertions because(String... justifications) {
    if (optionList.isEmpty())
      throw new AssertionError(
          "Justification can only be tested with a previous capture option assertion");
    List<String> expected = Stream.of(justifications).sorted().toList();
    List<String> actualJustifications =
        optionList.stream()
            .map(CaptureOptionDTO::justification)
            .map(JustificationStringMapper::mapJustification)
            .sorted()
            .toList();
    assertThat(actualJustifications).isEqualTo(expected);
    optionList.clear();
    return this;
  }

  public OptionAssertions canCaptureWithByEncounter(
      String actorRepresentation, String... targetRepresentations) {
    return canCaptureWithBy(actorRepresentation, CaptureTypeDTO.ENCOUNTER, targetRepresentations);
  }

  public OptionAssertions canCaptureWithByAssault(
      String actorRepresentation, String... targetRepresentations) {
    return canCaptureWithBy(actorRepresentation, CaptureTypeDTO.ASSAULT, targetRepresentations);
  }

  public OptionAssertions canCaptureWithByPower(
      String actorRepresentation, String... targetRepresentations) {
    return canCaptureWithBy(actorRepresentation, CaptureTypeDTO.POWER, targetRepresentations);
  }

  public OptionAssertions canCaptureWithByAmbush(
      String actorRepresentation, String... targetRepresentations) {
    return canCaptureWithBy(actorRepresentation, CaptureTypeDTO.AMBUSH, targetRepresentations);
  }
}
