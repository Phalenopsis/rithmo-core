package eu.nicosworld.rithmo.core.turn.application.decision;

import eu.nicosworld.rithmo.core.DecisionKey;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Responsible for maintaining stable identity of UI decisions.
 *
 * <p>A {@link DecisionKey} represents the semantic meaning of a decision
 * (actor, captured pieces, landing, skip flag).</p>
 *
 * <p>This service ensures that identical decision semantics always map
 * to the same UUID during a game session.</p>
 *
 * <p>This guarantees UI stability even when multiple engine actions
 * produce equivalent decision structures.</p>
 */
public class DecisionIdService {

    private final Map<DecisionKey, UUID> ids = new HashMap<>();

    public UUID resolve(DecisionKey key) {
        return ids.computeIfAbsent(key, k -> UUID.randomUUID());
    }
}