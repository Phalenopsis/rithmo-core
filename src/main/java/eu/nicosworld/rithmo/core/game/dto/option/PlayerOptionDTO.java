package eu.nicosworld.rithmo.core.game.dto.option;

public sealed interface PlayerOptionDTO
        permits
        MoveOptionDTO,
        CaptureOptionDTO,
        PreCaptureOptionDTO,
        SkipOptionDTO {
}
