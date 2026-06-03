package eu.nicosworld.rithmo.core.game.dto.option.justification;

import eu.nicosworld.rithmo.engine.capture.justification.*;
import java.util.Objects;

public sealed interface CaptureJustificationDTO
    permits AmbushJustificationDTO,
        AssaultJustificationDTO,
        EncounterJustificationDTO,
        PowerJustificationDTO {
  static CaptureJustificationDTO from(CaptureJustification justification) {
    Objects.requireNonNull(justification);

    return switch (justification) {
      case AmbushJustification a -> AmbushJustificationDTO.from(a);
      case AssaultJustification a -> AssaultJustificationDTO.from(a);
      case EncounterJustification a -> EncounterJustificationDTO.from(a);
      case PowerJustification a -> PowerJustificationDTO.from(a);
    };
  }
}
