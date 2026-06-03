package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MultiplePreCaptureTest {
  private GameFacade gameFacade;

  @BeforeEach
  void setUp() {
    InMemoryGameRepository gameRepository = new InMemoryGameRepository();
    InMemoryOptionRepository optionRepository = new InMemoryOptionRepository();
    gameFacade = new GameFacade(gameRepository, optionRepository);
  }

  @Test
  void testMultiplePreCaptures_EncounterAndPower() throws VictoryException, PatException {
    Game game = PreDefinedTestGame.gameTestWithMultiplePreCaptures();
    GameStatusDTO status = gameFacade.startGame(game);

    // spotless:off
    StatusDTOAssertion.from(status)
        .status()
        .hasActivePlayer(PlayerColorDTO.BLACK)
        .isInPreCapturePhase()
        .options()
          .hasOptionCount(4)
          .hasSkipOption()
          .canCaptureWithByEncounter("BC4(2,1)", "WT4(3,2)")
            .because("4 = 4")
          .canCaptureWithByEncounter("BC4(2,1)", "WC4(3,0)")
            .because("4 = 4")
          .canCaptureWithByEncounter("BC4(2,1)", "WT4(3,2)", "WC4(3,0)")
            .because("4 = 4", "4 = 4")
          .canCaptureWithByPower("BT16(1,2)", "WT4(3,2)")
            .because("16 root 2 = 4");
    // spotless:on
  }

  @Test
  void test4Rules() throws VictoryException, PatException {
    Game game = PreDefinedTestGame.gameWithMultiCaptures_FourRules();
    GameStatusDTO status = gameFacade.startGame(game);

    // spotless:off
    StatusDTOAssertion.from(status)
        .status()
          .isInPreCapturePhase()
          .hasActivePlayer(PlayerColorDTO.BLACK)
        .options()
          .hasOptionCount(8)
          .hasSkipOption()
          .canCaptureWithByEncounter("BS8(0,0)", "WC8(0,3)")
            .because("8 = 8")
          .canCaptureWithByEncounter("BT8(2,3)", "WC8(0,3)")
            .because("8 = 8")
          .canCaptureWithByAmbush("BS8(0,0)", "WT64(0,3)")
            .because("8 * 8 = 64")
          .canCaptureWithByAmbush("BT8(2,3)", "WT64(0,3)")
            .because("8 * 8 = 64")
          .canCaptureWithByAssault("BT8(2,3)", "WC8(0,3)")
            .because("8 * 1 = 8")
          .canCaptureWithByPower("BS8(0,0)", "WT64(0,3)")
            .because("8 exp 2 = 64")
          .canCaptureWithByPower("BT8(2,3)", "WT64(0,3)")
            .because("8 exp 2 = 64");
    // spotless:on
  }
}
