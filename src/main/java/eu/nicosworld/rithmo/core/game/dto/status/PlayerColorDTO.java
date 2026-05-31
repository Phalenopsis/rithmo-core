package eu.nicosworld.rithmo.core.game.dto.status;

import eu.nicosworld.rithmo.engine.model.PlayerColor;

public enum PlayerColorDTO {
  BLACK,
  WHITE;

  public static PlayerColorDTO mapColor(PlayerColor color) {
    return switch (color) {
      case PlayerColor.BLACK -> PlayerColorDTO.BLACK;
      case PlayerColor.WHITE -> PlayerColorDTO.WHITE;
    };
  }
}
