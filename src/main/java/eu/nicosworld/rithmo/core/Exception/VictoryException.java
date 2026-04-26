package eu.nicosworld.rithmo.core.Exception;

import eu.nicosworld.rithmo.engine.model.Player;

public class VictoryException extends Exception {
    public VictoryException(Player player) {
        super(player.getColor() + " is winner");
    }
}
