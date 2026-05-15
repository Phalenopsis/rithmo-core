package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.reintroduction.Reintroduction;

public record ReintroductionOption(
        Reintroduction reintroduction
) implements TurnOption {}
