package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.move.Move;

/**
 * A proposal for a movement on the board.
 *
 * @param move The engine-level move details.
 */
public record MoveOption(
        Piece actor,
        Move move
) implements TurnOption {}