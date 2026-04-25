package eu.nicosworld.rithmo.core.turn.option;

public sealed interface TurnOption
        permits PreCaptureOption,
        SkipPreCaptureOption,
        MoveOption,
        PostCaptureOption,
        SkipPostCaptureOption {
}