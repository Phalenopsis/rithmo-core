package eu.nicosworld.rithmo.core.turn.application.decision;

import eu.nicosworld.rithmo.core.game.PendingAction;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.action.TurnAction;
import java.util.UUID;

/**
 * Responsible for binding UI decisions to executable engine actions.
 *
 * <p>Each decision ID is associated with a {@link TurnAction} that can later
 * be retrieved and executed by the engine when the user confirms a move.</p>
 *
 * <p>This layer isolates persistence concerns from decision generation logic
 * and acts as the bridge between UI selection and engine execution.</p>
 */
public class DecisionCommandStore {

    private final OptionRepository optionRepository;

    public DecisionCommandStore(OptionRepository optionRepository) {
        this.optionRepository = optionRepository;
    }

    public void store(UUID gameId, UUID decisionId, TurnAction action) {
        optionRepository.save(
                new PendingAction(decisionId, gameId, action)
        );
    }
}
