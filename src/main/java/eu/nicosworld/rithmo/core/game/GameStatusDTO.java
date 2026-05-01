package eu.nicosworld.rithmo.core.game;

import eu.nicosworld.rithmo.core.exception.logical.NoPhaseException;
import eu.nicosworld.rithmo.core.game.dto.board.BoardDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PhaseDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;

import java.util.List;
import java.util.UUID;

public record GameStatusDTO(
        UUID gameId,
        BoardDTO board,
        PlayerColorDTO currentPlayer,
        PhaseDTO phase,
        List<PlayerOptionDTO> possibleOptions
) {
    public static GameStatusDTO from(Game game, List<PlayerOptionDTO> possibleOptions) throws NoPhaseException {
        return new GameStatusDTO(
                game.getId(),
                BoardDTO.mapFrom(game.getCurrentState().state().board()),
                PlayerColorDTO.mapColor(game.getCurrentState().state().currentPlayer().getColor()),
                PhaseDTO.mapPhase(game.getCurrentState().phase()),
                possibleOptions);
    }
}
