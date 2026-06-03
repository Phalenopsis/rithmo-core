package eu.nicosworld.rithmo.core.e2e;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.FindDecisionHelper;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import eu.nicosworld.rithmo.engine.model.Position;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EncounterTutorialTest {

  private GameFacade gameFacade;

  @BeforeEach
  void setUp() {
    InMemoryGameRepository gameRepository = new InMemoryGameRepository();
    InMemoryOptionRepository optionRepository = new InMemoryOptionRepository();
    gameFacade = new GameFacade(gameRepository, optionRepository);
  }

  @Test
  @DisplayName("1. Choisir SKIP doit mener à une phase de MOVE")
  void shouldLeadToMovesWhenSkipIsChosen() throws Exception {
    Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
    GameStatusDTO status = gameFacade.startGame(game);

    UUID skipId = FindDecisionHelper.findSkipDecision(status);

    GameStatusDTO nextStatus = gameFacade.play(game.getId(), skipId);

    // spotless:off
    StatusDTOAssertion.from(nextStatus)
            .status()
              .isInMovePhase()
            .decisions()
              .hasOnlyMoveDecisions();
    // spotless:on
  }

  @Test
  @DisplayName("2. Choisir une capture simple (1 cible) doit mener à une phase de MOVE")
  void shouldLeadToMovesWhenSingleCaptureIsChosen() throws Exception {
    Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
    GameStatusDTO status = gameFacade.startGame(game);

    UUID landingId = FindDecisionHelper.findDecisionWithCaptures(status, 1);
    GameStatusDTO nextStatus = gameFacade.play(game.getId(), landingId);

    // spotless:off
    StatusDTOAssertion.from(nextStatus)
            .status()
              .isInMovePhase()
            .decisions()
              .hasOnlyMoveDecisions();
    // spotless:on
  }

  @Test
  @DisplayName("3. Choisir la double capture doit lever une VictoryException")
  void shouldThrowVictoryExceptionWhenDoubleCaptureIsChosen() throws Exception {
    Game game = PreDefinedTestGame.encounterPreCaptureTestCase();
    GameStatusDTO status = gameFacade.startGame(game);

    UUID landingId = FindDecisionHelper.findDecisionWithCaptures(status, 2);

    assertThatThrownBy(() -> gameFacade.play(game.getId(), landingId))
        .isInstanceOf(VictoryException.class);
  }

  @Test
  @DisplayName("4. Il doit y avoir 3 options")
  void shouldExposeSkipAndTwoCaptureOptionsWhenTwoPiecesTargetSameEnemy() throws Exception {
    Game game = PreDefinedTestGame.encounterPreCaptureTestCase_WhitePlayer();
    GameStatusDTO status = gameFacade.startGame(game);

    // spotless:off
    StatusDTOAssertion.from(status)
        .status()
          .hasActivePlayer(PlayerColorDTO.WHITE)
          .isInPreCapturePhase()
        .options()
          .hasOptionCount(3)
          .hasSkipOption()
          .canCaptureWithByEncounter("WC4(3,1)", "BC4(2,2)")
          .because("4 = 4")
          .canCaptureWithByEncounter("WC4(3,3)", "BC4(2,2)")
          .because("4 = 4")
        .decisions()
          .hasSkipDecision()
          .hasCaptureDecisionCount(2);
    // spotless:on
  }

  @Test
  @DisplayName("5. on doit avoir une option de pre capture en ambush")
  void shouldResolveAmbushPreCaptureAndTransitionToMovePhase() throws Exception {

    Game game = PreDefinedTestGame.encounterPreCaptureTest_WhiteAttacker2PyramidsAndAnotherTarget();
    GameStatusDTO status = gameFacade.startGame(game);

    // spotless:off
    StatusDTOAssertion.from(status)
        .status()
        .isInPreCapturePhase()
        .options()
        .hasOptionCount(4)
        .hasOptionCountFor("WC5(2,0)", 2)
        .hasOptionCountFor("WC4(2,0)", 1)
        .decisions()
        .hasDecisionCount(5)
        .hasDecisionCountFor("WC5(2,0)", 3)
        .hasDecisionCountFor("WC4(2,0)", 1);
    // spotless:on

    UUID id =
        FindDecisionHelper.findCaptureDecisionId(
            status, "WC5(2,0)", new Position(3, 1), "BC5(3,1)", "BC5(1,1)");

    GameStatusDTO statusAfterPreCapture = gameFacade.play(game.getId(), id);

    // spotless:off
    StatusDTOAssertion.from(statusAfterPreCapture)
        .status()
          .hasActivePlayer(PlayerColorDTO.WHITE)
          .isInMovePhase()
        .board()
          .hasPiece("WP9(3,1)")
        .assets()
          .capturedContains("BC5", "BC5")
          .hasInReserve("WC5")
        .options()
          .hasNoReintroductionOptions()
        .decisions()
          .hasOnlyMoveDecisions()
          .hasStrictMoveDecisionTo("(2,0)", "(2,2)");
    // spotless:on
  }
}
