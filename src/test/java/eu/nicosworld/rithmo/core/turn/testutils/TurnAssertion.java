package eu.nicosworld.rithmo.core.turn.testutils;

import eu.nicosworld.rithmo.core.turn.TurnPhase;
import eu.nicosworld.rithmo.core.turn.TurnState;
import eu.nicosworld.rithmo.core.turn.option.*;
import eu.nicosworld.rithmo.engine.capture.CaptureAction;
import eu.nicosworld.rithmo.engine.capture.CaptureType;
import eu.nicosworld.rithmo.engine.model.Player;
import eu.nicosworld.rithmo.engine.model.Position;
import eu.nicosworld.rithmo.engine.move.Move;
import eu.nicosworld.rithmo.engine.move.MoveNature;
import eu.nicosworld.rithmo.engine.testutils.GameStateAssertion;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

public class TurnAssertion {
    private final TurnState turnState;

    public GameStateAssertion checkState() {
        return GameStateAssertion.assertThis(this.turnState.state());
    }

    private TurnAssertion(TurnState turnState) {
        this.turnState = turnState;
    }

    public static TurnAssertion assertThis(TurnState turnState) {
        return new TurnAssertion(turnState);
    }

    public TurnAssertion hasCurrentPlayer(Player player) {
        assertThat(turnState.state().currentPlayer())
                .isEqualTo(player);
        return this;
    }

    public TurnAssertion hasCurrentBlackPlayer() {
        return hasCurrentPlayer(Player.BLACK);
    }

    public TurnAssertion hasCurrentWhitePlayer() {
        return hasCurrentPlayer(Player.WHITE);
    }

    public TurnAssertion isInPhase(TurnPhase phase) {
        assertThat(turnState.phase())
                .isEqualTo(phase);
        return this;
    }

    public TurnAssertion isInPostCaptureApplicationPhase() {
        return isInPhase(TurnPhase.POST_CAPTURE_APPLICATION);
    }

    public TurnAssertion isInPreCaptureApplicationPhase() {
        return isInPhase(TurnPhase.PRE_CAPTURE_APPLICATION);
    }

    public TurnAssertion isInMoveApplicationPhase() {
        return isInPhase(TurnPhase.MOVE_APPLICATION);
    }

    public TurnAssertion hasOptionsCount(int expectedNumberOption) {
        assertThat(turnState.options().size()).
                isEqualTo(expectedNumberOption);
        return this;
    }

    public TurnAssertion hasPreCaptureOptionsCount(int expectedCount) {
        long count = turnState.options().stream()
                .filter(PreCaptureOption.class::isInstance)
                .count();

        assertThat(count)
                .as("Nombre d'options de capture pré-mouvement")
                .isEqualTo(expectedCount);

        return this;
    }

    public TurnAssertion hasPreCaptureOptions(CaptureType type) {
        List<PreCaptureOption> preCaptureOptions = turnState.options().stream()
                .filter(PreCaptureOption.class::isInstance)
                .map(o -> (PreCaptureOption) o)
                .toList();

        List<List<CaptureAction>> captureActions = preCaptureOptions.stream()
                .map(PreCaptureOption::captures)
                        .toList();
        Set<CaptureAction> uniques = captureActions.stream().flatMap(Collection::stream)
                .filter(c -> c.type().equals(type))
                        .collect(Collectors.toSet());


        assertThat(uniques)
                .as("Nombre d'options de capture pré-mouvement de type " + type)
                .isNotEmpty();

        return this;
    }

    public TurnAssertion hasPostCaptureOptions(CaptureType type) {
        List<PostCaptureOption> preCaptureOptions = turnState.options().stream()
                .filter(PostCaptureOption.class::isInstance)
                .map(o -> (PostCaptureOption) o)
                .toList();

        List<List<CaptureAction>> captureActions = preCaptureOptions.stream()
                .map(PostCaptureOption::captures)
                .toList();
        Set<CaptureAction> uniques = captureActions.stream().flatMap(Collection::stream)
                .filter(c -> c.type().equals(type))
                .collect(Collectors.toSet());


        assertThat(uniques)
                .as("Nombre d'options de capture pré-mouvement de type " + type)
                .isNotEmpty();

        return this;
    }

    public TurnAssertion hasSkipPreCaptureOption() {
        assertThat(turnState.options())
                .anyMatch(option -> option instanceof SkipPreCaptureOption);
       return this;
    }

    public TurnAssertion hasSkipPostCaptureOption() {
        assertThat(turnState.options())
                .anyMatch(option -> option instanceof SkipPostCaptureOption);
        return this;
    }

    public TurnAssertion hasMoveOption() {
        assertThat(turnState.options())
                .anyMatch(option -> option instanceof MoveOption);
        return this;
    }

    public TurnAssertion hasRegularMoveOption() {
        assertThat(getAllMove())
                .as("Vérification des mouvements réguliers")
                .extracting(Move::nature)
                .contains(MoveNature.REGULAR);
        return this;
    }

    public TurnAssertion hasNoRegularMoveOption() {
        assertThat(getAllMove())
                .as("Vérification d'absence de mouvements réguliers")
                .extracting(Move::nature)
                .doesNotContain(MoveNature.REGULAR);
        return this;
    }

    public TurnAssertion hasNoIrregularMoveOption() {
        assertThat(getAllMove())
                .as("Vérification d'absence de mouvements irrégulier")
                .extracting(Move::nature)
                .doesNotContain(MoveNature.IRREGULAR);
        return this;
    }

    public TurnAssertion hasIrregularMoveOption() {
        assertThat(getAllMove())
                .as("Vérification des mouvements irréguliers")
                .extracting(Move::nature)
                .contains(MoveNature.IRREGULAR);
        return this;
    }

    public TurnAssertion hasRegularMoveOption(int expectedCount) {
        assertThat(getAllMove().stream().filter(m -> m.nature().equals(MoveNature.REGULAR)))
                .hasSize(expectedCount);
        return this;
    }

    public TurnAssertion hasIrregularMoveOption(int expectedCount) {
        assertThat(getAllMove().stream().filter(m -> m.nature().equals(MoveNature.IRREGULAR)))
                .hasSize(expectedCount);
        return this;
    }

    public TurnAssertion hasRegularMoveTo(int x, int y) {
        Position pos = new Position(x, y);
        assertThat(getAllMove())
                .as("Vérification du mouvement régulier vers " + pos)
                .extracting(Move::nature, Move::to) // Extraction multiple !
                .contains(tuple(MoveNature.REGULAR, pos));
        return this;
    }

    public TurnAssertion hasIrregularMoveTo(int x, int y) {
        Position pos = new Position(x, y);
        assertThat(getAllMove())
                .as("Vérification du mouvement irrégulier vers " + pos)
                .extracting(Move::nature, Move::to) // Extraction multiple !
                .contains(tuple(MoveNature.IRREGULAR, pos));
        return this;
    }

    private List<Move> getAllMove() {
        return turnState.options().stream()
                .filter(MoveOption.class::isInstance)
                .map(MoveOption.class::cast)
                .map(MoveOption::move)
                .toList();
    }

    public TurnAssertion hasNoOptions() {
        assertThat(turnState.options()).isEmpty();
        return this;
    }

    public TurnAssertion hasCaptureLandingOption(Position expectedPosition) {
        List<Position> allPossibleLandings = turnState.options().stream()
                .filter(PreCaptureOption.class::isInstance)
                .map(PreCaptureOption.class::cast)
                .map(PreCaptureOption::landing)
                .toList();

        assertThat(allPossibleLandings)
                .as("Vérification de la présence de la destination %s parmi toutes les captures possibles", expectedPosition)
                .contains(expectedPosition);

        return this;
    }

    public TurnAssertion hasPreCaptureLandingOption(Position expectedPosition) {
        List<Position> allLandings = turnState.options().stream()
                // On ne garde que les PreCaptureOption
                .filter(PreCaptureOption.class::isInstance)
                .map(PreCaptureOption.class::cast)
                .map(PreCaptureOption::landing)
                .toList();

        assertThat(allLandings)
                .as("Vérification des options de destination après capture (Pre-Move)")
                .contains(expectedPosition);

        return this;
    }

    public TurnAssertion hasPostCaptureOption(Position targetPosition) {
        boolean found = turnState.options().stream()
                .filter(PostCaptureOption.class::isInstance)
                .map(PostCaptureOption.class::cast)
                .flatMap(option -> option.captures().stream())
                .anyMatch(capture -> capture.targetPosition().equals(targetPosition));

        assertThat(found)
                .as("Une option de capture post-mouvement devrait être disponible sur la position %s", targetPosition)
                .isTrue();

        return this;
    }
}
