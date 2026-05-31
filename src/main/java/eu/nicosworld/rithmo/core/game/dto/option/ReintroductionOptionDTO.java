package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.turn.option.ReintroductionOption;
import eu.nicosworld.rithmo.engine.model.Position;

/**
 * UI-facing representation of a reintroduction option available to the player.
 *
 * <p>A {@code ReintroductionOptionDTO} is derived from a {@link ReintroductionOption}, which is a
 * low-level engine-provided option produced by the {@code PhaseResolver}.
 *
 * <p>It represents a single atomic reintroduction choice: placing a previously captured piece back
 * onto the board at a valid landing position.
 *
 * <p>This DTO is strictly a presentation model and is not executable. To apply this option, the UI
 * must select the associated {@link eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO}.
 *
 * <p>The embedded {@link PieceDTO} represents the piece being reintroduced, and the landing
 * position indicates the target square where it will be placed.
 *
 * @param pieceDTO the piece being reintroduced (as a UI representation)
 * @param landing the target position where the piece will be placed on the board
 */
public record ReintroductionOptionDTO(PieceDTO pieceDTO, Position landing)
    implements PlayerOptionDTO {
  public static ReintroductionOptionDTO from(ReintroductionOption option) {
    return new ReintroductionOptionDTO(
        PieceDTO.from(option.reintroduction().piece(), null), option.reintroduction().position());
  }
}
