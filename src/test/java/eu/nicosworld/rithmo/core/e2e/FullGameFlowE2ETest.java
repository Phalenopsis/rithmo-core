package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.helper.FindOptionHelper;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.core.game.dto.status.PhaseDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.engine.model.Position;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FullGameFlowE2ETest {

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
    @DisplayName("Scénario complet : Switch Player BLACK -> WHITE -> BLACK + Victoire")
    void fullScenarioTest() throws Exception {
        // Arrange
        Game game = PreDefinedTestGame.switchPlayerTutorial();
        UUID gameId = game.getId();

        // 1. BLACK déplace son cercle (0,0) -> (1,1)
        GameStatusDTO status1 = gameFacade.startGame(game);
        assertThat(status1.currentPlayer()).isEqualTo(PlayerColorDTO.BLACK);

        // Comme pas de PreCapture possible au début, on devrait être en MOVE
        UUID moveBlackId = FindOptionHelper.findMoveIdByDestination(status1, new Position(1, 1));
        GameStatusDTO statusAfterBlackMove = gameFacade.play(gameId, moveBlackId);

        // 2. WHITE prend la main (Switch automatique car BLACK a fini son tour)
        // On vérifie que WHITE est maintenant le joueur actif
        assertThat(statusAfterBlackMove.currentPlayer()).isEqualTo(PlayerColorDTO.WHITE);

        // WHITE se déplace en (2,2)
        UUID moveWhiteId = FindOptionHelper.findMoveIdByDestination(statusAfterBlackMove, new Position(2, 2));
        GameStatusDTO statusAfterWhiteMove = gameFacade.play(gameId, moveWhiteId);

        // 3. WHITE est en phase POST_CAPTURE (ou l'UI propose le choix après le move)
        // On vérifie que WHITE peut choisir de skipper la post-capture
        assertThat(statusAfterWhiteMove.phase()).isEqualTo(PhaseDTO.POST_CAPTURE);

        UUID skipPostId = statusAfterWhiteMove.possibleDecisions().get(DecisionDTO.skipFrom());

        // WHITE skip la post-capture -> Main repasse à BLACK
        GameStatusDTO statusAfterWhiteSkip = gameFacade.play(gameId, skipPostId);
        assertThat(statusAfterWhiteSkip.currentPlayer()).isEqualTo(PlayerColorDTO.BLACK);

        // 4. BLACK a maintenant une opportunité de capture (Encounter)
        // Le processeur s'arrête en PRE_CAPTURE car une action est requise
        assertThat(statusAfterWhiteSkip.phase()).isEqualTo(PhaseDTO.PRE_CAPTURE);


        DecisionDTO captureDecision = statusAfterWhiteSkip.possibleDecisions().keySet().stream()
                .filter(d -> !d.capturedIdList().isEmpty())
                .findFirst()
                .orElseThrow();

        UUID landingId = statusAfterWhiteSkip.possibleDecisions().get(captureDecision);


         //5. BLACK exécute la capture -> VictoryException (VictoryRule BODY = 1)
        assertThatThrownBy(() -> gameFacade.play(gameId, landingId))
                .isInstanceOf(VictoryException.class);
    }
}