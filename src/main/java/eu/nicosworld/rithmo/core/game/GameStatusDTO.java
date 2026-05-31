package eu.nicosworld.rithmo.core.game;

import eu.nicosworld.rithmo.core.exception.logical.NoPhaseException;
import eu.nicosworld.rithmo.core.game.dto.board.BoardDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PlayerAssetsDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PlayerOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PhaseDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public record GameStatusDTO(
    UUID gameId,
    BoardDTO board,
    PlayerColorDTO currentPlayer,
    PhaseDTO phase,
    Map<PieceDTO, Set<PlayerOptionDTO>> possibleOptions,
    Set<DecisionDTO> possibleDecisions,
    Map<PlayerColorDTO, PlayerAssetsDTO> assets) {
  public static GameStatusDTO from(
      Game game,
      Map<PieceDTO, Set<PlayerOptionDTO>> possibleOptions,
      Set<DecisionDTO> possibleDecisions)
      throws NoPhaseException {
    var state = game.getCurrentState().state();

    Map<PlayerColorDTO, PlayerAssetsDTO> assetsDTO =
        state.assets().entrySet().stream()
            .collect(
                Collectors.toMap(
                    e -> PlayerColorDTO.mapColor(e.getKey()),
                    e -> PlayerAssetsDTO.from(e.getValue())));

    return new GameStatusDTO(
        game.getId(),
        BoardDTO.mapFrom(game.getCurrentState().state().board()),
        PlayerColorDTO.mapColor(game.getCurrentState().state().currentPlayer().getColor()),
        PhaseDTO.mapPhase(game.getCurrentState().phase()),
        possibleOptions,
        possibleDecisions,
        assetsDTO);
  }
}
