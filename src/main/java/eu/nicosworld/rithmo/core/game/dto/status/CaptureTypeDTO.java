package eu.nicosworld.rithmo.core.game.dto.status;

import eu.nicosworld.rithmo.engine.capture.CaptureType;

public enum CaptureTypeDTO {
    ENCOUNTER,     // RENCONTRE
    AMBUSH,        // EMBUCHE
    ASSAULT,       // ASSAUT
    POWER,         // PUISSANCE
    PROGRESSION,   // PROGRESSION
    IMPRISONMENT;   // EMPRISONNEMENT

    public static CaptureTypeDTO from(CaptureType type) {
        return switch (type) {
            case ENCOUNTER -> CaptureTypeDTO.ENCOUNTER;
            case AMBUSH -> CaptureTypeDTO.AMBUSH;
            case ASSAULT -> CaptureTypeDTO.ASSAULT;
            case POWER -> CaptureTypeDTO.POWER;
            case PROGRESSION -> CaptureTypeDTO.PROGRESSION;
            case IMPRISONMENT -> CaptureTypeDTO.IMPRISONMENT;
        };
    }
}
