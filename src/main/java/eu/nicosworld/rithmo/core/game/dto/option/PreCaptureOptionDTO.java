package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.status.CaptureTypeDTO;
import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;
import java.util.List;

/**
 * UI-facing representation of an atomic pre-movement capture option.
 * <p>
 * A {@code PreCaptureOptionDTO} is derived from a {@link PreCaptureOption} and represents
 * a single capture possibility available before the main movement phase.
 * <p>
 * Each instance corresponds to one atomic {@link CaptureAction} that the player may select.
 * Multiple pre-capture options can be selected together to form a composite decision.
 *
 * <p>
 * Important: each pre-capture option includes a {@code landing} value, which represents
 * one possible resulting position of the capturing piece for this specific atomic capture.
 * The final landing used in execution may depend on the combination of selected options.
 *
 * <p>
 * Each option contains:
 * <ul>
 *     <li>the target piece being captured</li>
 *     <li>the capture type (semantic classification of the rule applied)</li>
 *     <li>the supporting pieces involved in the capture (if any)</li>
 *     <li>a possible landing position associated with this atomic capture</li>
 * </ul>
 *
 * <p>
 * This DTO is strictly a presentation model. It is not executable and must be
 * transformed into a {@link eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO}
 * via user selection, where multiple options may contribute to a single decision.
 */
public record PreCaptureOptionDTO(
        PieceDTO target,
        CaptureTypeDTO type,
        List<PieceDTO> ally,
        Position landing
) implements PlayerOptionDTO {
    public static List<PreCaptureOptionDTO> from(PreCaptureOption option) {
        return option.captures()
                .stream()
                .map(PreCaptureOptionDTO::from)
                .toList();
    }

    public static PreCaptureOptionDTO from(CaptureAction action) {
        return new PreCaptureOptionDTO(
                PieceDTO.from(
                        action.target().specificComponent(),
                        action.targetPosition()),
                CaptureTypeDTO.from(action.type()),
                action.supporters().stream()
                        .map(PieceDTO::from)
                        .toList(),
                action.targetPosition()
        );
    }
}
