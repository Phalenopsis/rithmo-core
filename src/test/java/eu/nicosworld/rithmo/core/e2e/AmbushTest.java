package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PhaseDTO;
import eu.nicosworld.rithmo.core.helper.FindDecisionHelper;
import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.helper.StatusDTOAssertion;
import eu.nicosworld.rithmo.core.helper.TestDebugger;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryGameRepository;
import eu.nicosworld.rithmo.core.helper.persistence.InMemoryOptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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

        StatusDTOAssertion.from(nextStatus)
                .isInPostCapturePhase()
                .canCaptureInOneDecision("WT12");
    }

    @Test
    @DisplayName("2. on doit avoir une option de pre capture en ambush")
    void shouldProposeAPreCaptureOption() throws Exception {
        Game game = PreDefinedTestGame.ambushPreCaptureTest_Case();
        GameStatusDTO status = gameFacade.startGame(game);

        StatusDTOAssertion.from(status)
                .isInPreCapturePhase()
                .canCaptureInOneDecision("WT12");
    }

    @Test
    @DisplayName("3. on doit avoir une option de pre capture en ambush")
    void shouldProposeAPreCaptureOption_With2fullPyramid() throws Exception {
        Game game = PreDefinedTestGame.ambushPreCaptureTest_BlackAndWhitePyramidCase();
        GameStatusDTO status = gameFacade.startGame(game);

        TestDebugger.render(status);
        TestDebugger.print(status.possibleOptions());
        TestDebugger.print(status.possibleDecisions());

        StatusDTOAssertion.from(status)
                        .isInPreCapturePhase()
                .hasNDecisions(11)
                .hasNOptions(7);
    }
}
