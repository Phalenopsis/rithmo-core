package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.option.*;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.List;
import java.util.UUID;

public class FindOptionHelper {
    // Helper pour simplifier la lecture
    public static  <T extends PlayerOptionDTO> UUID findOptionIdByType(GameStatusDTO status, Class<T> clazz) {
        return status.possibleOptions().stream()
                .filter(clazz::isInstance)
                .map(opt -> {
                    return switch (opt) {
                        case MoveOptionDTO m -> m.id();
                        case SkipOptionDTO s -> s.id();
                        case PostCaptureOptionDTO p -> p.id();
                        default ->
                                throw new UnsupportedOperationException("L'ID doit être extrait manuellement pour ce type");
                    };
                })
                .findFirst().orElseThrow();
    }

    /**
     * Helper pour simuler le choix de l'UI.
     * Pour une PreCapture, l'ID jouable est dans LandingChoiceDTO.
     * Pour les autres, il est à la racine du record.
     */
    public static UUID extractPlayableId(PlayerOptionDTO dto) {
        if (dto instanceof PreCaptureOptionDTO pre) {
            return pre.choices().getFirst().actionId(); // On prend le premier atterrissage possible
        } else if (dto instanceof MoveOptionDTO move) {
            return move.id();
        } else if (dto instanceof PostCaptureOptionDTO post) {
            return post.id();
        } else if (dto instanceof SkipOptionDTO skip) {
            return skip.id();
        }
        throw new IllegalArgumentException("Type d'option inconnu : " + dto.getClass());
    }

    public static UUID findMoveIdByDestination(GameStatusDTO status, Position to) {
        return status.possibleOptions().stream()
                .filter(MoveOptionDTO.class::isInstance)
                .map(MoveOptionDTO.class::cast)
                .filter(m -> m.to().equals(to))
                .map(MoveOptionDTO::id)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Move vers " + to + " non trouvé"));
    }

    public static <T extends PlayerOptionDTO> List<T> findPreCaptureOptions(GameStatusDTO status, Class<T> clazz) {
        return status.possibleOptions().stream()
                .filter(clazz::isInstance)
                .map(clazz::cast)
                .toList();
    }
}
