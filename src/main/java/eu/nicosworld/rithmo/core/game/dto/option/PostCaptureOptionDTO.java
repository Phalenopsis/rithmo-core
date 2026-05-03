package eu.nicosworld.rithmo.core.game.dto.option;

import eu.nicosworld.rithmo.core.turn.option.PostCaptureOption;
import eu.nicosworld.rithmo.engine.capture.model.CaptureAction;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;
import java.util.UUID;

public record PostCaptureOptionDTO(
        UUID id,
        Position attackerPos,
        List<Position> targets
) implements PlayerOptionDTO {
    public static PostCaptureOptionDTO from(PostCaptureOption option, UUID id) {
        return new PostCaptureOptionDTO(
                id,
                option.captures().getFirst().actor().position(),
                option.captures().stream().map(CaptureAction::targetPosition).toList()
        );
    }
}
