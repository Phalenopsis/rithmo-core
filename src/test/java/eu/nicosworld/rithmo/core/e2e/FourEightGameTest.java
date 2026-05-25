package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.PreDefinedGame;
import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.status.CaptureTypeDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.FindDecisionHelper;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.core.helper.TestDebugger;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

public class FourEightGameTest {
    private GameFacade gameFacade;

    @BeforeEach
    void setUp() {
        InMemoryGameRepository gameRepository = new InMemoryGameRepository();
        InMemoryOptionRepository optionRepository = new InMemoryOptionRepository();
        gameFacade = new GameFacade(gameRepository, optionRepository);
    }

    @Test
    @DisplayName("Four Eight Game")
    void testFourEightGame() throws VictoryException, PatException {
        Game game = PreDefinedGame.fourEightBoardGame();
        UUID gameId = game.getId();
        GameStatusDTO status = gameFacade.startGame(game);

        TestDebugger.render(status);

        UUID firstMoveId = FindDecisionHelper.findMoveDecisionId(status, "BP91(0,0)", "(2,0)");
        GameStatusDTO statusDTO2 = gameFacade.play(gameId, firstMoveId);

        UUID firstTurnPostCaptureId = FindDecisionHelper.findCaptureDecisionId(statusDTO2, "BC4(2,0)", "WC16(7,0)");
        GameStatusDTO statusDTO3 = gameFacade.play(gameId, firstTurnPostCaptureId);

        UUID secondTurnPreCaptureId = FindDecisionHelper.findCaptureDecisionId(statusDTO3, "WT36(7,0)", "BT9(2,0)");
        GameStatusDTO statusDTO4 = gameFacade.play(gameId, secondTurnPreCaptureId);

        UUID secondTurnMoveId = FindDecisionHelper.findMoveDecisionId(statusDTO4, "WP174(7,0)", "(4,0)");
        GameStatusDTO statusDTO5 = gameFacade.play(gameId, secondTurnMoveId);

        StatusDTOAssertion.from(statusDTO5)
                .hasActivePlayer(PlayerColorDTO.WHITE)
                .isInPostCapturePhase()
                .haveSkipDecision()
                .hasCaptureDecisionCount(3)
                .hasNOptions(6)
                .canPostCaptureWithByEncounter("WT25(4,0)", "BS25(2,0)")
                .canPostCaptureWithByAssault("WT25(4,0)", "BS25(2,0)")
                .canPostCaptureWithByEncounter("WT36(4,0)", "BS36(2,0)")
                .canPostCaptureWithByAssault("WT36(4,0)", "BS36(2,0)")
                .canPostCaptureWithByPower("WS64(4,0)", "BC4(2,0)")
                .hasCaptureCiblesFor("WT25(4,0)", "BS25(2,0)")
                .hasCaptureCiblesFor("WS64(4,0)", "BC4(2,0)")
                .hasCaptureCiblesFor("WT36(4,0)", "BS36(2,0)");
    }
}
