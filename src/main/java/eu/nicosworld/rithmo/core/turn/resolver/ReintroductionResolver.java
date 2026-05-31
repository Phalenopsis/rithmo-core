package eu.nicosworld.rithmo.core.turn.resolver;

import eu.nicosworld.rithmo.engine.model.GameState;
import eu.nicosworld.rithmo.engine.reintroduction.Reintroduction;
import eu.nicosworld.rithmo.engine.reintroduction.ReintroductionEngine;
import java.util.List;

public class ReintroductionResolver {
    ReintroductionEngine reintroductionEngine;

    public ReintroductionResolver(ReintroductionEngine reintroductionEngine) {
        this.reintroductionEngine = reintroductionEngine;
    }

    public List<Reintroduction> resolveReintroductions(GameState gameState) {
        return reintroductionEngine.generateReintroduction(gameState);
    }
}
