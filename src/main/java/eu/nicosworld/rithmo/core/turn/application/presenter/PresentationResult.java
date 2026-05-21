package eu.nicosworld.rithmo.core.turn.application.presenter;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;
import eu.nicosworld.rithmo.core.turn.action.TurnAction;

import java.util.List;

public record PresentationResult(
        PieceDTO piece,
        List<PlayerOptionDTO> options,
        List<TurnAction> actions,
        List<DecisionDTO> decisions
) {}
