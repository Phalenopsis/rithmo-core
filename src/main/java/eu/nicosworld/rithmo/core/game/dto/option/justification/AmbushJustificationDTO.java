package eu.nicosworld.rithmo.core.game.dto.option.justification;

import eu.nicosworld.rithmo.engine.capture.justification.AmbushJustification;
import eu.nicosworld.rithmo.engine.capture.justification.AmbushOperator;

public record AmbushJustificationDTO(
    int actorValue,
    AmbushOperatorDTO operator,
    boolean operandsReversed,
    int supporterValue,
    int targetValue)
    implements CaptureJustificationDTO {
  public static AmbushJustificationDTO from(AmbushJustification justification) {
    MappedOperator mappedOperator = mapOperator(justification.operator());

    return new AmbushJustificationDTO(
        justification.actorValue(),
        mappedOperator.operator(),
        mappedOperator.isReversed(),
        justification.supporterValue(),
        justification.targetValue());
  }

  private static MappedOperator mapOperator(AmbushOperator operator) {
    return switch (operator) {
      case ADD -> new MappedOperator(AmbushOperatorDTO.ADD, false);
      case MULTIPLY -> new MappedOperator(AmbushOperatorDTO.MULTIPLY, false);
      case SUBTRACT -> new MappedOperator(AmbushOperatorDTO.SUBTRACT, false);
      case SUBTRACT_INV -> new MappedOperator(AmbushOperatorDTO.SUBTRACT, true);
      case DIVIDE -> new MappedOperator(AmbushOperatorDTO.DIVIDE, false);
      case DIVIDE_INV -> new MappedOperator(AmbushOperatorDTO.DIVIDE, true);
      case null -> throw new IllegalArgumentException();
    };
  }

  private record MappedOperator(AmbushOperatorDTO operator, boolean isReversed) {}
}
