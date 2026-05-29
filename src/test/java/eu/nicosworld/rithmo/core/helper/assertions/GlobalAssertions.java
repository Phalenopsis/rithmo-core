package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PhaseDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;

import static org.assertj.core.api.Assertions.assertThat;

public final class GlobalAssertions extends NestedStatusAssertions {
    public GlobalAssertions(GameStatusDTO actual, StatusDTOAssertion parent) {
        super(actual, parent);
    }

    public GlobalAssertions hasActivePlayer(PlayerColorDTO colorDTO) {
        assertThat(actual.currentPlayer())
                .isEqualTo(colorDTO);
        return this;
    }

    public GlobalAssertions isInPreCapturePhase() {
        assertThat(actual.phase())
                .isEqualTo(PhaseDTO.PRE_CAPTURE);
        return this;
    }

    public GlobalAssertions isInMovePhase() {
        assertThat(actual.phase())
                .isEqualTo(PhaseDTO.MOVE);
        return this;
    }
}
