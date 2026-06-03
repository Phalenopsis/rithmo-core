package eu.nicosworld.rithmo.core.game.dto.option.justification;

import eu.nicosworld.rithmo.engine.capture.justification.EncounterJustification;

public record EncounterJustificationDTO(int matchedValue) implements CaptureJustificationDTO {
  public static EncounterJustificationDTO from(EncounterJustification justification) {
    return new EncounterJustificationDTO(justification.matchedValue());
  }
}
