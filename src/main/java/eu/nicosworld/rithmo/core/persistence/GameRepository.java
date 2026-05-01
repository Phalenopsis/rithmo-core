package eu.nicosworld.rithmo.core.persistence;

import eu.nicosworld.rithmo.core.game.Game;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GameRepository {
    void save(Game game);
    Optional<Game> findById(UUID id);
    List<Game> findAll();
}
