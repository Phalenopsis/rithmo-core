package eu.nicosworld.rithmo.core.game;

import eu.nicosworld.rithmo.core.turn.action.TurnAction;

import java.util.UUID;

public record PendingAction(
        UUID id,
        UUID gameId,
        TurnAction actionToExecute
) {}
