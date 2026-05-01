package eu.nicosworld.rithmo.core.persistence;

import eu.nicosworld.rithmo.core.game.PendingAction;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OptionRepository {

    // Vide la table/map des options pour recommencer un tour à neuf
    void clearOptionsForGame(UUID gameId);

    // Persiste l'objet complet (ID, GameID, Action?, DTO?)
    void save(PendingAction pending);

    // Retourne TOUT ce qui est stocké pour ce jeu (Actions techniques ET DTOs d'affichage)
    List<PendingAction> findOptionsForGame(UUID gameId);

    // Pour l'exécution du tour : on retrouve l'action par l'ID cliqué par l'utilisateur
    Optional<PendingAction> findById(UUID optionId);
}