package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.game.CaptureRuleOption;
import eu.nicosworld.rithmo.core.game.Game;
import eu.nicosworld.rithmo.core.game.VictoryRuleOption;
import eu.nicosworld.rithmo.core.turn.TurnPhase;
import eu.nicosworld.rithmo.core.turn.TurnState;
import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Player;
import eu.nicosworld.rithmo.engine.model.Position;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
import java.util.Map;
import java.util.Set;

public class PreDefinedGame {
  public static Game predefinedVerySimpleGame() {
    Board board =
        new BoardBuilder(4, 4)
            .blackTriangle(4)
            .at(0, 0)
            .blackCircle(4)
            .at(1, 1)
            .whiteCircle(4)
            .at(3, 1)
            .whiteCircle(4)
            .at(3, 3)
            .build();
    GameOptions options =
        new GameOptions(Set.of(CaptureRuleOption.ENCOUNTER), Map.of(VictoryRuleOption.BODY, 1));

    GameState gameState = GameState.initial(board, Player.BLACK);
    TurnState turnState = TurnState.of(gameState, TurnPhase.START);

    return new Game(options, turnState);
  }

  public static Game fourEightBoardGame() {
    Board board =
        new BoardBuilder(8, 4)
            // --- Black player (Columns 0 and 1) ---
            .fullBlackPyramidAt(new Position(0, 0))
            .blackSquare(16)
            .at(0, 1)
            .blackTriangle(12)
            .at(0, 2)
            .blackCircle(2)
            .at(0, 3)
            .blackCircle(4)
            .at(1, 3)

            // --- White player (Columns 6 and 7) ---
            .fullWhitePyramidAt(new Position(7, 0))
            .whiteSquare(25)
            .at(7, 1)
            .whiteTriangle(9)
            .at(7, 2)
            .whiteCircle(5)
            .at(7, 3)
            .whiteCircle(3)
            .at(6, 3)
            .build();

    GameOptions options =
        new GameOptions(
            Set.of(
                CaptureRuleOption.ENCOUNTER,
                CaptureRuleOption.ASSAULT,
                CaptureRuleOption.POWER,
                CaptureRuleOption.AMBUSH),
            Map.of(
                VictoryRuleOption.BODY, 3,
                VictoryRuleOption.GOODS, 30));

    GameState gameState = GameState.initial(board, Player.BLACK);
    TurnState turnState = TurnState.of(gameState, TurnPhase.START);

    return new Game(options, turnState);
  }
}
