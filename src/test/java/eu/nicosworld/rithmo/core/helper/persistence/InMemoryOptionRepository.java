package eu.nicosworld.rithmo.core.helper.persistence;

import eu.nicosworld.rithmo.core.game.PendingAction;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryOptionRepository implements OptionRepository {
  private final Map<UUID, PendingAction> storage = new HashMap<>();

  @Override
  public void clearOptionsForGame(UUID gameId) {
    storage.values().removeIf(action -> action.gameId().equals(gameId));
  }

  @Override
  public void save(PendingAction pending) {
    storage.put(pending.id(), pending);
  }

  @Override
  public List<PendingAction> findOptionsForGame(UUID gameId) {
    return storage.values().stream()
        .filter(action -> action.gameId().equals(gameId))
        .collect(Collectors.toList());
  }

  @Override
  public Optional<PendingAction> findById(UUID optionId) {
    return Optional.ofNullable(storage.get(optionId));
  }
}
