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
import eu.nicosworld.rithmo.engine.victory.VictoryRule;

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

    public static Game FourEigthBoardGame() {
        Board board = new BoardBuilder(8, 4)
                // --- CAMP NOIR (Colonnes 0, 1, 2) ---
                // Colonne 0 : Pyramide et Carrés
                .fullBlackPyramidAt(new Position(0, 0))
                .blackSquare(16).at(0, 1)
                .blackSquare(36).at(0, 2)
                .blackSquare(64).at(0, 3)

                // Colonne 1 : Cercles
                .blackCircle(2).at(1, 0)
                .blackCircle(4).at(1, 1)
                .blackCircle(6).at(1, 2)
                .blackCircle(8).at(1, 3)

                // Colonne 2 : Triangles
                .blackTriangle(12).at(2, 0)
                .blackTriangle(20).at(2, 1)
                .blackTriangle(30).at(2, 2)
                .blackTriangle(42).at(2, 3)

                // --- CAMP BLANC (Colonnes 5, 6, 7) ---
                // Colonne 5 : Triangles
                .whiteTriangle(9).at(5, 0)
                .whiteTriangle(25).at(5, 1)
                .whiteTriangle(49).at(5, 2)
                .whiteTriangle(81).at(5, 3)

                // Colonne 6 : Cercles
                .whiteCircle(3).at(6, 0)
                .whiteCircle(5).at(6, 1)
                .whiteCircle(7).at(6, 2)
                .whiteCircle(9).at(6, 3)

                // Colonne 7 : Carrés et Pyramide
                .whiteSquare(25).at(7, 0)
                .whiteSquare(45).at(7, 1)
                .whiteSquare(81).at(7, 2)
                .fullWhitePyramidAt(new Position(7, 3))

                .build();

        GameOptions options = new GameOptions(
                Set.of(
                        CaptureRuleOption.ENCOUNTER,
                        CaptureRuleOption.ASSAULT,
                        CaptureRuleOption.POWER,
                        CaptureRuleOption.AMBUSH),
                Map.of(
                        VictoryRuleOption.BODY, 1,
                        VictoryRuleOption.GOODS, 5)
        );

        GameState gameState = GameState.initial(board, Player.BLACK);
        TurnState turnState = TurnState.of(gameState, TurnPhase.START);

        return new Game(options, turnState);
    }
}
