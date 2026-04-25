package eu.nicosworld.rithmo.core.turn.action;

public sealed interface TurnAction
        permits PreCaptureAction,
        SkipPreCaptureAction,
        MoveAction,
        PostCaptureAction,
        SkipPostCaptureAction {
}
