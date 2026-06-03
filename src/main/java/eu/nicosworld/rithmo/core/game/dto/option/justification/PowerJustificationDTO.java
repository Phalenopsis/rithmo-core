package eu.nicosworld.rithmo.core.game.dto.option.justification;

import eu.nicosworld.rithmo.engine.capture.justification.PowerJustification;
import eu.nicosworld.rithmo.engine.capture.justification.PowerRelation;

public record PowerJustificationDTO(
    int actorValue, PowerRelationDTO relation, int degree, int targetValue)
    implements CaptureJustificationDTO {
  public static PowerJustificationDTO from(PowerJustification justification) {
    return new PowerJustificationDTO(
        justification.actorValue(),
        mapRelation(justification.relation()),
        justification.degree(),
        justification.targetValue());
  }

  private static PowerRelationDTO mapRelation(PowerRelation relation) {
    return switch (relation) {
      case POWER -> PowerRelationDTO.POWER;
      case ROOT -> PowerRelationDTO.ROOT;
      case null -> throw new IllegalArgumentException();
    };
  }
}
