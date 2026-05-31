package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.helper.assertions.*;

public class StatusDTOAssertion {

  private final GameStatusDTO actual;
  private final StatusAssertionSupport support;

  private final DecisionAssertions decisions;
  private final OptionAssertions options;
  private final AssetAssertions assets;
  private final BoardAssertions board;
  private final GlobalAssertions status;

  private StatusDTOAssertion(GameStatusDTO actual) {
    this.actual = actual;
    this.support = new StatusAssertionSupport(actual);

    this.decisions = new DecisionAssertions(actual, this);
    this.options = new OptionAssertions(actual, this);
    this.assets = new AssetAssertions(actual, this);
    this.board = new BoardAssertions(actual, this);
    this.status = new GlobalAssertions(actual, this);
  }

  public static StatusDTOAssertion from(GameStatusDTO statusDTO) {
    return new StatusDTOAssertion(statusDTO);
  }

  public DecisionAssertions decisions() {
    return decisions;
  }

  public OptionAssertions options() {
    return options;
  }

  public AssetAssertions assets() {
    return assets;
  }

  public BoardAssertions board() {
    return board;
  }

  public GlobalAssertions status() {
    return status;
  }

  public StatusAssertionSupport support() {
    return support;
  }
}
