package eu.nicosworld.rithmo.core.turn.action;

/**
 * Internal control-flow action used exclusively by the TurnProcessor.
 *
 * <p>Represents a synthetic action used to trigger automatic state transitions
 * when no player input is required.</p>
 *
 * <p>This action is strictly internal to the engine and must not be:
 * <ul>
 *     <li>exposed to the UI layer</li>
 *     <li>persisted in repositories</li>
 *     <li>used as a player-issued command</li>
 * </ul>
 * </p>
 */
public record NoAction() implements TurnAction {}
