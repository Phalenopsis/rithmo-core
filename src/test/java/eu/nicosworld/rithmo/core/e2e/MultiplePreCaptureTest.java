package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.game.dto.victory.VictoryConditionDTO;
import eu.nicosworld.rithmo.core.helper.*;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import java.util.UUID;
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
    // Victory rules :
    // VictoryCondition.BODY_AND_GOODS_AND_LAWSUIT
    // VictoryType.BODY, 3,
    // VictoryType.LAWSUIT, 3,
    // VictoryType.GOODS, 70),

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
            .because("8 exp 2 = 64")
          .decisions()
            .canCaptureInOneDecision("WC8(0,3)", "WT64(0,3)");
    // spotless:on

    UUID firstCapture =
        FindDecisionHelper.findCaptureDecisionIdWithLanding(
            status, "BT8(2,3)", "(0,3)", "WC8(0,3)", "WT64(0,3)");
    GameStatusDTO status2 = gameFacade.play(game.getId(), firstCapture);

    StatusDTOAssertion.from(status2)
        .status()
        .hasActivePlayer(PlayerColorDTO.BLACK)
        .isInMovePhase()
        .assets()
        .capturedContains("WC8", "WT64")
        // Victory checks :
        // VictoryType.LAWSUIT, 3,
        // VictoryType.GOODS, 70),
        .hasEmptyReserve()
        .decisions()
        .hasMoveDecisionTo("(0,1)");
    UUID moveTo01 = FindDecisionHelper.findMoveDecisionId(status2, "BT8(0,3)", "(0,1)");

    GameStatusDTO status3 = gameFacade.play(game.getId(), moveTo01);

    StatusDTOAssertion.from(status3).status().hasActivePlayer(PlayerColorDTO.WHITE).isInMovePhase();
    UUID whiteMove = FindDecisionHelper.findMoveDecisionId(status3, "WC64(3,1)", "(2,2)");
    GameStatusDTO status4 = gameFacade.play(game.getId(), whiteMove);

    UUID blackMove = FindDecisionHelper.findMoveDecisionId(status4, "BS8(0,0)", "(3,0)");
    GameStatusDTO status5 = gameFacade.play(game.getId(), blackMove);

    UUID whiteMove2 = FindDecisionHelper.findMoveDecisionId(status5, "WC64(2,2)", "(3,3)");
    GameStatusDTO status6 = gameFacade.play(game.getId(), whiteMove2);

    StatusDTOAssertion.from(status6)
        .status()
        .hasActivePlayer(PlayerColorDTO.BLACK)
        .isInPreCapturePhase()
        .decisions()
        .canCaptureInOneDecision("WC64(3,3)");

    UUID lastAction =
        FindDecisionHelper.findCaptureDecisionIdWithLanding(
            status6, "BS8(3,0)", "(3,3)", "WC64(3,3)");

    VictoryAssertion.from(() -> gameFacade.play(game.getId(), lastAction))
        .hasWinner(PlayerColorDTO.BLACK)
        .hasCondition(VictoryConditionDTO.BODY_AND_GOODS_AND_LAWSUIT)
        .hasCapturedCount(3)
        .hasRequiredCount(3)
        .hasCapturedValue(136)
        .hasRequiredValue(70)
        .hasCapturedDigitCount(5)
        .hasCapturedDigitRequired(3);
  }
}
