package eu.nicosworld.rithmo.core.helper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.game.dto.victory.*;

public class VictoryAssertion {
  private final VictoryDTO victoryDTO;

  public VictoryAssertion(VictoryDTO dto) {
    victoryDTO = dto;
  }

  public static VictoryAssertion from(VictoryExecutable executable) {
    VictoryException exception = assertThrows(VictoryException.class, executable::execute);

    return new VictoryAssertion(exception.getVictoryDto());
  }

  public VictoryAssertion hasWinner(PlayerColorDTO colorDTO) {
    assertEquals(colorDTO, victoryDTO.winner());
    return this;
  }

  public VictoryAssertion isByBody() {
    body();
    return this;
  }

  private BodyVictoryDTO body() {
    return victoryDTO.justifications().stream()
        .filter(j -> j instanceof BodyVictoryDTO)
        .map(j -> (BodyVictoryDTO) j)
        .findFirst()
        .orElseThrow(() -> new AssertionError("No body Victory found."));
  }

  public VictoryAssertion hasCapturedCount(int expected) {
    assertThat(body().actual()).isEqualTo(expected);
    return this;
  }

  public VictoryAssertion hasRequiredCount(int expected) {
    assertThat(body().required()).isEqualTo(expected);
    return this;
  }

  private GoodsVictoryDTO goods() {
    return victoryDTO.justifications().stream()
        .filter(j -> j instanceof GoodsVictoryDTO)
        .map(j -> (GoodsVictoryDTO) j)
        .findFirst()
        .orElseThrow(() -> new AssertionError("No goods Victory found."));
  }

  public VictoryAssertion isByGoods() {
    goods();
    return this;
  }

  public VictoryAssertion hasCapturedValue(int expected) {
    assertThat(goods().actual()).isEqualTo(expected);
    return this;
  }

  public VictoryAssertion hasRequiredValue(int expected) {
    assertThat(goods().required()).isEqualTo(expected);
    return this;
  }

  public VictoryAssertion hasCondition(VictoryConditionDTO conditionDTO) {
    assertThat(victoryDTO.conditions()).contains(conditionDTO);

    return this;
  }

  public VictoryAssertion isByLawsuit() {
    lawsuit();

    return this;
  }

  private LawsuitVictoryDTO lawsuit() {
    return victoryDTO.justifications().stream()
        .filter(j -> j instanceof LawsuitVictoryDTO)
        .map(j -> (LawsuitVictoryDTO) j)
        .findFirst()
        .orElseThrow(() -> new AssertionError("No goods Victory found."));
  }

  public VictoryAssertion hasCapturedDigitCount(int expected) {
    assertThat(lawsuit().actual()).isEqualTo(expected);
    return this;
  }

  public VictoryAssertion hasCapturedDigitRequired(int expected) {
    assertThat(lawsuit().required()).isEqualTo(expected);
    return this;
  }
}
