package eu.nicosworld.rithmo.core.helper.persistence;

import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.persistence.GameRepository;

import java.util.*;

public class InMemoryGameRepository implements GameRepository {
    private final Map<UUID, Game> storage = new HashMap<>();

    @Override
    public void save(Game game) {
        storage.put(game.getId(), game);
    }

    @Override
    public Optional<Game> findById(UUID id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Override
    public List<Game> findAll() {
        return new ArrayList<>(storage.values());
    }
}
