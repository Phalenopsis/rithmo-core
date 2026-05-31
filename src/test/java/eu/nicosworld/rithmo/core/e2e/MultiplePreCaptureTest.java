package eu.nicosworld.rithmo.core.e2e;

import eu.nicosworld.rithmo.core.GameFacade;
import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.GameStatusDTO;
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
  void testMultiplePreCaptures() throws VictoryException, PatException {
    Game game = PreDefinedTestGame.gameTestWithMultiplePreCaptures();
    GameStatusDTO status = gameFacade.startGame(game);
  }

  @Test
  void test4Rules() throws VictoryException, PatException {
    Game game = PreDefinedTestGame.gameWithMultiCaptures_FourRules();
    GameStatusDTO status = gameFacade.startGame(game);

    StatusDTOAssertion.from(status).isInPreCapturePhase();
  }
}
