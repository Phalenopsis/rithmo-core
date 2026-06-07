package eu.nicosworld.rithmo.core.game.dto.victory;

import eu.nicosworld.rithmo.core.game.victory.VictoryCondition;

public enum VictoryConditionDTO {
  BODY,
  GOODS,
  LAWSUIT,
  BODY_AND_GOODS,
  BODY_AND_LAWSUIT,
  GOODS_AND_LAWSUIT,
  BODY_AND_GOODS_AND_LAWSUIT;

  public static VictoryConditionDTO from(VictoryCondition condition) {
    return switch (condition) {
      case BODY -> BODY;
      case GOODS -> GOODS;
      case LAWSUIT -> LAWSUIT;
      case BODY_AND_GOODS -> BODY_AND_GOODS;
      case BODY_AND_LAWSUIT -> BODY_AND_LAWSUIT;
      case GOODS_AND_LAWSUIT -> GOODS_AND_LAWSUIT;
      case BODY_AND_GOODS_AND_LAWSUIT -> BODY_AND_GOODS_AND_LAWSUIT;
    };
  }
}
