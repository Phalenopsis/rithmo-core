package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.engine.model.Position;

import java.util.UUID;

public record LandingChoiceDTO(
        UUID actionId,
        Position landingPosition
) {}
