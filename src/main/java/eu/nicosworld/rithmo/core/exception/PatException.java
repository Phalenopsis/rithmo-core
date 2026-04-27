package eu.nicosworld.rithmo.core.Exception;

import eu.nicosworld.rithmo.engine.model.Player;

public class PatException extends Exception {
    public PatException(Player player) {
        super(player.getColor() + " is pat");
    }
}
