package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;

public class DecisionAssertions extends NestedStatusAssertions {
    public DecisionAssertions(GameStatusDTO actual, StatusDTOAssertion statusAssertions) {
        super(actual, statusAssertions);

    }
}
