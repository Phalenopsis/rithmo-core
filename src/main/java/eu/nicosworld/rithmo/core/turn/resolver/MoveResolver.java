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

    public List<Move> resolveMove(GameState state) {
        return movementEngine.getAllMoves(state);
    }

    public List<Move> resolveMove(GameState state, PieceAtPosition pap) {
        return movementEngine.generateMoves(state, pap);
    }
}
