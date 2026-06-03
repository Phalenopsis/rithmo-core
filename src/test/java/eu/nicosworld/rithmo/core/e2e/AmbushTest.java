package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.helper.FindDecisionHelper;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class AmbushTest {
  private GameFacade gameFacade;

  @BeforeEach
  void setUp() {
    InMemoryGameRepository gameRepository = new InMemoryGameRepository();
    InMemoryOptionRepository optionRepository = new InMemoryOptionRepository();
    gameFacade = new GameFacade(gameRepository, optionRepository);
  }

  @Test
  @DisplayName("1. Après le move, on doit avoir une option de post capture en ambush")
  void shouldProposeAPostCaptureOption() throws Exception {
    Game game = PreDefinedTestGame.ambushPostCaptureTest_Case();
    GameStatusDTO status = gameFacade.startGame(game);

    UUID moveId = FindDecisionHelper.findMoveDecisionId(status, "BC4(0,3)", "(1,2)");
    GameStatusDTO nextStatus = gameFacade.play(status.gameId(), moveId);

    // spotless:off
    StatusDTOAssertion.from(nextStatus)
        .status()
          .isInPostCapturePhase()
        .decisions()
          .canCaptureInOneDecision("WT12(2,1)")
          .hasCaptureCiblesFor("BC4(1,2)", "WT12(2,1)")
          .cannotCaptureWith("BC8(1,0)", "WT12(2,1)");
    // spotless:on

  }

  @Test
  @DisplayName("2. on doit avoir une option de pre capture en ambush")
  void shouldProposeAPreCaptureOption() throws Exception {
    Game game = PreDefinedTestGame.ambushPreCaptureTest_Case();
    GameStatusDTO status = gameFacade.startGame(game);

    // spotless:off
    StatusDTOAssertion.from(status)
        .status()
          .isInPreCapturePhase()
        .decisions()
          .canCaptureInOneDecision("WT12(2,1)")
          .hasCaptureSourcesFor("WT12(2,1)", "BC8(1,0)", "BC4(1,2)");
    // spotless:on
  }

  @Test
  @DisplayName("3. on doit avoir 6 options de pre capture en ambush")
  void shouldProposeAPreCaptureOption_With2fullPyramid() throws Exception {
    Game game = PreDefinedTestGame.ambushPreCaptureTest_BlackAndWhitePyramidCase();
    GameStatusDTO status = gameFacade.startGame(game);

    // spotless:off
    StatusDTOAssertion.from(status)
        .status()
          .isInPreCapturePhase()
        .options()
          .hasOptionCount(7)
          .hasSkipOption()
          .canCaptureWithByAmbush("BT16(1,0)", "WS64(2,1)")
            .because("16 * 4 = 64")
          .canCaptureWithByAmbush("BT9(1,0)", "WT36(2,1)")
            .because("9 * 4 = 36")
          .canCaptureWithByAmbush("BC4(1,0)", "WC16(2,1)")
            .because("4 * 4 = 16")
          .canCaptureWithByAmbush("BC4(1,2)", "WC16(2,1)", "WT36(2,1)", "WS64(2,1)")
            .because("4 * 4 = 16", "4 * 9 = 36", "4 * 16 = 64")
        .decisions()
          .hasDecisionCount(11)
          .hasSkipDecision()
          .canCaptureInOneDecision("WC16(2,1)", "WT36(2,1)", "WS64(2,1)");
    // spotless:on
  }
}
