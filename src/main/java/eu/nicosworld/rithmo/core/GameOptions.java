package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.game.CaptureRuleOption;
import eu.nicosworld.rithmo.core.game.VictoryRuleOption;

import java.util.Map;
import java.util.Set;

public record GameOptions(
    Set<CaptureRuleOption> captureRules,
    Map<VictoryRuleOption, Integer> victoryRules
) { }
