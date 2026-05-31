package eu.nicosworld.rithmo.core.exception;

import eu.nicosworld.rithmo.engine.model.Player;

public class PatException extends Exception {
  public PatException(Player player) {
    super(player.getColor() + " is pat");
  }
}
