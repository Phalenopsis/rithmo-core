package eu.nicosworld.rithmo.core.turn.application.projection;

import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.turn.action.TurnAction;

public record ExecutableDecision(
        DecisionDTO decision,
        TurnAction action
) {
}