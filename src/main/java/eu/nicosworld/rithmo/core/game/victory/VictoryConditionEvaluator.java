package eu.nicosworld.rithmo.core.game.victory;

import eu.nicosworld.rithmo.engine.model.victory.BodyVictory;
import eu.nicosworld.rithmo.engine.model.victory.GoodsVictory;
import eu.nicosworld.rithmo.engine.model.victory.LawsuitVictory;
import eu.nicosworld.rithmo.engine.model.victory.Victory;
import eu.nicosworld.rithmo.engine.victory.VictoryType;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class VictoryConditionEvaluator {
  private final Set<VictoryCondition> conditions;

  public VictoryConditionEvaluator(Set<VictoryCondition> conditions) {
    this.conditions = conditions;
  }

  public boolean isSatisfied(List<Victory> victories) {
    Set<VictoryType> mappedVictories = mapVictories(victories);

    return conditions.stream().anyMatch(c -> c.matches(mappedVictories));
  }

  private Set<VictoryType> mapVictories(List<Victory> victories) {
    return victories.stream().map(this::mapVictory).collect(Collectors.toSet());
  }

  private VictoryType mapVictory(Victory victory) {
    return switch (victory) {
      case GoodsVictory ignored -> VictoryType.GOODS;
      case BodyVictory ignored -> VictoryType.BODY;
      case LawsuitVictory ignored -> VictoryType.LAWSUIT;
    };
  }
}
