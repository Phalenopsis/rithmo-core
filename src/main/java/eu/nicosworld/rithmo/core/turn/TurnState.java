package eu.nicosworld.rithmo.core.turn;

import eu.nicosworld.rithmo.core.turn.option.TurnOption;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

public record TurnState(
        GameState state,
        TurnPhase phase,
        List<TurnOption> options,
        boolean hasCaptured,
        boolean wasMoveIrregular,
        Position attackerPos
) {


    public static TurnState of(
            GameState state,
            TurnPhase phase,
            List<TurnOption> options
    ) {
        return new TurnState(state, phase, options, false, false, null);
    }

    public static TurnState of(
            GameState state,
            TurnPhase phase
    ) {
        return TurnState.of(state, phase, List.of());
    }

    public static TurnState withIrregularMove(
            GameState state,
            TurnPhase phase,
            List<TurnOption> options
    ) {
        return new TurnState(state, phase, options, false, true, null);
    }

    public static TurnState withPosition(
            GameState state,
            TurnPhase phase,
            List<TurnOption> options,
            Position position
    ) {
        return new TurnState(state, phase, options, true, false, position);
    }
}