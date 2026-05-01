package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;

public record PreCaptureOptionDTO(
        Position attackerPos,
        List<Position> targets,      // Qui on capture
        List<LandingChoiceDTO> choices
) implements PlayerOptionDTO {}