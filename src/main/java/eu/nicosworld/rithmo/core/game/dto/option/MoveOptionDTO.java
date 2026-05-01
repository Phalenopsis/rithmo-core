package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.engine.model.Position;
import eu.nicosworld.rithmo.engine.move.MoveNature;

import java.util.UUID;

public record MoveOptionDTO(
        UUID id,
        Position from,
        Position to,
        MoveNature nature
) implements PlayerOptionDTO {}
