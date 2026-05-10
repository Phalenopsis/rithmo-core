package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.status.CaptureTypeDTO;
import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;

import java.util.List;

public record CaptureOptionDTO(
        PieceDTO target,
        CaptureTypeDTO type,
        List<PieceDTO> ally
) implements PlayerOptionDTO {
    public static List<CaptureOptionDTO> from(PostCaptureOption option) {
        return option.captures()
                        .stream()
                        .map(CaptureOptionDTO::from)
                        .toList();
    }

    public static CaptureOptionDTO from(CaptureAction action) {
        return new CaptureOptionDTO(
                PieceDTO.from(
                        action.target().specificComponent(),
                        action.targetPosition()),
                CaptureTypeDTO.from(action.type()),
                action.supporters().stream()
                        .map(PieceDTO::from)
                        .toList()
                );
    }
}
