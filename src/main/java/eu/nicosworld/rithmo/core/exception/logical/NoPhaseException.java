package eu.nicosworld.rithmo.core.exception.logical;

import eu.nicosworld.rithmo.core.turn.TurnPhase;

public class NoPhaseException extends RuntimeException {
  public NoPhaseException(TurnPhase phase) {
    super("Phase " + phase + "shouldn't be exposed.");
  }
}
