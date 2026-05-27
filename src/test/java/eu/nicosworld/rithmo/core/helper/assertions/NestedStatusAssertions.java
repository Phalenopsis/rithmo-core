package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;

abstract class NestedStatusAssertions {

    protected final GameStatusDTO status;
    protected final StatusDTOAssertion parent;

    protected NestedStatusAssertions(
            GameStatusDTO status,
            StatusDTOAssertion parent
    ) {
        this.status = status;
        this.parent = parent;
    }

    public StatusDTOAssertion and() {
        return parent;
    }
}
