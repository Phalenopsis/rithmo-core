package eu.nicosworld.rithmo.core.game.dto.victory;

import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import java.util.List;
import java.util.Set;

public record VictoryDTO(
    PlayerColorDTO winner,
    Set<VictoryConditionDTO> conditions,
    List<VictoryJustificationDTO> justifications) {}
