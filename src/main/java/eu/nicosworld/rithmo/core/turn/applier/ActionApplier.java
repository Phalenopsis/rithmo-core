package eu.nicosworld.rithmo.core.turn.applier;

import eu.nicosworld.rithmo.core.exception.logical.NoActionException;
import eu.nicosworld.rithmo.core.turn.action.*;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.model.Position;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.move.MoveNature;
import eu.nicosworld.rithmo.engine.reintroduction.Reintroduction;
import java.util.Objects;

/**
 * Component responsible for executing {@link TurnAction}s and calculating
 * their impact on the {@link GameState}.
 * <p>
 * It acts as the bridge between the high-level turn logic and the
 * low-level engine rules (movement and captures).
 */
public class ActionApplier {

    private final CaptureApplier captureApplier;
    private final MoveApplier moveApplier;
    private final ReintroductionApplier reintroductionApplier;

    public ActionApplier(CaptureApplier captureApplier,
                         MoveApplier moveApplier,
                         ReintroductionApplier reintroductionApplier) {
        this.captureApplier = captureApplier;
        this.moveApplier = moveApplier;
        this.reintroductionApplier = reintroductionApplier;
    }

    /**
     * Applies the given action to the state.
     *
     * @param state  The current game state.
     * @param action The action to perform.
     * @return An {@link AppliedResult} containing the new state and move metadata.
     * @throws NoActionException If a {@link NoAction} is passed, indicating a logic error in the flow.
     */
    public AppliedResult apply(GameState state, TurnAction action) {

        return switch (action) {

            case PreCaptureAction a ->
                    applyPreCapture(state, a);

            case SkipPreCaptureAction a ->
                    AppliedResult.of(state);

            case MoveAction a ->
                    applyMove(state, a);

            case PostCaptureAction a ->
                    applyPostCapture(state, a);

            case SkipPostCaptureAction a ->
                    AppliedResult.of(state);

            case ReintroductionAction a ->
                applyReintroduction(state ,a);

            case NoAction a ->
                throw new NoActionException();
        };
    }

    private AppliedResult applyReintroduction(GameState state, ReintroductionAction action) {
        Reintroduction reintroduction = action.reintroduction();

        GameState updatedState = reintroductionApplier.applyReintroduction(state, reintroduction);
        return AppliedResult.withReintroduction(updatedState, reintroduction.position());
    }

    // =========================
    // PRE CAPTURE
    // =========================

    private AppliedResult applyPreCapture(GameState state, PreCaptureAction action) {

        for (CaptureAction capture : action.actions()) {
            state = captureApplier.applyCapture(state, capture);
        }

        Position initialPos = action.actions().getFirst().actor().position();

        if(Objects.isNull(action.landing())) {
            return AppliedResult.withCapture(state, initialPos);
        }
        Move move = new Move(
                initialPos,
                action.landing(),
                MoveNature.REGULAR
        );

        GameState newState = state.withBoard(
                moveApplier.applyMove(state.board(), move)
        );

        return AppliedResult.withCapture(newState, move.to());
    }

    // =========================
    // MOVE
    // =========================

    private AppliedResult applyMove(GameState state, MoveAction action) {

        Move move = action.move();

        GameState newState = state.withBoard(
                moveApplier.applyMove(state.board(), move)
        );


        return AppliedResult.of(newState, move);
    }

    // =========================
    // POST CAPTURE
    // =========================

    private AppliedResult applyPostCapture(GameState state, PostCaptureAction action) {

        for (CaptureAction capture : action.actions()) {
            state = captureApplier.applyCapture(state, capture);
        }

        return AppliedResult.of(state);
    }
}