package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.game.CaptureRuleOption;
import eu.nicosworld.rithmo.core.game.victory.VictoryCondition;
import eu.nicosworld.rithmo.engine.victory.VictoryType;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record GameOptions(
    Set<CaptureRuleOption> captureRules,
    Map<VictoryType, Integer> victoryRules,
    Set<VictoryCondition> victoryConditions) {

  public GameOptions(Set<CaptureRuleOption> captureRules, Map<VictoryType, Integer> victoryRules) {
    this(
        captureRules,
        victoryRules,
        victoryRules.keySet().stream().map(VictoryCondition::fromRule).collect(Collectors.toSet()));
  }
}
