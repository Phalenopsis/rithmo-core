package eu.nicosworld.rithmo.core.turn;

import static eu.nicosworld.rithmo.core.turn.testutils.TurnHelper.setupProcessor;

import eu.nicosworld.rithmo.core.exception.PatException;
import eu.nicosworld.rithmo.core.exception.VictoryException;
import eu.nicosworld.rithmo.core.turn.action.MoveAction;
import eu.nicosworld.rithmo.core.turn.action.PostCaptureAction;
import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.core.turn.testutils.TurnAssertion;
import eu.nicosworld.rithmo.core.turn.testutils.TurnHelper;
import eu.nicosworld.rithmo.engine.capture.CaptureType;
import eu.nicosworld.rithmo.engine.capture.capturerule.EncounterRule;
import eu.nicosworld.rithmo.engine.model.*;
import eu.nicosworld.rithmo.engine.move.FreePathMovementValidator;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.move.RegularMoveGenerator;
import eu.nicosworld.rithmo.engine.setup.BoardBuilder;
import eu.nicosworld.rithmo.engine.victory.BodyVictoryRule;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TurnProcessorIntegrationAfterMoveTest {
  private BoardBuilder builder;
  private TurnProcessor processor;

  @BeforeEach
  void setup() {
    RegularMoveGenerator regularMoveGenerator = new RegularMoveGenerator();
    FreePathMovementValidator freePathMovementValidator = new FreePathMovementValidator();

    EncounterRule encounterRule =
        new EncounterRule(regularMoveGenerator, freePathMovementValidator);

    BodyVictoryRule bodyVictoryRule = new BodyVictoryRule(2);

    processor = setupProcessor(List.of(encounterRule), List.of(bodyVictoryRule));
    builder = new BoardBuilder(4, 4);
  }

  @Test
  void shouldMovePieceAndPassToNextPhase_SwitchPlayer_NoCapturePossible()
      throws VictoryException, PatException {
    // 1. Setup :
    Board board = builder.blackCircle(5).at(1, 1).whiteCircle(5).at(3, 0).build();
    GameState state = GameState.initial(board, Player.BLACK);

    Position startingPosition = new Position(1, 1);
    Piece blackCircle5 = board.getPieceAt(startingPosition);

    // On part d'un état où on doit bouger (déjà calculé par le processeur auparavant)
    TurnState turn1 = processor.process(TurnState.of(state, TurnPhase.START), null);

    // 2. Action : On récupère un mouvement et on l'applique
    Move move = TurnHelper.getAllMoves(turn1, startingPosition).getFirst();
    Piece actor = turn1.state().board().getPieceAt(move.from());
    MoveAction action = new MoveAction(actor, move);

    TurnState turn2 = processor.process(turn1, action);

    // 3. Assertions
    TurnAssertion.assertThis(turn2)
        .hasCurrentWhitePlayer()
        .isInMoveApplicationPhase()
        .checkState()
        .player(Player.BLACK)
        .isEmpty(startingPosition) // Case de départ vide
        .hasOnBoard(blackCircle5)
        .at(move.to()); // Arrivée OK
  }

  @Test
  void shouldMovePieceAndPassToNextPhase_CapturePossible() throws VictoryException, PatException {
    // 1. Setup :
    Board board = builder.blackCircle(5).at(1, 1).whiteCircle(5).at(3, 1).build();
    GameState state = GameState.initial(board, Player.BLACK);

    Position startingPosition = new Position(1, 1);
    Position goalPosition = new Position(2, 2);
    Piece blackCircle5 = board.getPieceAt(startingPosition);
    Position whiteCirclePos = new Position(3, 1);

    // On part d'un état où on doit bouger (déjà calculé par le processeur auparavant)
    TurnState turn1 = processor.process(TurnState.of(state, TurnPhase.START), null);

    // 2. Action : On récupère un mouvement et on l'applique
    Move move = TurnHelper.getMove(turn1, startingPosition, goalPosition);
    Piece actor = turn1.state().board().getPieceAt(move.from());
    MoveAction action = new MoveAction(actor, move);

    TurnState turn2 = processor.process(turn1, action);

    // 3. Assertions
    TurnAssertion.assertThis(turn2)
        .hasCurrentBlackPlayer()
        .isInPostCaptureApplicationPhase()
        .hasOptionsCount(2)
        .hasSkipPostCaptureOption()
        .hasPostCaptureOptions(CaptureType.ENCOUNTER)
        .hasPostCaptureOption(whiteCirclePos)
        .checkState()
        .player(Player.BLACK)
        .isEmpty(startingPosition) // Case de départ vide
        .hasOnBoard(blackCircle5)
        .at(move.to()); // Arrivée OK
  }

  @Test
  void shouldMovePieceAndCaptureAndPassToNextPhase_SwitchPlayer()
      throws VictoryException, PatException {
    // 1. Setup :
    Board board =
        builder.blackCircle(5).at(1, 1).whiteCircle(5).at(3, 1).whiteSquare(15).at(0, 3).build();
    GameState state = GameState.initial(board, Player.BLACK);

    Position startingPosition = new Position(1, 1);
    Position goalPosition = new Position(2, 2);
    Piece blackCircle5 = board.getPieceAt(startingPosition);
    Position whiteCirclePos = new Position(3, 1);

    // On part d'un état où on doit bouger (déjà calculé par le processeur auparavant)
    TurnState turn1 = processor.process(TurnState.of(state, TurnPhase.START), null);

    // 2. Action : On récupère un mouvement et on l'applique
    Move move = TurnHelper.getMove(turn1, startingPosition, goalPosition);
    Piece actor = turn1.state().board().getPieceAt(move.from());
    MoveAction action = new MoveAction(actor, move);

    TurnState turn2 = processor.process(turn1, action);

    PostCaptureOption option = TurnHelper.findPostCaptureOption(turn2.options(), whiteCirclePos);
    PostCaptureAction captureAction = PostCaptureAction.from(option);

    TurnState turn3 = processor.process(turn2, captureAction);

    // 3. Assertions
    TurnAssertion.assertThis(turn3)
        .hasCurrentWhitePlayer()
        .isInMoveApplicationPhase()
        .hasOptionsCount(4);
  }
}
