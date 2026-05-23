package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.helper.FindDecisionHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.engine.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FullGameFlowE2ETest {

    private GameFacade gameFacade;

    @BeforeEach
    void setUp() {
        InMemoryGameRepository gameRepository = new InMemoryGameRepository();
        InMemoryOptionRepository optionRepository = new InMemoryOptionRepository();
        gameFacade = new GameFacade(gameRepository, optionRepository);
    }

    @Test
    @DisplayName("Scénario complet : Switch Player BLACK -> WHITE -> BLACK + Victoire")
    void fullScenarioTest() throws Exception {
        // Arrange
        Game game = PreDefinedTestGame.switchPlayerTutorial();
        UUID gameId = game.getId();

        // 1. BLACK déplace son cercle (0,0) -> (1,1)
        GameStatusDTO status1 = gameFacade.startGame(game);
        StatusDTOAssertion.from(status1)
                 .hasActivePlayer(PlayerColorDTO.BLACK)
                .isInMovePhase()
                .hasStrictMoveDecisionTo("(1,1)");

        // Comme pas de PreCapture possible au début, on devrait être en MOVE
        UUID moveBlackId = FindDecisionHelper.findMoveDecisionId(status1, new Position(1, 1));
        GameStatusDTO statusAfterBlackMove = gameFacade.play(gameId, moveBlackId);

        // 2. WHITE prend la main (Switch automatique car BLACK a fini son tour)
        // On vérifie que WHITE est maintenant le joueur actif
        StatusDTOAssertion.from(statusAfterBlackMove)
                .hasActivePlayer(PlayerColorDTO.WHITE)
                .isInMovePhase()
                .hasNDecisions(1)
                .hasNOptions(1)
                .hasStrictMoveDecisionTo("(2,2)");

        // WHITE se déplace en (2,2)
        UUID moveWhiteId = FindDecisionHelper.findMoveDecisionId(statusAfterBlackMove, new Position(2, 2));
        GameStatusDTO statusAfterWhiteMove = gameFacade.play(gameId, moveWhiteId);

        // 3. WHITE est en phase POST_CAPTURE (ou l'UI propose le choix après le move)
        // On vérifie que WHITE peut choisir de skipper la post-capture
        StatusDTOAssertion.from(statusAfterWhiteMove)
                .hasActivePlayer(PlayerColorDTO.WHITE)
                .haveSkipDecision()
                .canCaptureInOneDecision("BC4(1,1)");

        UUID skipPostId = FindDecisionHelper.findSkipDecision(statusAfterWhiteMove);

        // WHITE skip la post-capture -> Main repasse à BLACK
        GameStatusDTO statusAfterWhiteSkip = gameFacade.play(gameId, skipPostId);
        StatusDTOAssertion.from(statusAfterWhiteSkip)
                .hasActivePlayer(PlayerColorDTO.BLACK)
                .isInPreCapturePhase()
                .canCaptureInOneDecision("WC4(2,2)")
                .haveSkipDecision()
                .hasNDecisionsFor("BC4(1,1)", 1);

        // 4. BLACK a maintenant une opportunité de capture (Encounter)
        // Le processeur s'arrête en PRE_CAPTURE car une action est requise
        UUID landingId = FindDecisionHelper.findCaptureDecisionId(
                statusAfterWhiteSkip,
                "BC4(1,1)",
                new Position(2, 2),
                "WC4(2,2)");


         //5. BLACK exécute la capture -> VictoryException (VictoryRule BODY = 1)
        assertThatThrownBy(() -> gameFacade.play(gameId, landingId))
                .isInstanceOf(VictoryException.class);
    }

    @Test
    @DisplayName("Test for Reintroduction")
    void fullScenarioTest_WithReintroduction() throws VictoryException, PatException {
        Game startGame = PreDefinedTestGame.encounterPreCaptureTestCase();
        UUID gameId = startGame.getId();
        //              0      1      2      3
        //        0   [  .  ][  .  ][  .  ][  .  ]
        //        1   [  .  ][  .  ][  .  ][WC4  ]
        //        2   [  .  ][  .  ][BC4  ][  .  ]
        //        3   [  .  ][  .  ][  .  ][WC4  ]
        GameStatusDTO status1 = gameFacade.startGame(startGame);


        // PreMoveCapture -> Black capture WC4 at 3,1
        UUID captureId = FindDecisionHelper.findCaptureDecisionId(
                status1,
                "BC4(2,2)",
                new Position(3, 1),
                "WC4(3,1)"
                );

        GameStatusDTO status2 = gameFacade.play(gameId, captureId);

        // Black move to 2,0
        UUID moveTo20Id = FindDecisionHelper.findMoveDecisionId(
                status2, new Position(2, 0));
        GameStatusDTO status3 = gameFacade.play(gameId, moveTo20Id);

        StatusDTOAssertion.from(status3)
                .isInMovePhase()
                .hasActivePlayer(PlayerColorDTO.WHITE)
                .dontHaveSkipDecision()
                .hasStrictMoveDecisionTo("(2,2)")
                .hasNoReintroductionOptions();

        // White move to 2,2
        UUID moveTo22Id = FindDecisionHelper.findMoveDecisionId(
                status3, new Position(2, 2));
        GameStatusDTO status4 = gameFacade.play(gameId, moveTo22Id);

        StatusDTOAssertion.from(status4)
                .hasActivePlayer(PlayerColorDTO.BLACK)
                .isInMovePhase()
                .hasReintroductionOptionsForActivePlayer()
                .allReintroductionOptionsComeFromReserve();

        UUID reintroductionID = FindDecisionHelper.findReintroductionIdByDestination(
                status4,
                "BC4", new Position(0, 2));

        GameStatusDTO status5 = gameFacade.play(gameId, reintroductionID);

        StatusDTOAssertion.from(status5)
                .hasActivePlayer(PlayerColorDTO.WHITE)
                .isInMovePhase();

        UUID moveTo11Id = FindDecisionHelper.findMoveDecisionId(
                status5, new Position(1, 1));
        GameStatusDTO status6 = gameFacade.play(gameId, moveTo11Id);

        StatusDTOAssertion.from(status6)
                .hasActivePlayer(PlayerColorDTO.WHITE)
                .isInPostCapturePhase();

        UUID whiteCaptureId = FindDecisionHelper.findDecisionWithCaptures(status6, 2);

        assertThatThrownBy(() -> gameFacade.play(gameId, whiteCaptureId))
                .isInstanceOf(VictoryException.class);
    }
}