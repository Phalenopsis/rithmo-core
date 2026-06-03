package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.dto.option.justification.*;

public class JustificationStringMapper {
  public static String mapJustification(CaptureJustificationDTO justification) {
    return switch (justification) {
      case EncounterJustificationDTO e -> mapEncounter(e);
      case AmbushJustificationDTO a -> mapAmbush(a);
      case AssaultJustificationDTO a -> mapAssault(a);
      case PowerJustificationDTO p -> mapPower(p);
    };
  }

  private static String mapPower(PowerJustificationDTO p) {
    return p.actorValue() + mapPowerRelation(p.relation()) + p.degree() + " = " + p.targetValue();
  }

  private static String mapAssault(AssaultJustificationDTO a) {
    return a.actorValue()
        + mapAssaultOperator(a.operator())
        + a.distance()
        + " = "
        + a.targetValue();
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

  private static String mapAssaultOperator(AssaultOperatorDTO operator) {
    return switch (operator) {
      case MULTIPLY -> " * ";
      case DIVIDE -> " / ";
    };
  }

  /**
   * Converts capture justification DTOs into deterministic developer-oriented textual
   * representations used by the assertion DSL.
   *
   * <p>These strings are intended for test assertions and debugging only.
   */
  private static String mapPowerRelation(PowerRelationDTO operator) {
    return switch (operator) {
      case POWER -> " exp ";
      case ROOT -> " root ";
    };
  }
}
