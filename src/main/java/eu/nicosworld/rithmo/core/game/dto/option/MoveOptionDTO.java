package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.game.dto.status.MoveTypeDTO;
import eu.nicosworld.rithmo.core.turn.option.MoveOption;
import eu.nicosworld.rithmo.engine.model.Position;

public record MoveOptionDTO(
        Position to,
        MoveTypeDTO typeDTO
) implements PlayerOptionDTO {
    public static MoveOptionDTO from(MoveOption option) {
        return new MoveOptionDTO(
                option.move().to(),
                MoveTypeDTO.from(option.move().nature())
        );
    }
}
