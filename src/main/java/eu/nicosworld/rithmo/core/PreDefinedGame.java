package eu.nicosworld.rithmo.core;

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

public class PreDefinedGame {
    public static Game predefinedVerySimpleGame() {
        Board board = new BoardBuilder(4,4)
                .blackTriangle(4).at(0,0)
                .blackCircle(4).at(1,1)
                .whiteCircle(4).at(3,1)
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
