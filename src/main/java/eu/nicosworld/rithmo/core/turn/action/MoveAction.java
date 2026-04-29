package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.MoveOption;
import eu.nicosworld.rithmo.engine.move.Move;

public record MoveAction(
        Move move
) implements TurnAction {
    public static MoveAction from(MoveOption option) {
        return new MoveAction(option.move());
    }
}
