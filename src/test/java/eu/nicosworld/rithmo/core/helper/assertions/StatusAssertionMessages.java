package eu.nicosworld.rithmo.core.helper.assertions;

import java.util.List;
import java.util.Set;

final class StatusAssertionMessages {

    private StatusAssertionMessages() {}

    static String pieceNotFound(String pieceRepresentation) {
        return """
                Aucune pièce trouvée.

                Attendue : %s
                """.formatted(pieceRepresentation);
    }

    static String incorrectOptionCount(
            String pieceRepresentation,
            int expected,
            long actual
    ) {
        return """
                Nombre d'options incorrect pour %s

                Attendu : %d
                Actuel  : %d
                """
                .formatted(pieceRepresentation, expected, actual);
    }

    static String incorrectDecisionCount(
            String pieceRepresentation,
            int expected,
            long actual
    ) {
        return """
                Nombre de décisions incorrect pour %s

                Attendu : %d
                Actuel  : %d
                """
                .formatted(pieceRepresentation, expected, actual);
    }

    static String missingMoveDestinations(
            Set<String> missing,
            Set<String> available
    ) {
        return """
                Certaines destinations de mouvement sont manquantes.

                Manquantes : %s
                Disponibles : %s
                """.formatted(
                        missing,
                        available);
    }

    static String unexpectedMoveDestinations(Set<String> unexpected) {
        return """
                Mode strict : destinations inattendues.

                En trop : %s
                """.formatted(unexpected);
    }

    static String unexpectedReintroductionOptions() {
        return """
               Des options de réintroduction sont présentes.
               """;
    }

    static String missingInCaptures(List<String> missing, List<String> captured) {
        return """
                Certaines pièces manquent dans les captures.

                Manquantes : %s
                Capturées  : %s
                """.formatted(
                        missing,
                        captured);
    }

    static String missingInReserve(List<String> missing, List<String> reserve) {
        return """
                Certaines pièces manquent dans la réserve.

                Manquantes : %s
                Réserve    : %s
                """.formatted(
                        missing,
                        reserve);
    }

    static String noMatchCapture(List<String> expected, String possiblesDecisions) {
        return """
                Aucune décision ne permet de capturer exactement :

                %s

                Décisions possibles :
                %s
                """.formatted(
                    expected,
                    possiblesDecisions);
    }

    static String incorrectCaptureTargets(String actorRepresentation, String expectedTargets, Set<String> actualTargets) {
        return """
               Cibles de capture incorrectes pour %s

               Attendus : %s
               Actuels  : %s
               """.formatted(
                actorRepresentation,
                expectedTargets,
                actualTargets
        );
    }

    static String incorrectCaptureSources(String targetRepresentation, String expectedActors, Set<String> actualActors) {
        return """
               Sources de capture incorrectes pour %s

               Attendus : %s
               Actuels  : %s
               """.formatted(
                targetRepresentation,
                expectedActors,
                actualActors
        );
    }

    static String forbiddenCapture(String actorRepresentation, String targetRepresentation) {
        return """
                Capture interdite détectée

                Actor : %s
                Target: %s

                Une décision existe alors qu'elle ne devrait pas.
                """.formatted(
                actorRepresentation,
                targetRepresentation);
    }

    static String optionNotFound(String actorId, String targetId) {
        return "Option non trouvée pour l'acteur " + actorId + " vers la cible " + targetId;
    }
}
