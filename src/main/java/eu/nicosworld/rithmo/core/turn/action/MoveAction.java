package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.MoveOption;
import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.move.Move;

/**
 * Represents a movement action on the board.
 *
 * @param move The movement details (from/to) defined by the engine.
 */
public record MoveAction(Piece actor, Move move) implements TurnAction {
    public static MoveAction from(MoveOption option) {
        return new MoveAction(option.actor().piece(), option.move());
    }
}
