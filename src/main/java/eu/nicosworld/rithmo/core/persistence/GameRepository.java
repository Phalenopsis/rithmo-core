package eu.nicosworld.rithmo.core.persistence;

import eu.nicosworld.rithmo.core.game.Game;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Storage interface for game aggregates.
 *
 * <p>Responsible for persisting the full state of a game, including its configuration, current
 * board state, and turn history.
 */
public interface GameRepository {

  /**
   * Persists or updates a game instance.
   *
   * @param game The game aggregate to save.
   */
  void save(Game game);

  /**
   * Retrieves a game by its unique identifier.
   *
   * @param id The game UUID.
   * @return An Optional containing the game if found, empty otherwise.
   */
  Optional<Game> findById(UUID id);

  /**
   * Returns all games currently stored in the system.
   *
   * @return A list of all game instances.
   */
  List<Game> findAll();
}
