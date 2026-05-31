package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import java.util.List;

/**
 * Internal execution action representing a capture sequence performed
 * after the main movement phase.
 *
 * <p>A {@code PostCaptureAction} is derived from a {@link PostCaptureOption},
 * which is produced by the resolver layer when post-move captures are available.</p>
 *
 * <p>This action is engine-level and contains a sequence of atomic
 * {@link CaptureAction}s that will be applied in order by the game engine.</p>
 *
 * <p>It is a composite execution action: it does not represent a single move,
 * but a batch of capture operations linked to the previously executed movement.</p>
 *
 * <p>This action is not exposed to the UI layer and is only produced after
 * player validation of a corresponding decision.</p>
 *
 * @param actions
 *         Ordered list of engine-level capture primitives to execute.
 */
public record PostCaptureAction(
        List<CaptureAction> actions
) implements TurnAction {
    /**
     * Creates a {@link PostCaptureAction} from a {@link PostCaptureOption}.
     *
     * <p>This method converts a resolver-provided post-move capture option
     * into an executable engine action.</p>
     *
     * <p>The resulting action preserves the full list of capture primitives
     * required to update the game state.</p>
     *
     * @param option
     *         The post-capture option selected from the resolver output.
     *
     * @return an executable post-capture action.
     */
    public static PostCaptureAction from(PostCaptureOption option) {
        return new PostCaptureAction(List.copyOf(option.captures()));
    }
}
