package eu.nicosworld.rithmo.core;

import eu.nicosworld.rithmo.engine.model.Position;
import java.util.Set;

public record DecisionKey(
    String actorId, Set<String> capturedIdList, Position landing, boolean skip) {}
