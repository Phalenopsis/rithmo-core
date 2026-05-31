package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.SkipPostCaptureOption;

/**
 * Internal execution action representing the explicit отказ (skip) of the post-capture phase.
 *
 * <p>A {@code SkipPostCaptureAction} is derived from a {@link SkipPostCaptureOption} produced by
 * the resolver layer when post-capture actions are available.
 *
 * <p>This action represents a deliberate player choice to not perform any post-movement capture,
 * and therefore to proceed to the end of the turn.
 *
 * <p>It is an internal engine-level control action and is not exposed beyond the execution
 * pipeline.
 */
public record SkipPostCaptureAction() implements TurnAction {
  /**
   * Creates a {@link SkipPostCaptureAction} from a {@link SkipPostCaptureOption}.
   *
   * <p>This method maps a resolver-provided skip option into an executable control-flow action
   * indicating that the player declines post-capture actions.
   *
   * @param option The skip option selected from the resolver output.
   * @return an executable skip action for the post-capture phase.
   */
  public static SkipPostCaptureAction from(SkipPostCaptureOption option) {
    return new SkipPostCaptureAction();
  }
}
