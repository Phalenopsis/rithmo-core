package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.core.turn.option.PreCaptureOption;
import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.CaptureContext;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.capture.capturerule.AmbushRule;
import eu.nicosworld.rithmo.engine.capture.capturerule.EncounterRule;
import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Player;
import eu.nicosworld.rithmo.engine.move.FreePathMovementValidator;
import eu.nicosworld.rithmo.engine.move.RegularMoveGenerator;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
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

        System.out.println(board.prettyPrint());

        for(PreCaptureOption option: choices) {
            System.out.println(option);
        }

        assertEquals(12, choices.size());
    }

    @Test
    void resolvePreCaptures_3choices() {
        BoardBuilder builder = new BoardBuilder(4, 4);
        Board board = builder.blackCircle(5).at(1,1)
                .whiteTriangle(5).at(0,2)
                .whiteCircle(5).at(2, 0)
                .build();

        List<PreCaptureOption> choices = captureResolver.resolvePreCaptures(GameState.initial(board, black));

        assertEquals(4, choices.size());
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

        assertEquals(4, choices.size());
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

        assertEquals(5, choices.size());
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

            System.out.println("RAW CAPTURES:");

            assertEquals(3, choices.size());
        }
    }

    @Test
    void resolvePostCaptures() {
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

            System.out.println("  actions = " + actions);
        });
    }
}