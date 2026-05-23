package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.status.MoveTypeDTO;
import eu.nicosworld.rithmo.core.turn.option.MoveOption;
import eu.nicosworld.rithmo.engine.model.Position;

/**
 * UI-facing representation of a legal movement option available to the player.
 * <p>
 * A {@code MoveOptionDTO} is derived from a {@link MoveOption}, which is a
 * low-level engine-provided option produced by the {@code PhaseResolver}.
 * <p>
 * It is used only for presentation purposes (highlighting a piece and a destination),
 * and is not directly executable. To execute it, the UI must select the associated
 * {@link eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO}.
 *
 * <p>
 * This DTO is part of the read-model exposed to the UI layer and must remain
 * free of any engine-side behavior or mutation logic.
 *
 * @param actor the piece performing the move, including its current position
 * @param to the destination position of the move
 * @param typeDTO the semantic type of the move (normal, capture-like behavior, etc.)
 */
public record MoveOptionDTO(
        PieceDTO actor,
        Position to,
        MoveTypeDTO typeDTO
) implements PlayerOptionDTO {
    public static MoveOptionDTO from(MoveOption option) {
        return new MoveOptionDTO(
                PieceDTO.from(option.actor().piece(), option.move().from()),
                option.move().to(),
                MoveTypeDTO.from(option.move().nature())
        );
    }
}
