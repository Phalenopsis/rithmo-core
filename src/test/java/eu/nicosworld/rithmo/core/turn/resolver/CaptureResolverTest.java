package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.core.helper.PreDefinedTestGame;
import eu.nicosworld.rithmo.core.helper.TestDebugger;
import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.engine.capture.capturerule.AssaultRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.PowerRule;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.model.CaptureContext;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.capture.capturerule.AmbushRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.EncounterRule;
import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.move.FreePathMovementValidator;
import eu.nicosworld.rithmo.engine.move.RegularMoveGenerator;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
import eu.nicosworld.rithmo.engine.testutils.RithmoDebug;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;


import static org.junit.jupiter.api.Assertions.*;

class CaptureResolverTest {

    Player black;
    CaptureEngine captureEngine;
    CaptureResolver captureResolver;
    RegularMoveGenerator regularMoveGenerator;
    FreePathMovementValidator freePathMovementValidator;

    @BeforeEach
    void setup() {
        black = Player.BLACK;

        regularMoveGenerator = new RegularMoveGenerator();
        freePathMovementValidator = new FreePathMovementValidator();

        EncounterRule encounterRule = new EncounterRule(regularMoveGenerator, freePathMovementValidator);

        captureEngine = new CaptureEngine(List.of(encounterRule));
        captureResolver = new CaptureResolver(captureEngine);
    }

    @Test
    void resolvePreCaptures_7Possibilities() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder.blackCircle(5).at(1,1)
                .whiteTriangle(5).at(0,2)
                .whiteCircle(5).at(2, 0)
                .whiteSquare(5).at(1, 3)
                .whiteSquare(5).at(0,0)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

        assertEquals(7, choices.size());
    }

    @Test
    void resolvePreCaptures_3choices() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder.blackCircle(5).at(1,1)
                .whiteTriangle(5).at(0,2)
                .whiteCircle(5).at(2, 0)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

        assertEquals(3, choices.size());
    }

    @Test
    void resolvePreCaptures_2blackPiecesButOnly1canCapture() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder.blackCircle(5).at(1,1)
                .whiteTriangle(5).at(0,2)
                .whiteCircle(5).at(2, 0)
                .blackSquare(5).at(3,3)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

        assertEquals(3, choices.size());
    }

    @Test
    void resolvePreCaptures_2blackPieces() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder.blackCircle(5).at(1,1)
                .whiteTriangle(5).at(0,2)
                .whiteCircle(5).at(2, 0)
                .blackCircle(5).at(1,3)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

        assertEquals(4, choices.size());
    }

    @Test
    void resolvePreCaptures_noCapture() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder
                .whiteTriangle(5).at(0,2)
                .whiteCircle(5).at(2, 0)
                .blackSquare(5).at(1,3)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

        assertEquals(0, choices.size());
    }

    @Test
    void resolvePreCaptures_OnlyCircle() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder
                .whiteCircle(5).at(0,2)
                .whiteCircle(5).at(2, 0)
                .blackCircle(5).at(1,3)
                .blackCircle(5).at(1,1)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

        assertEquals(4, choices.size());
        List<PreCaptureOption> choicesForAttackerInX1Y1 = choices.stream()
                .filter(p -> p.captures().getFirst().actor().position().equals(new Position(1,1)))
                .toList();
        assertEquals(3, choicesForAttackerInX1Y1.size());
    }

    @Test
    void resolvePreCaptures_PyramidComponentCouldDo2Captures() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder
                .piece(PieceType.PYRAMID, 0, PlayerColor.BLACK)
                .withComponent(PieceType.CIRCLE, 5)
                .withComponent(PieceType.CIRCLE, 4)
                .at(1, 1)
                .whiteCircle(5).at(0,2)
                .whiteCircle(5).at(2, 0)
                .blackCircle(5).at(1,3)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

        assertEquals(4, choices.size());
        List<PreCaptureOption> choicesForAttackerInX1Y1 = choices.stream()
                .filter(p -> p.captures().getFirst().actor().position().equals(new Position(1,1)))
                .toList();

        assertEquals(3, choicesForAttackerInX1Y1.size());
    }

    @Test
    void resolvePreCaptures_WhiteAttacker() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder
                .piece(PieceType.PYRAMID, 0, PlayerColor.BLACK)
                .withComponent(PieceType.CIRCLE, 5)
                .withComponent(PieceType.CIRCLE, 4)
                .at(1, 1)
                .whiteCircle(5).at(0,2)
                .whiteCircle(5).at(2, 0)
                .blackCircle(5).at(1,3)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, Player.WHITE));

        assertEquals(4, choices.size());
        List<PreCaptureOption> choicesForAttackerInX1Y1 = choices.stream()
                .filter(p -> p.captures().getFirst().actor().position().equals(new Position(0,2)))
                .toList();
        assertEquals(3, choicesForAttackerInX1Y1.size());
    }

    @Test
    void resolvePreCaptures_WhiteAttackerOnly2Pyramids() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder
                .piece(PieceType.PYRAMID, 0, PlayerColor.BLACK)
                .withComponent(PieceType.CIRCLE, 5)
                .withComponent(PieceType.CIRCLE, 4)
                .withComponent(PieceType.CIRCLE, 6)
                .at(1, 1)
                .piece(PieceType.PYRAMID, 0, PlayerColor.WHITE)
                .withComponent(PieceType.CIRCLE, 5)
                .withComponent(PieceType.CIRCLE, 4)
                .at(2, 0)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, Player.WHITE));

        assertEquals(2, choices.size());
        List<PreCaptureOption> choicesForAttackerInX1Y1 = choices.stream()
                .filter(p -> p.captures().getFirst().actor().position().equals(new Position(2,0))
                && p.captures().getFirst().actor().specificComponent().getValue() == 5)
                .toList();
        assertEquals(1, choicesForAttackerInX1Y1.size());
    }

    @Test
    void resolvePreCaptures_WhiteAttacker2PyramidsAndAnotherTarget() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder
                .piece(PieceType.PYRAMID, 0, PlayerColor.BLACK)
                .withComponent(PieceType.CIRCLE, 5)
                .withComponent(PieceType.CIRCLE, 4)
                .withComponent(PieceType.CIRCLE, 6)
                .at(1, 1)
                .piece(PieceType.PYRAMID, 0, PlayerColor.WHITE)
                .withComponent(PieceType.CIRCLE, 5)
                .withComponent(PieceType.CIRCLE, 4)
                .at(2, 0)
                .blackCircle(5)
                .at(3,1)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, Player.WHITE));

        //assertEquals(2, choices.size());
        List<PreCaptureOption> choicesForAttackerInX1Y1 = choices.stream()
                .filter(p -> p.captures().getFirst().actor().position().equals(new Position(2,0))
                        && p.captures().getFirst().actor().specificComponent().getValue() == 5)
                .toList();

        // attendu : composant valeur 5 peut prendre composant noir de valeur 5 landing en 1,1
        //           composant valeur 5 peut prendre pion noir de valeur 5 landing en 3,1
        //          composant valeur 5 peut prendre composant noir de valeur 5 et pion noir de valeur 5 landing en 1,1
        //          composant valeur 5 peut prendre composant noir de valeur 5 et pion noir de valeur 5 landing en 3,1
        assertEquals(3, choicesForAttackerInX1Y1.size());
    }

    @Nested
    class TwoRulesEngine {
        @BeforeEach
        void setup() {
            EncounterRule encounterRule = new EncounterRule(regularMoveGenerator, freePathMovementValidator);
            AmbushRule ambushRule = new AmbushRule(regularMoveGenerator, freePathMovementValidator);

            captureEngine = new CaptureEngine(List.of(encounterRule, ambushRule));
            captureResolver = new CaptureResolver(captureEngine);
        }

        @Test
        void preCapture_ambushEncounter() {
            BoardBuilder builder = new BoardBuilder(4, 4);
            Board board = builder.blackCircle(1).at(0,0)
                    .whiteTriangle(5).at(1,1)
                    .blackCircle(4).at(0,2)
                    .build();

            List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

            assertEquals(2, choices.size());
        }

        @Test
        void preCapture_ambushEncounter2() {
            BoardBuilder builder = new BoardBuilder(4, 4);
            Board board = builder.blackCircle(1).at(0,0)
                    .whiteTriangle(4).at(1,1)
                    .blackCircle(4).at(0,2)
                    .build();

            List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

            assertEquals(3, choices.size());
        }
    }

    @Nested
    class TwoRulesEngine_EncounterAndPower {
        @BeforeEach
        void setup() {
            EncounterRule encounterRule = new EncounterRule(regularMoveGenerator, freePathMovementValidator);
            PowerRule powerRule = new PowerRule(regularMoveGenerator, freePathMovementValidator);

            captureEngine = new CaptureEngine(List.of(encounterRule, powerRule));
            captureResolver = new CaptureResolver(captureEngine);
        }

        @Test
        void resolvePostCaptures_WithPyramids_EncounterAndPower() {
            Board board = new BoardBuilder(4, 4)
                    .fullBlackPyramidAt(0, 0)
                    .fullWhitePyramidAt(0, 3)
                    .build();

            List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

            assertEquals(6, choices.size());
        }
    }

    @Nested
    class FourRulesEngine {
        @BeforeEach
        void setup() {
            EncounterRule encounterRule = new EncounterRule(regularMoveGenerator, freePathMovementValidator);
            PowerRule powerRule = new PowerRule(regularMoveGenerator, freePathMovementValidator);
            AssaultRule assaultRule = new AssaultRule(regularMoveGenerator, freePathMovementValidator);
            AmbushRule ambushRule = new AmbushRule(regularMoveGenerator, freePathMovementValidator);

            captureEngine = new CaptureEngine(List.of(encounterRule, powerRule, assaultRule, ambushRule));
            captureResolver = new CaptureResolver(captureEngine);
        }

        @Test
        void resolvePostCaptures_WithPyramids() {
            Board board = PreDefinedTestGame.gameWithMultiCaptures_FourRules().getCurrentState().state().board();

            List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));
            assertEquals(13, choices.size());
        }
    }


    /**
     * debug Helper
     * @param board board to debug
     */
    void printRawCapture(Board board) {
        System.out.println("RAW CAPTURES:");

        board.getPiecesForPlayer(black).forEach(p -> {
            System.out.println("Piece: " + p);

            CaptureContext ctx = new CaptureContext(GameState.initial(board, black), p);

            List<CaptureAction> actions = captureEngine.findCaptures(ctx);

            System.out.println("  captures = " + actions);
        });
    }
}