package eu.nicosworld.rithmo.core.game.dto.option;

public sealed interface PlayerOptionDTO
        permits CaptureOptionDTO, MoveOptionDTO, PreCaptureOptionDTO, ReintroductionOptionDTO, SkipOptionDTO {
}
