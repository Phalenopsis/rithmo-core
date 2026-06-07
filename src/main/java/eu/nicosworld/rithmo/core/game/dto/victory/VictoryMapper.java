package eu.nicosworld.rithmo.core.game.dto.victory;

import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.game.victory.VictoryCondition;
import eu.nicosworld.rithmo.engine.model.victory.BodyVictory;
import eu.nicosworld.rithmo.engine.model.victory.GoodsVictory;
import eu.nicosworld.rithmo.engine.model.victory.LawsuitVictory;
import eu.nicosworld.rithmo.engine.model.victory.Victory;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class VictoryMapper {
  public static VictoryDTO toDTO(Set<VictoryCondition> conditions, List<Victory> victories) {
    PlayerColorDTO colorDTO =
        PlayerColorDTO.mapColor(victories.stream().findFirst().orElseThrow().winner().getColor());

    Set<VictoryConditionDTO> satisfiedConditions =
        conditions.stream().map(VictoryConditionDTO::from).collect(Collectors.toSet());

    List<VictoryJustificationDTO> justifications =
        victories.stream().map(VictoryMapper::map).toList();
    return new VictoryDTO(colorDTO, satisfiedConditions, justifications);
  }

  private static VictoryJustificationDTO map(Victory victory) {
    return switch (victory) {
      case BodyVictory v -> new BodyVictoryDTO(v.capturedCount(), v.requiredCount());

      case GoodsVictory v -> new GoodsVictoryDTO(v.capturedValue(), v.requiredValue());

      case LawsuitVictory v ->
          new LawsuitVictoryDTO(v.capturedDigitCount(), v.requiredDigitCount());
    };
  }
}
