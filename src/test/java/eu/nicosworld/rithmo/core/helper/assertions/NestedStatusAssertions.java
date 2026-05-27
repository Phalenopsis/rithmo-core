package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;

abstract class NestedStatusAssertions {

    protected final GameStatusDTO actual;
    protected final StatusDTOAssertion parent;

    protected NestedStatusAssertions(
            GameStatusDTO actual,
            StatusDTOAssertion parent
    ) {
        this.actual = actual;
        this.parent = parent;
    }

    public StatusDTOAssertion and() {
        return parent;
    }

    public GlobalAssertions status() {
        return parent.status();
    }

    public DecisionAssertions decisions() {
        return parent.decisions();
    }

    public OptionAssertions options() {
        return parent.options();
    }

    public AssetAssertions assets() {
        return parent.assets();
    }

    public BoardAssertions board() {
        return parent.board();
    }
}
