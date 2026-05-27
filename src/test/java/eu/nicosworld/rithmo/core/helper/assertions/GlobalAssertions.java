package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;

public class GlobalAssertions extends NestedStatusAssertions {
    public GlobalAssertions(GameStatusDTO actual, StatusDTOAssertion statusAssertions) {
        super(actual, statusAssertions);
    }
}
