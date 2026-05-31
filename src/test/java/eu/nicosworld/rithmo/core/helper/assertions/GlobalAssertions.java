package eu.nicosworld.rithmo.core.helper.assertions;

import static org.assertj.core.api.Assertions.assertThat;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PhaseDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;

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

    public GlobalAssertions isInPostCapturePhase() {
        assertThat(actual.phase())
                .isEqualTo(PhaseDTO.POST_CAPTURE);

        return this;
    }
}
