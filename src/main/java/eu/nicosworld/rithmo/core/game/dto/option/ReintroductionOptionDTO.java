package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.turn.option.ReintroductionOption;
import eu.nicosworld.rithmo.engine.model.Position;

public record ReintroductionOptionDTO(PieceDTO pieceDTO,
                                      Position landing) implements PlayerOptionDTO {
    public static ReintroductionOptionDTO from(ReintroductionOption option) {
        return new ReintroductionOptionDTO(
                PieceDTO.from(option.reintroduction().piece(), null),
                option.reintroduction().position());
    }
}
