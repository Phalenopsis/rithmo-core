package eu.nicosworld.rithmo.core.turn.application.decision;

import eu.nicosworld.rithmo.core.DecisionKey;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.action.TurnAction;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Orchestrates the full lifecycle of a UI decision.
 *
 * <p>This includes:
 *
 * <ul>
 *   <li>Resolving stable decision identity
 *   <li>Projecting raw engine output into UI DTOs
 *   <li>Persisting executable actions for later execution
 *   <li>Maintaining an in-memory set of active UI decisions
 * </ul>
 *
 * <p>This class does not contain domain logic itself. It delegates responsibilities to specialized
 * services.
 *
 * <p>It acts as the coordination layer between engine output and UI-facing decision representation.
 */
public class DecisionRegistry {

  private final DecisionIdService idService;
  private final DecisionProjectionService projectionService;
  private final DecisionCommandStore commandStore;

  private final Set<DecisionDTO> decisions = new HashSet<>();

  public DecisionRegistry(OptionRepository optionRepository) {
    this.idService = new DecisionIdService();
    this.projectionService = new DecisionProjectionService();
    this.commandStore = new DecisionCommandStore(optionRepository);
  }

  public void register(UUID gameId, TurnAction action, DecisionDTO rawDecision) {

    DecisionKey key = buildKey(rawDecision);

    UUID id = idService.resolve(key);

    DecisionDTO finalDecision = projectionService.build(id, rawDecision);

    decisions.add(finalDecision);

    commandStore.store(gameId, id, action);
  }

  public Set<DecisionDTO> getDecisions() {
    return Set.copyOf(decisions);
  }

  private DecisionKey buildKey(DecisionDTO dto) {
    return new DecisionKey(dto.actorId(), dto.capturedIdList(), dto.landing(), dto.skip());
  }
}
