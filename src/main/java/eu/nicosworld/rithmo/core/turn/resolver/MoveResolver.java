package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.PieceAtPosition;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.move.MovementEngine;
import java.util.List;

public class MoveResolver {
  private final MovementEngine movementEngine;

  public MoveResolver(MovementEngine movementEngine) {
    this.movementEngine = movementEngine;
  }

  /**
   * Resolves all possible movement options for the current player.
   *
   * @param state The current game state.
   * @return A list of {@link Move }s.
   */
  public List<Move> resolveMove(GameState state) {
    return movementEngine.getAllMoves(state);
  }

  /**
   * Resolves movements restricted to a specific piece. Useful for multi-step movements or forced
   * actions.
   *
   * @param state The current game state.
   * @param pap The specific piece (Piece At Position) allowed to move.
   * @return A list of {@link Move }s for the specified piece.
   */
  public List<Move> resolveMove(GameState state, PieceAtPosition pap) {
    return movementEngine.generateFreePathRegularMoves(state, pap);
  }
}
