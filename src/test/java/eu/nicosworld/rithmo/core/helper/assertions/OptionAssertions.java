package eu.nicosworld.rithmo.core.helper.assertions;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;

public class OptionAssertions extends NestedStatusAssertions {
    public OptionAssertions(GameStatusDTO actual, StatusDTOAssertion statusAssertions) {
        super(actual, statusAssertions);
    }
}
