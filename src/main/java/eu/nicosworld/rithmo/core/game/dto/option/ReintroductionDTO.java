package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.turn.option.ReintroductionOption;
import eu.nicosworld.rithmo.engine.model.Position;

public record ReintroductionDTO(PieceDTO pieceDTO,
                                Position landing) {
    public static ReintroductionDTO from(ReintroductionOption option) {
        return new ReintroductionDTO(
                PieceDTO.from(option.reintroduction().piece(), null),
                option.reintroduction().position());
    }
}
