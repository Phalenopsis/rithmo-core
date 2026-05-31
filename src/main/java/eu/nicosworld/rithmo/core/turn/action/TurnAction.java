package eu.nicosworld.rithmo.core.turn.action;

/**
 * Root interface for all executable engine actions applied during a turn.
 *
 * <p>A {@code TurnAction} represents a validated, engine-level instruction produced after a player
 * decision has been resolved.
 *
 * <p>It is part of the execution layer and is processed exclusively by the {@code TurnProcessor}
 * and {@code ActionApplier} to mutate the game state.
 *
 * <p>This interface is sealed to guarantee that only a fixed set of action types can exist,
 * ensuring full exhaustiveness of the game engine state machine.
 *
 * <p>Turn actions are never exposed directly to the UI layer. They are always derived from
 * higher-level {@code TurnOption} selections and {@code DecisionDTO} resolutions.
 */
public sealed interface TurnAction
    permits MoveAction,
        NoAction,
        PostCaptureAction,
        PreCaptureAction,
        ReintroductionAction,
        SkipPostCaptureAction,
        SkipPreCaptureAction {}
