package eu.nicosworld.rithmo.core.persistence;

import eu.nicosworld.rithmo.core.UiInformation;
import java.util.List;
import java.util.UUID;

public interface UiInformationRepository {
    void clearForGame(UUID gameId);

    void save(UiInformation uiInformation);

    List<UiInformation> findAllForGame(UUID gameId);
}
