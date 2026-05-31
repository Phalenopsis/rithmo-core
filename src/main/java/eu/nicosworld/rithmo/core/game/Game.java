package eu.nicosworld.rithmo.core.game;

import eu.nicosworld.rithmo.core.GameOptions;
import eu.nicosworld.rithmo.core.turn.TurnState;
import java.util.UUID;

public class Game {
    private final UUID id;
    private final GameOptions options;
    private final TurnState currentState;

    public Game(GameOptions options, TurnState currentState) {
        this(UUID.randomUUID(), options, currentState);
    }

    public Game(UUID id, GameOptions options, TurnState currentState) {
        this.id = id;
        this.options = options;
        this.currentState = currentState;
    }

    public UUID getId() {
        return id;
    }

    public GameOptions getOptions() {
        return options;
    }

    public TurnState getCurrentState() {
        return currentState;
    }

    public static Game from(Game oldGame, TurnState currentState) {
        return new Game(oldGame.id, oldGame.getOptions(), currentState);
    }
}
