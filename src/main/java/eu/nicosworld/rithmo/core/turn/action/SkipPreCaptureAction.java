package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.SkipPreCaptureOption;

/**
 * Internal execution action representing the explicit decision to skip the pre-capture phase.
 *
 * <p>A {@code SkipPreCaptureAction} is derived from a {@link SkipPreCaptureOption}
 * produced by the resolver layer when pre-capture actions are available.</p>
 *
 * <p>This action represents a deliberate player choice to not perform any
 * pre-movement capture, allowing the turn to proceed to the movement phase.</p>
 *
 * <p>It is an internal engine-level control action and is not exposed beyond
 * the execution pipeline.</p>
 */
public record SkipPreCaptureAction() implements TurnAction {
    /**
     * Creates a {@link SkipPreCaptureAction} from a {@link SkipPreCaptureOption}.
     *
     * <p>This method maps a resolver-provided skip option into an executable
     * control-flow action indicating that the player declines pre-capture actions.</p>
     *
     * @param option
     *         The skip option selected from the resolver output.
     *
     * @return an executable skip action for the pre-capture phase.
     */
    public static SkipPreCaptureAction from(SkipPreCaptureOption option) {
        return new SkipPreCaptureAction();
    }
}
