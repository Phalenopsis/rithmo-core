package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.move.Move;

public record MoveOption(
        Move move
) implements TurnOption {}
