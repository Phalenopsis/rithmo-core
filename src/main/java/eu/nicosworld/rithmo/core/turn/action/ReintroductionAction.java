package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.ReintroductionOption;
import eu.nicosworld.rithmo.engine.reintroduction.Reintroduction;

/**
 * Internal execution action representing the reintroduction of a previously captured piece
 * onto the game board.
 *
 * <p>A {@code ReintroductionAction} is derived from a {@link ReintroductionOption}
 * produced by the resolver layer.</p>
 *
 * <p>This action is atomic and represents a single engine-level operation:
 * placing a piece back onto the board at a valid position defined by the engine.</p>
 *
 * <p>It is strictly internal to the execution layer and is not exposed to the UI.
 * It is produced only after a corresponding validated decision selection.</p>
 *
 * @param reintroduction
 *         The engine-level reintroduction definition (piece + target position).
 */
public record ReintroductionAction(
        Reintroduction reintroduction
) implements TurnAction {
    /**
     * Creates a {@link ReintroductionAction} from a {@link ReintroductionOption}.
     *
     * <p>This method performs a direct transformation from a resolver-provided option
     * into an executable engine action without expansion or branching.</p>
     *
     * @param option
     *         The reintroduction option selected from the resolver output.
     *
     * @return an executable reintroduction action.
     */
    public static ReintroductionAction from(ReintroductionOption option) {
        return new ReintroductionAction(option.reintroduction());
    }
}
