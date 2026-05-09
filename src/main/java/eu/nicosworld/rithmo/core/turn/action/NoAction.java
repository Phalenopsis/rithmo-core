package eu.nicosworld.rithmo.core.turn.action;

/**
 * Represents a technical non-action used for automatic transitions or internal processing.
 * <p>
 *     Should never exit from TurnProcessor.
 * </p>
 */
public record NoAction() implements TurnAction {}
