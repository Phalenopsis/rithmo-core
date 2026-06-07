package eu.nicosworld.rithmo.core.exception;

import eu.nicosworld.rithmo.core.game.dto.victory.VictoryDTO;

public class VictoryException extends Exception {
  private final VictoryDTO dto;

  public VictoryException(VictoryDTO dto) {
    this.dto = dto;
  }

  public VictoryDTO getVictoryDto() {
    return dto;
  }
}
