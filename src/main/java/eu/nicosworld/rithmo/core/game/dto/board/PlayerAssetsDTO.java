package eu.nicosworld.rithmo.core.game.dto.board;

import eu.nicosworld.rithmo.engine.model.PlayerAssets;
import java.util.List;

public record PlayerAssetsDTO(List<PieceDTO> captured, List<PieceDTO> reserve) {
  public static PlayerAssetsDTO from(PlayerAssets assets) {
    return new PlayerAssetsDTO(
        assets.captured().stream().map(PieceDTO::from).toList(),
        assets.reserve().stream().map(PieceDTO::from).toList());
  }
}
