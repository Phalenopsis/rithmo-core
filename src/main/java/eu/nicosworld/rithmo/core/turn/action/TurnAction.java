package eu.nicosworld.rithmo.core.turn.action;

/**
 * Root interface for all possible captures a player can take during a turn.
 * <p>
 * This is a sealed interface, ensuring that the game engine only processes
 * a strictly defined set of action types, maintaining the integrity of the state machine.
 */
public sealed interface TurnAction
        permits PreCaptureAction,
        SkipPreCaptureAction,
        MoveAction,
        PostCaptureAction,
        SkipPostCaptureAction,
        NoAction
{
}