package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.dto.option.justification.AmbushJustificationDTO;
import eu.nicosworld.rithmo.core.game.dto.option.justification.AmbushOperatorDTO;
import eu.nicosworld.rithmo.core.game.dto.option.justification.CaptureJustificationDTO;
import eu.nicosworld.rithmo.core.game.dto.option.justification.EncounterJustificationDTO;

public class JustificationStringMapper {
  public static String mapJustification(CaptureJustificationDTO justification) {
    return switch (justification) {
      case EncounterJustificationDTO e -> mapEncounter(e);
      case AmbushJustificationDTO a -> mapAmbush(a);
      default -> "in progress";
    };
  }

  private static String mapEncounter(EncounterJustificationDTO justification) {
    return justification.matchedValue() + " = " + justification.matchedValue();
  }

  private static String mapAmbush(AmbushJustificationDTO justification) {
    String actor;
    String ally;

    if (justification.operandsReversed()) {
      actor = String.valueOf(justification.supporterValue());
      ally = String.valueOf(justification.actorValue());
    } else {
      actor = String.valueOf(justification.actorValue());
      ally = String.valueOf(justification.supporterValue());
    }

    return actor
        + mapAmbushOperator(justification.operator())
        + ally
        + " = "
        + justification.targetValue();
  }

  private static String mapAmbushOperator(AmbushOperatorDTO operator) {
    return switch (operator) {
      case DIVIDE -> " / ";
      case SUBTRACT -> " - ";
      case MULTIPLY -> " * ";
      case ADD -> " + ";
    };
  }
}
