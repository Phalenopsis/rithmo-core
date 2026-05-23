package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.status.MoveTypeDTO;
import eu.nicosworld.rithmo.core.turn.option.MoveOption;
import eu.nicosworld.rithmo.engine.model.Position;

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
