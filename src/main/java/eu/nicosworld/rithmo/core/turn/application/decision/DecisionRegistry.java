package eu.nicosworld.rithmo.core.turn.application.decision;

import eu.nicosworld.rithmo.core.DecisionKey;
import eu.nicosworld.rithmo.core.game.PendingAction;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.action.TurnAction;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Maintains UI decision identity consistency across multiple engine actions.
 *
 * Multiple engine actions may correspond to the same player-facing decision.
 * This registry:
 * - deduplicates UI decisions
 * - guarantees stable decision IDs
 * - persists executable pending actions
 */
public class DecisionRegistry {

    private final OptionRepository optionRepository;

    /**
     * UI-visible decisions.
     */
    private final Set<DecisionDTO> decisions =
            new HashSet<>();

    /**
     * Deduplication registry.
     */
    private final Map<DecisionKey, UUID> existingDecisionIds =
            new HashMap<>();

    public DecisionRegistry(
            OptionRepository optionRepository
    ) {
        this.optionRepository = optionRepository;
    }

    /**
     * Registers a new executable action and associates it
     * with a stable UI decision identifier.
     *
     * Several engine actions may share the same decision ID.
     */
    public void register(
            UUID gameId,
            TurnAction action,
            DecisionDTO rawDecision
    ) {

        DecisionKey key =
                buildDecisionKey(rawDecision);

        UUID decisionId =
                existingDecisionIds.computeIfAbsent(
                        key,
                        k -> UUID.randomUUID()
                );

        boolean alreadyExists =
                decisions.stream()
                        .anyMatch(d ->
                                d.id().equals(decisionId)
                        );

        if (!alreadyExists) {

            DecisionDTO finalDecision;

            if (rawDecision.skip()) {

                finalDecision =
                        DecisionDTO.skipFrom(decisionId);

            } else {

                finalDecision =
                        new DecisionDTO(
                                decisionId,
                                rawDecision.actorId(),
                                rawDecision.capturedIdList(),
                                rawDecision.landing(),
                                false
                        );
            }

            decisions.add(finalDecision);
        }

        /**
         * IMPORTANT:
         * multiple engine actions
         * may share the same decision ID
         */
        savePending(
                gameId,
                decisionId,
                action
        );
    }

    public Set<DecisionDTO> getDecisions() {
        return Set.copyOf(decisions);
    }

    /**
     * Persists an executable pending action.
     */
    private void savePending(
            UUID gameId,
            UUID decisionId,
            TurnAction action
    ) {

        optionRepository.save(
                new PendingAction(
                        decisionId,
                        gameId,
                        action
                )
        );
    }

    private DecisionKey buildDecisionKey(
            DecisionDTO dto
    ) {

        return new DecisionKey(
                dto.actorId(),
                dto.capturedIdList(),
                dto.landing(),
                dto.skip()
        );
    }
}