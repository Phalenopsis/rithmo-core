package eu.nicosworld.rithmo.core.turn.option;

import eu.nicosworld.rithmo.engine.reintroduction.Reintroduction;

/**
 * Represents a single executable piece reintroduction possibility.
 *
 * <p>A {@code ReintroductionOption} is an atomic turn option produced by the engine
 * when a captured piece may legally be reintroduced onto the board.</p>
 *
 * <p>The acting piece and placement information are fully carried
 * by the underlying {@link Reintroduction} object.</p>
 *
 * @param reintroduction
 *         The engine-level reintroduction description.
 */
public record ReintroductionOption(
        Reintroduction reintroduction
) implements TurnOption {}
