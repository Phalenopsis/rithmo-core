package eu.nicosworld.rithmo.core.turn.action;

import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

/**
 * Represents a capture performed BEFORE the main movement phase.
 *
 * @param actions The list of engine-level capture primitives to execute.
 * @param landing The resulting position of the capturing piece after this action.
 */
public record PreCaptureAction(
        List<CaptureAction> actions,
        Position landing
) implements TurnAction {
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
