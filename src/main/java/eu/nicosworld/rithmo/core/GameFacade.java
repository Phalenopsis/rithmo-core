package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.core.turn.resolver.CaptureResolver;
import eu.nicosworld.rithmo.core.turn.resolver.MoveResolver;
import eu.nicosworld.rithmo.engine.capture.CaptureEngine;
import eu.nicosworld.rithmo.engine.capture.capturerule.EncounterRule;
import eu.nicosworld.rithmo.engine.model.Board;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Player;
import eu.nicosworld.rithmo.engine.model.PlayerColor;
import eu.nicosworld.rithmo.engine.move.FreePathMovementValidator;
import eu.nicosworld.rithmo.engine.move.MovementEngine;
import eu.nicosworld.rithmo.engine.move.RegularMoveGenerator;

import java.util.List;

public class GameService {

    private final Player white = Player.WHITE;
    private final Player black = Player.BLACK;

    private final MovementEngine movementEngine = new MovementEngine();
    private final MoveResolver moveResolver = new MoveResolver(movementEngine);

    private final RegularMoveGenerator regularMoveGenerator = new RegularMoveGenerator();
    private final FreePathMovementValidator freePathMovementValidator = new FreePathMovementValidator();

    private final EncounterRule encounterRule = new EncounterRule(regularMoveGenerator, freePathMovementValidator);

    private final CaptureEngine captureEngine = new CaptureEngine(List.of(encounterRule));


    public GameService() {

    }

    public void startGame(Board board) {



    }


}
