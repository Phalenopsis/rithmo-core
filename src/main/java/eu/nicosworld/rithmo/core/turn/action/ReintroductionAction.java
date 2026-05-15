package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.ReintroductionOption;
import eu.nicosworld.rithmo.engine.reintroduction.Reintroduction;

public record ReintroductionAction(
        Reintroduction reintroduction
) implements TurnAction {
    public static ReintroductionAction from(ReintroductionOption option) {
        return new ReintroductionAction(option.reintroduction());
    }
}
