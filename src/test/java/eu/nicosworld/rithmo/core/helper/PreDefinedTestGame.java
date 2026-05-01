package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.GameOptions;
import eu.nicosworld.rithmo.core.game.CaptureRuleOption;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.VictoryRuleOption;
import eu.nicosworld.rithmo.core.turn.TurnPhase;
import eu.nicosworld.rithmo.core.turn.TurnState;
import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Player;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;

import java.util.Map;
import java.util.Set;

public class PreDefinedTestGame {
    /**
     * Creates a test scenario for the "Encounter" capture rule.
     * The board state and options are designed to validate three branching paths:
     * <ul>
     *     <li>If the player chooses to skip, the game should transition to the MOVE phase.</li>
     *     <li>If the player selects a single-capture option (one of the two available),
     *         the game should transition to the MOVE phase.</li>
     *     <li>If the player selects the double-capture option, a {@code VictoryException}
     *         must be thrown as the victory condition (2 BODY) is met.</li>
     * </ul>
     *
     * @return A configured Game instance for the Encounter tutorial.
     */
    public static Game encounterPreCaptureTestCase() {
        Board board = new BoardBuilder(4,4)
                .blackCircle(4).at(2,2)
                .whiteCircle(4).at(3,1)
                .whiteCircle(4).at(3,3)
                .build();
        GameOptions options = new GameOptions(
                Set.of(CaptureRuleOption.ENCOUNTER),
                Map.of(VictoryRuleOption.BODY, 2)
        );

        GameState gameState = GameState.initial(board, Player.BLACK);
        TurnState turnState = TurnState.of(gameState, TurnPhase.START);

        return new Game(options, turnState);
    }

    /**
     * Creates a test scenario for the "Assault" capture rule.
     * The board configuration is designed to trigger an Assault sequence from the first turn:
     * <ul>
     *     <li>If the player chooses to skip, the game should transition to the {@code MOVE} phase.</li>
     *     <li>If the player selects the Assault pre-capture option:
     *          <ul>
     *              <li>The game transitions to the {@code MOVE} phase, specifically proposing a move to (2,2).</li>
     *              <li>After executing this move, the game transitions to the {@code POST_CAPTURE} phase
     *                  to resolve the pending assault.</li>
     *              <li>Upon selecting the post-capture resolution, a {@code VictoryException}
     *                  is thrown as the victory condition (2 BODY) is fulfilled.</li>
     *          </ul>
     *     </li>
     * </ul>
     *
     * @return A configured Game instance for the Assault tutorial.
     */
    public static Game assaultPreCaptureTutorialTestCase() {
        Board board = new BoardBuilder(4,4)
                .blackCircle(4).at(0,1)
                .whiteCircle(8).at(3,1)
                .whiteCircle(4).at(3,3)
                .build();
        GameOptions options = new GameOptions(
                Set.of(CaptureRuleOption.ASSAULT, CaptureRuleOption.ENCOUNTER),
                Map.of(VictoryRuleOption.BODY, 2)
        );

        GameState gameState = GameState.initial(board, Player.BLACK);
        TurnState turnState = TurnState.of(gameState, TurnPhase.START);

        return new Game(options, turnState);
    }

    /**
     * Creates a scenario to validate player switching and multi-turn flow.
     * Initial state: Black Circle at (0,0), White Circle at (3,3).
     * <ul>
     *     <li>Black moves (0,0) to (1,1). Turn switches to White.</li>
     *     <li>White moves (3,3) to (2,2), triggering a potential capture.</li>
     *     <li>White skips the post-capture resolution. Turn switches back to Black.</li>
     *     <li>Black executes an Encounter capture on White's piece at (2,2).</li>
     *     <li>A {@code VictoryException} is expected as White loses their only piece.</li>
     * </ul>
     *
     * @return A Game instance configured for the full player switch scenario.
     */
    public static Game switchPlayerTutorial() {
        Board board = new BoardBuilder(4,4)
                .blackCircle(4).at(0,0)
                .whiteCircle(4).at(3,3)
                .build();
        GameOptions options = new GameOptions(
                Set.of(CaptureRuleOption.ENCOUNTER),
                Map.of(VictoryRuleOption.BODY, 1)
        );

        GameState gameState = GameState.initial(board, Player.BLACK);
        TurnState turnState = TurnState.of(gameState, TurnPhase.START);

        return new Game(options, turnState);
    }
}
