package eu.nicosworld.rithmo.core.persistence;

import eu.nicosworld.rithmo.core.game.PendingAction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Transient storage for pending player choices.
 * <p>
 * This repository maps unique interaction IDs (UUIDs) to internal technical
 * actions. It ensures that when a player clicks an option in the UI, the
 * system can retrieve the corresponding logic to execute.
 */
public interface OptionRepository {

    /**
     * Deletes all stored options associated with a specific game.
     * Usually called at the beginning of a new turn phase to ensure
     * stale options cannot be reused.
     *
     * @param gameId The UUID of the game to clear.
     */
    void clearOptionsForGame(UUID gameId);

    /**
     * Persists a pending action.
     *
     * @param pending The object containing the interaction ID, technical action,
     *                and display DTO.
     */
    void save(PendingAction pending);

    /**
     * Retrieves all pending actions currently available for a specific game.
     *
     * @param gameId The game UUID.
     * @return A list of all available choices (both technical and display-oriented).
     */
    List<PendingAction> findOptionsForGame(UUID gameId);

    /**
     * Retrieves a specific pending action by its interaction ID.
     * Used to resolve which action to execute when a user submits a choice.
     *
     * @param optionId The unique ID generated for the UI choice.
     * @return An Optional containing the pending action if valid and not expired.
     */
    Optional<PendingAction> findById(UUID optionId);
}