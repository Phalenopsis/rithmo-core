package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.model.Piece;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;
import eu.nicosworld.rithmo.engine.move.Move;

/**
 * Represents a single executable movement possibility for a piece.
 *
 * <p>A {@code MoveOption} is an atomic turn option produced by the engine during movement
 * resolution. It directly describes one valid move that can later be transformed into a
 * player-facing decision and executable action.
 *
 * <p>The acting piece context is embedded directly in the option in order to avoid external board
 * lookups during application-layer projection and UI assembly.
 *
 * @param actor The moving piece together with its current board position.
 * @param move The engine-level movement description.
 */
public record MoveOption(PieceAtPosition actor, Move move) implements TurnOption {
  public static MoveOption from(Piece actor, Move move) {
    return new MoveOption(new PieceAtPosition(actor, move.from()), move);
  }
}
