package eu.nicosworld.rithmo.core.game.dto.option.justification;

import eu.nicosworld.rithmo.engine.capture.justification.AssaultJustification;
import eu.nicosworld.rithmo.engine.capture.justification.AssaultOperator;

public record AssaultJustificationDTO(
    int distance, AssaultOperatorDTO operator, int actorValue, int targetValue)
    implements CaptureJustificationDTO {
  public static AssaultJustificationDTO from(AssaultJustification justification) {
    return new AssaultJustificationDTO(
        justification.distance(),
        mapOperator(justification.operator()),
        justification.actorValue(),
        justification.targetValue());
  }

  private static AssaultOperatorDTO mapOperator(AssaultOperator operator) {
    return switch (operator) {
      case MULTIPLY -> AssaultOperatorDTO.MULTIPLY;
      case DIVIDE -> AssaultOperatorDTO.DIVIDE;
      case null -> throw new IllegalArgumentException();
    };
  }
}
