package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.exception.logical.NoPhaseException;
import eu.nicosworld.rithmo.core.game.*;
import eu.nicosworld.rithmo.core.persistence.GameRepository;
import eu.nicosworld.rithmo.core.persistence.OptionRepository;
import eu.nicosworld.rithmo.core.turn.TurnPhase;
import eu.nicosworld.rithmo.core.turn.TurnProcessor;
import eu.nicosworld.rithmo.core.turn.TurnProcessorFactory;
import eu.nicosworld.rithmo.core.turn.TurnState;
import eu.nicosworld.rithmo.core.turn.application.GameStatusAssembler;
import eu.nicosworld.rithmo.engine.model.*;
import java.util.*;

/**
 * Main entry point for the Rithmo core logic. Orchestrates game state transitions, persistence, and
 * option generation.
 */
public class GameFacade {

  private final GameRepository gameRepository;
  private final OptionRepository optionRepository;
  private final TurnProcessorFactory turnProcessorFactory;
  private final GameStatusAssembler gameStatusAssembler;

  /** Constructs the facade and initializes stateless engine components. */
  public GameFacade(GameRepository gameRepository, OptionRepository optionRepository) {

    this.gameRepository = gameRepository;
    this.optionRepository = optionRepository;

    this.turnProcessorFactory = new TurnProcessorFactory();
    this.gameStatusAssembler = new GameStatusAssembler(optionRepository);
  }

  /**
   * Initializes and starts a new game with a specific board and options.
   *
   * @param gameOptions Chosen rules and victory conditions.
   * @param board The initial board configuration.
   * @return Current game status and available options.
   * @throws VictoryException If initial state triggers a win.
   * @throws PatException If initial state triggers a stalemate.
   */
  public GameStatusDTO startGame(GameOptions gameOptions, Board board)
      throws VictoryException, PatException {
    GameState gameState = GameState.initial(board, Player.BLACK);
    TurnState turnState = TurnState.of(gameState, TurnPhase.START);

    return play(new Game(gameOptions, turnState));
  }

  /**
   * Starts or resumes a game using a pre-constructed Game object.
   *
   * @param game The game instance.
   * @return Current game status and available options.
   */
  public GameStatusDTO startGame(Game game) throws VictoryException, PatException {
    return play(game);
  }

  /** Internal execution of the first automatic phase transition. */
  private GameStatusDTO play(Game game) throws VictoryException, PatException {
    TurnProcessor processor = turnProcessorFactory.create(game.getOptions());

    // Process initial START phase to reach the first decision point
    TurnState nextState = processor.process(game.getCurrentState());

    Game updatedGame = Game.from(game, nextState);
    return finalizeTurnUpdate(updatedGame);
  }

  /**
   * Processes a user action identified by an option ID.
   *
   * @param gameId ID of the target game.
   * @param optionId ID of the selected option.
   * @return Updated game status.
   */
  public GameStatusDTO play(UUID gameId, UUID optionId) throws VictoryException, PatException {
    Game game =
        gameRepository
            .findById(gameId)
            .orElseThrow(() -> new IllegalArgumentException("Game not found: " + gameId));

    PendingAction pending =
        optionRepository
            .findById(optionId)
            .orElseThrow(
                () -> new IllegalArgumentException("Option expired or invalid: " + optionId));

    TurnProcessor processor = turnProcessorFactory.create(game.getOptions());
    TurnState nextState = processor.process(game.getCurrentState(), pending.actionToExecute());

    Game updatedGame = Game.from(game, nextState);
    return finalizeTurnUpdate(updatedGame);
  }

  /** Saves game state, clears old options, and generates new ones. */
  private GameStatusDTO finalizeTurnUpdate(Game game) throws NoPhaseException {
    gameRepository.save(game);
    optionRepository.clearOptionsForGame(game.getId());

    UiInformation uiInformation = gameStatusAssembler.assemble(game);

    return GameStatusDTO.from(
        game, uiInformation.playerOptionPerPiece(), uiInformation.possibleDecisions());
  }
}
