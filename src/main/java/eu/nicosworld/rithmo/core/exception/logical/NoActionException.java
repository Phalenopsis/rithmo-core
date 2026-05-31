package eu.nicosworld.rithmo.core.exception.logical;

import eu.nicosworld.rithmo.core.turn.TurnPhase;

public class NoActionException extends RuntimeException {
  public NoActionException(TurnPhase phase) {
    super("Phase " + phase + " doesn't have action");
  }

  public NoActionException() {
    super("There's no action in applier");
  }
}
