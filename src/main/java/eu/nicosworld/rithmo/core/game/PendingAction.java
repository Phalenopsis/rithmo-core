package eu.nicosworld.rithmo.core.game;

import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;
import eu.nicosworld.rithmo.core.turn.action.TurnAction;

import java.util.UUID;

public record PendingAction(
        UUID id,
        UUID gameId,
        TurnAction actionToExecute,
        PlayerOptionDTO dto // Le contrat d'affichage associé
) {}
