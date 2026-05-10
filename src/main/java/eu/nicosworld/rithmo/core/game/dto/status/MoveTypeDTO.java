package eu.nicosworld.rithmo.core.game.dto.status;

import eu.nicosworld.rithmo.engine.move.MoveNature;

public enum MoveTypeDTO {
    REGULAR, IRREGULAR;

    public static MoveTypeDTO from(MoveNature type) {
        return switch (type) {
            case REGULAR -> MoveTypeDTO.REGULAR;
            case IRREGULAR -> MoveTypeDTO.IRREGULAR;
        };
    }
}
