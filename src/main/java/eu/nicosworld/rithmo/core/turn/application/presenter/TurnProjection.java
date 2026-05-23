package eu.nicosworld.rithmo.core.turn.application.presenter;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;
import eu.nicosworld.rithmo.core.turn.application.projection.ExecutableDecision;

import java.util.List;

public record TurnProjection(
        PieceDTO piece,
        List<PlayerOptionDTO> options,
        List<ExecutableDecision> executableDecisions
) {}
