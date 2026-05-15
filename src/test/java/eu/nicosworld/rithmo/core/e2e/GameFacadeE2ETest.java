package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.FindOptionHelper;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.core.helper.TestDebugger;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.PreDefinedGame;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.engine.testutils.RithmoDebug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GameFacadeE2ETest {

    private GameFacade gameFacade;
    private InMemoryGameRepository gameRepository;
    private InMemoryOptionRepository optionRepository;

    @BeforeEach
    void setUp() {
        gameRepository = new InMemoryGameRepository();
        optionRepository = new InMemoryOptionRepository();
        gameFacade = new GameFacade(gameRepository, optionRepository);
    }

    @Test
    @DisplayName("Flux complet : Démarrage et exécution d'une action (gère Moves et PreCaptures)")
    void fullGameFlowTest() throws Exception {
        // 1. ARRANGEMENT
        Game initialGame = PreDefinedGame.predefinedVerySimpleGame();
        UUID gameId = initialGame.getId();
        RithmoDebug.printBoardAfterArrange(initialGame.getCurrentState().state().board());

        // 2. ACTION : Démarrage du jeu
        GameStatusDTO statusAfterStart = gameFacade.startGame(initialGame);

        // Vérification initiale : on a des options et le jeu est sauvé
        assertThat(gameRepository.findById(gameId)).isPresent();
        assertThat(statusAfterStart.possibleOptions()).isNotEmpty();

        // 3. ACTION : Sélection et exécution de la première option jouable
        // On simule l'UI qui doit extraire un ID valide pour le moteur
        UUID actionIdToPlay = FindOptionHelper.findAnyNonSkipOption(statusAfterStart);

        GameStatusDTO statusAfterPlay = gameFacade.play(gameId, actionIdToPlay);

        // 4. ASSERTIONS FINALES
        // Le jeu en base doit avoir évolué (TurnState différent)
        Game savedGame = gameRepository.findById(gameId).orElseThrow();
        assertThat(savedGame.getCurrentState())
                .as("L'état du tour doit avoir été mis à jour en base")
                .isNotEqualTo(initialGame.getCurrentState());

        // L'OptionRepository doit avoir été nettoyé
        assertThat(optionRepository.findById(actionIdToPlay))
                .as("L'action jouée ne doit plus exister dans le repository")
                .isEmpty();

        // Le nouveau DTO doit être cohérent
        assertThat(statusAfterPlay.gameId()).isEqualTo(gameId);
        assertThat(statusAfterPlay.possibleOptions())
                .as("De nouvelles options doivent être générées pour le nouveau tour/phase")
                .isNotNull();
    }

    @Test
    @DisplayName("Pyramid vs Pyramid : Power and Encounter")
    void testPyramidVsPyramid() throws VictoryException, PatException {
        Game initialGame = PreDefinedTestGame.pyramidVsPyramid();
        GameStatusDTO statusAfterStart = gameFacade.startGame(initialGame);

        StatusDTOAssertion.from(statusAfterStart)
                .isInPreCapturePhase()
                .canCaptureInOneDecision("PBT16", "PBC4");

        TestDebugger.render(statusAfterStart);

        DecisionDTO dto = statusAfterStart.possibleDecisions()
                .stream()
                .filter(d -> !d.skip())
                .filter(d -> d.capturedIdList().size() > 1)
                .findFirst()
                .orElseThrow();

        PieceDTO actor = FindOptionHelper.findActor(statusAfterStart, dto);

        GameStatusDTO statusAfterCapture = gameFacade.play(initialGame.getId(), dto.id());

        TestDebugger.render(statusAfterCapture);
        TestDebugger.print(statusAfterCapture.possibleDecisions());
        TestDebugger.print(statusAfterCapture.possibleOptions());

        StatusDTOAssertion.from(statusAfterCapture)
                .isInMovePhase()
                .dontHaveSkipOption()
                .haveAllDecisionsWithActor(actor)
                .havePyramidComposedBy(PlayerColorDTO.BLACK, "BS36", "BS25", "BT9", "BC1")
                .hasStrictMoveDecisionTo("(3,3)", "(1,2)", "(0,1)", "(2,3)")
                .havePyramidValue(PlayerColorDTO.BLACK, 71);
    }

    @Test
    @DisplayName("Pyramid vs incomplete Pyramid : Power and Encounter")
    void testPyramidVsIncompleteBlackPyramid() throws VictoryException, PatException {
        Game initialGame = PreDefinedTestGame.pyramidVsIncompleteBlackPyramid();
        GameStatusDTO statusAfterStart = gameFacade.startGame(initialGame);

        StatusDTOAssertion.from(statusAfterStart)
                .isInPreCapturePhase()
                .canCaptureInOneDecision("PBT16", "PBC4");

        TestDebugger.render(statusAfterStart);

        DecisionDTO dto = statusAfterStart.possibleDecisions()
                .stream()
                .filter(d -> !d.skip())
                .filter(d -> d.capturedIdList().size() > 1)
                .findFirst()
                .orElseThrow();

        TestDebugger.print(statusAfterStart.possibleOptions());
        PieceDTO actor = FindOptionHelper.findActor(statusAfterStart, dto);
        System.out.println(actor);


        GameStatusDTO statusAfterCapture = gameFacade.play(initialGame.getId(), dto.id());

        TestDebugger.render(statusAfterCapture);
        //TestDebugger.print(statusAfterCapture.possibleDecisions());
        //TestDebugger.print(statusAfterCapture.possibleOptions());

        StatusDTOAssertion.from(statusAfterCapture)
                .isInMovePhase()
                .dontHaveSkipOption()
                .haveAllDecisionsWithActor(actor)
                .havePyramidComposedBy(PlayerColorDTO.BLACK, "BS36")
                .hasStrictMoveDecisionTo("(3,3)", "(1,2)", "(0,1)", "(2,3)")
                .havePyramidValue(PlayerColorDTO.BLACK, 36);
    }
}