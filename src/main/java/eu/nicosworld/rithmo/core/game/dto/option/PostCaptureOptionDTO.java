package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.option.justification.CaptureJustificationDTO;
import eu.nicosworld.rithmo.core.game.dto.status.CaptureTypeDTO;
import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import java.util.List;

/**
 * UI-facing representation of a single capture possibility within a post-capture option.
 *
 * <p>A {@code PostCaptureOptionDTO} is derived from a {@link PostCaptureOption} and represents one
 * atomic capture action that the player may select as part of a larger decision.
 *
 * <p>Unlike other {@link PlayerOptionDTO}s, capture options are typically grouped: a single
 * post-capture phase may expose multiple capture possibilities, each represented by its own {@code
 * PostCaptureOptionDTO}.
 *
 * <p>Each capture option contains:
 *
 * <ul>
 *   <li>the target piece being captured
 *   <li>the type of capture (rule-based semantic classification)
 *   <li>the supporting pieces involved in the capture (if any)
 * </ul>
 *
 * <p>This DTO is strictly a presentation model. It is not executable and must be converted into a
 * {@link eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO} via the associated selection
 * mechanism.
 */
public record PostCaptureOptionDTO(
    PieceDTO target,
    CaptureTypeDTO type,
    CaptureJustificationDTO justification,
    List<PieceDTO> ally)
    implements CaptureOptionDTO {
  public static List<PostCaptureOptionDTO> from(PostCaptureOption option) {
    return option.captures().stream().map(PostCaptureOptionDTO::from).toList();
  }

  public static PostCaptureOptionDTO from(CaptureAction action) {
    return new PostCaptureOptionDTO(
        PieceDTO.from(action.target().specificComponent(), action.targetPosition()),
        CaptureTypeDTO.from(action.type()),
        CaptureJustificationDTO.from(action.justification()),
        action.supporters().stream().map(PieceDTO::from).toList());
  }
}
