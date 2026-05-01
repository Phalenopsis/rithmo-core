package eu.nicosworld.rithmo.core.turn;

import eu.nicosworld.rithmo.core.turn.option.TurnOption;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Position;
import java.util.List;

/**
 * An immutable snapshot of the current turn's progression.
 * <p>
 * This class acts as a container for the engine's {@link GameState} and tracking
 * data required by the {@link TurnProcessor} to handle multi-phase turns.
 *
 * @param state       The current underlying game state from the engine.
 * @param phase       The current active {@link TurnPhase}.
 * @param options     The list of legal actions available to the player in this state.
 * @param hasCaptured A flag indicating if a capture has already been performed during this turn.
 * @param isIrregular A flag indicating if the last move followed "irregular" movement rules.
 * @param actorPos The position of the piece currently being moved or used for capture.
 */
public record TurnState(
        GameState state,
        TurnPhase phase,
        List<TurnOption> options,
        boolean hasCaptured,
        boolean isIrregular,
        Position actorPos
) {

    /**
     * Creates a new TurnState for a specific phase and set of options.
     */
    public static TurnState of(GameState state, TurnPhase phase, List<TurnOption> options) {
        return new TurnState(state, phase, options, false, false, null);
    }

    /**
     * Creates a new TurnState for a specific phase without predefined options.
     */
    public static TurnState of(GameState state, TurnPhase phase) {
        return new TurnState(state, phase, null, false, false, null);
    }

    /**
     * Creates a new TurnState while tracking the position of a specific piece.
     * This is typically used during capture sequences (e.g., Assault).
     *
     * @param attackerPos The current position of the active piece.
     */
    public static TurnState withPosition(GameState state, TurnPhase phase, List<TurnOption> options, Position attackerPos) {
        return new TurnState(state, phase, options, false, false, attackerPos);
    }
}