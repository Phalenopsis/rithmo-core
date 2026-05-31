package eu.nicosworld.rithmo.core.turn.application.decision;

import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import java.util.UUID;

/**
 * Responsible for projecting raw decision data into a UI-consumable {@link DecisionDTO}.
 *
 * <p>This service does not generate identity. It only transforms raw decision data into its final
 * immutable UI representation.
 *
 * <p>It ensures consistent formatting of skip and non-skip decisions regardless of their origin in
 * the engine.
 *
 * <p>This layer is pure and stateless.
 */
public class DecisionProjectionService {

  public DecisionDTO build(UUID id, DecisionDTO raw) {

    if (raw.skip()) {
      return DecisionDTO.skipFrom(id);
    }

    return new DecisionDTO(id, raw.actorId(), raw.capturedIdList(), raw.landing(), false);
  }
}
