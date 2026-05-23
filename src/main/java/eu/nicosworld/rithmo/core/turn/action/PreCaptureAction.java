package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

/**
 * Internal execution action representing a capture performed before the main movement phase.
 *
 * <p>A {@code PreCaptureAction} is derived from a {@link PreCaptureOption} produced
 * by the resolver layer. Unlike other actions, a single option may generate multiple
 * executable actions depending on the possible landing positions.</p>
 *
 * <p>This action is composite and represents:
 * <ul>
 *     <li>a set of engine-level capture primitives</li>
 *     <li>a specific landing position for the capturing piece</li>
 * </ul>
 * </p>
 *
 * <p>If multiple landing positions are available in the option, each one produces
 * a distinct {@code PreCaptureAction}.</p>
 *
 * <p>This action is strictly internal to the engine and is not exposed to the UI layer.
 * It is produced only after a corresponding validated decision selection.</p>
 *
 * @param actions
 *         Ordered list of engine-level capture primitives to execute.
 *
 * @param landing
 *         Final position of the capturing piece after execution.
 */
public record PreCaptureAction(
        List<CaptureAction> actions,
        Position landing
) implements TurnAction {
    /**
     * Expands a {@link PreCaptureOption} into one or more executable {@link PreCaptureAction}s.
     *
     * <p>This method performs a structural expansion rather than a simple transformation:
     * a single option may represent multiple valid landing configurations.</p>
     *
     * <p>Each possible landing position produces a distinct executable action,
     * preserving the same capture sequence but differing in final placement.</p>
     *
     * <p>If no landing positions are defined, a single action with a null landing
     * is produced.</p>
     *
     * @param option
     *         The resolver-provided pre-capture option.
     *
     * @return a list of executable pre-capture actions derived from the option.
     */
    public static List<PreCaptureAction> from(PreCaptureOption option) {
        if(option.possibleLandings().isEmpty()) {
            return List.of(new PreCaptureAction(
                    option.captures(),
                    null
            ));
        }

        return option.possibleLandings().stream()
                .map(landing ->
                        new PreCaptureAction(
                                List.copyOf(option.captures()),
                                landing
                        )
                )
                .toList();
    }
}
