package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceShape;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.ReintroductionOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PhaseDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusDTOAssertion {
    private GameStatusDTO statusDTO;

    private StatusDTOAssertion(GameStatusDTO statusDTO) {
        this.statusDTO = statusDTO;
    }

    public static StatusDTOAssertion from(GameStatusDTO statusDTO) {
        return new StatusDTOAssertion(statusDTO);
    }

    public StatusDTOAssertion hasActivePlayer(PlayerColorDTO colorDTO) {
        assertThat(statusDTO.currentPlayer())
                .isEqualTo(colorDTO);
        return this;
    }

    public StatusDTOAssertion isInPreCapturePhase() {
        assertThat(statusDTO.phase()).isEqualTo(PhaseDTO.PRE_CAPTURE);
        return this;
    }

    public StatusDTOAssertion isInPostCapturePhase() {
        assertThat(statusDTO.phase()).isEqualTo(PhaseDTO.POST_CAPTURE);
        return this;
    }

    public StatusDTOAssertion isInMovePhase() {
        assertThat(statusDTO.phase()).isEqualTo(PhaseDTO.MOVE);
        return this;
    }

    public StatusDTOAssertion dontHaveSkipOption() {
        List<DecisionDTO> dtoList = statusDTO.possibleDecisions()
                .stream()
                .filter(DecisionDTO::skip)
                .toList();

        assertThat(dtoList).isEmpty();
        return this;
    }

    public StatusDTOAssertion haveAllDecisionsWithActor(PieceDTO actor) {
        assertThat(statusDTO.possibleDecisions()
                .stream()
                .allMatch(d->d.actorId().equals(actor.id()))
        );

        return this;
    }

    public StatusDTOAssertion canCaptureInOneDecision(String... pieceRepresentations) {
        // 1. On crée la map de correspondance [ID -> Représentation]
        Map<String, String> idToRepresentation = new HashMap<>();

        for (PieceDTO piece : statusDTO.board().pieces()) {
            // Pièce simple
            idToRepresentation.put(piece.id(), TestDebugger.getStringRepresentation(piece));

            // Composants de pyramide
            if (piece.shape().equals(PieceShape.PYRAMID)) {
                for (PieceDTO component : piece.components()) {
                    // On préfixe par 'P' comme tu l'as défini
                    String rep = "P" + TestDebugger.getStringRepresentation(component);
                    idToRepresentation.put(component.id(), rep);
                }
            }
        }

        // 2. Préparation des attentes (on trie pour comparer des listes/sets)
        List<String> expectedRepresentations = Arrays.asList(pieceRepresentations);
        Collections.sort(expectedRepresentations);

        // 3. Parcours des décisions pour trouver le "Match"
        boolean found = statusDTO.possibleDecisions().stream().anyMatch(decision -> {
            if(Objects.isNull(decision.capturedIdList())) return false;
            // Pour chaque ID capturé dans cette décision, on récupère sa représentation
            List<String> currentDecisionReps = decision.capturedIdList().stream()
                    .map(idToRepresentation::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // Si la décision capture exactement le bon nombre de pièces
            if (currentDecisionReps.size() != expectedRepresentations.size()) {
                return false;
            }

            Collections.sort(currentDecisionReps);
            return currentDecisionReps.equals(expectedRepresentations);
        });

        // 4. Assertion
        if (!found) {
            throw new AssertionError(String.format(
                    "Aucune décision ne permet de capturer exactement : %s. Décisions possibles : %s",
                    Arrays.toString(pieceRepresentations),
                    formatPossibleDecisionsForError(idToRepresentation)
            ));
        }

        return this;
    }

    // Optionnel : Pour aider au debug en cas d'échec
    private String formatPossibleDecisionsForError(Map<String, String> idToRep) {
        return statusDTO.possibleDecisions().stream()
                .filter(d -> !d.skip())
                .map(d -> d.capturedIdList().stream().map(idToRep::get).collect(Collectors.joining(", ")))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(" | "));
    }

    public StatusDTOAssertion havePyramidComposedBy(PlayerColorDTO color, String... expectedComponents) {
        // 1. Trouver la pyramide de la couleur donnée sur le plateau
        PieceDTO pyramid = statusDTO.board().pieces().stream()
                .filter(p -> p.owner().equals(color))
                .filter(p -> p.shape().equals(PieceShape.PYRAMID))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Aucune pyramide trouvée pour la couleur : " + color));

        // 2. Extraire les représentations des composants actuels de cette pyramide
        List<String> actualComponentsReps = pyramid.components().stream()
                .map(TestDebugger::getStringRepresentation)
                .collect(Collectors.toList());

        // 3. Préparer les listes pour comparaison (tri pour s'affranchir de l'ordre)
        List<String> expectedList = Arrays.asList(expectedComponents);

        Collections.sort(actualComponentsReps);
        Collections.sort(expectedList);

        // 4. Assertion
        if (!actualComponentsReps.equals(expectedList)) {
            throw new AssertionError(String.format(
                    "La pyramide %s n'a pas la composition attendue.\nAttendus : %s\nActuels  : %s",
                    color,
                    expectedList,
                    actualComponentsReps
            ));
        }

        return this;
    }

    public StatusDTOAssertion hasStrictMoveDecisionTo(String... expectedLandingPositions) {
        return hasMoveDecisionTo(true, expectedLandingPositions);
    }

    public StatusDTOAssertion hasMoveDecisionTo(String... expectedLandingPositions) {
        return hasMoveDecisionTo(false, expectedLandingPositions);
    }

    public StatusDTOAssertion hasMoveDecisionTo(boolean isStrict, String... expectedLandingPositions) {
        // 1. On extrait tous les landings des décisions possibles sous forme de String
        // On filtre les nulls (car certaines décisions comme 'skip' n'ont pas de landing)
        Set<String> actualLandings = statusDTO.possibleDecisions().stream()
                .map(DecisionDTO::landing)
                .filter(Objects::nonNull)
                .map(Position::toString)
                .map(s->s.replace(" ", ""))
                .collect(Collectors.toSet());

        // 2. On vérifie pour chaque position attendue si elle est présente dans les décisions
        List<String> missingPositions = new ArrayList<>();
        for (String expected : expectedLandingPositions) {
            String normalizedExpected = expected.replace(" ", "");
            if (!actualLandings.contains(normalizedExpected)) {
                missingPositions.add(expected); // On garde l'original pour le message d'erreur
            }
        }

        // 3. Assertion
        if (!missingPositions.isEmpty()) {
            throw new AssertionError(String.format(
                    "Certaines destinations de mouvement sont manquantes.\n" +
                            "Attendues non trouvées : %s\n" +
                            "Landings réellement disponibles : %s",
                    missingPositions,
                    actualLandings
            ));
        }

        Set<String> normalizedExpectedSet = Arrays.stream(expectedLandingPositions)
                .map(s -> s.replace(" ", ""))
                .collect(Collectors.toSet());

        if (!actualLandings.equals(normalizedExpectedSet)) {
            // On calcule la différence pour un message d'erreur clair
            Set<String> extras = new HashSet<>(actualLandings);
            extras.removeAll(normalizedExpectedSet);

            throw new AssertionError(String.format(
                    "Mode Strict : Les destinations ne correspondent pas exactement.\n" +
                            "En trop dans le moteur : %s",
                    extras
            ));
        }

        return this;
    }

    public StatusDTOAssertion havePyramidValue(PlayerColorDTO color, int expectedValue) {
        // 1. On récupère la pyramide
        PieceDTO pyramid = statusDTO.board().pieces().stream()
                .filter(p -> p.owner().equals(color))
                .filter(p -> p.shape().equals(PieceShape.PYRAMID))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Aucune pyramide trouvée pour " + color));

        // 2. On calcule la valeur actuelle (somme de ses composants)
        int actualValue = pyramid.value();


        // 3. Assertion
        if (actualValue != expectedValue) {
            throw new AssertionError(String.format(
                    "Valeur de la pyramide %s incorrecte.\nAttendue : %d\nActuelle : %d (via %s)",
                    color,
                    expectedValue,
                    actualValue,
                    pyramid.components().stream()
                            .map(c -> String.valueOf(c.value()))
                            .collect(Collectors.joining("+"))
            ));
        }

        return this;
    }

    public StatusDTOAssertion hasReintroductionOptionsForActivePlayer() {
        PlayerColorDTO color = statusDTO.currentPlayer();
        boolean exists = statusDTO.possibleOptions().entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .anyMatch(option ->
                        option instanceof ReintroductionOptionDTO ro &&
                                ro.pieceDTO().owner().equals(color)
                );

        if (!exists) {
            throw new AssertionError("Aucune option de réintroduction pour " + color);
        }

        return this;
    }

    public StatusDTOAssertion allReintroductionOptionsComeFromReserve() {

        Map<String, Set<String>> reserveByPlayer = statusDTO.assets().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().name(),
                        e -> e.getValue().reserve().stream()
                                .map(PieceDTO::id)
                                .collect(Collectors.toSet())
                ));

        statusDTO.possibleOptions().entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .filter(ReintroductionOptionDTO.class::isInstance)
                .map(ReintroductionOptionDTO.class::cast)
                .forEach(option -> {

                    String owner = option.pieceDTO().owner().name();
                    String pieceId = option.pieceDTO().id();

                    Set<String> reserveIds = reserveByPlayer.get(owner);

                    if (reserveIds == null || !reserveIds.contains(pieceId)) {
                        throw new AssertionError(
                                "Réintroduction invalide : pièce non présente en réserve = "
                                        + option.pieceDTO()
                        );
                    }
                });

        return this;
    }

    public StatusDTOAssertion hasNoReintroductionOptions() {

        boolean exists = statusDTO.possibleOptions().entrySet().stream()
                .flatMap(e -> e.getValue().stream())
                .anyMatch(ReintroductionOptionDTO.class::isInstance);

        if (exists) {
            throw new AssertionError("""
                Réintroduction options présentes alors qu'elles ne devraient pas exister.
                Vérifie les assets du joueur et la phase MOVE_COMPUTATION.
                """);
        }

        return this;
    }

    public StatusDTOAssertion hasPiece(
            String expectedRepresentation,
            String expectedPosition
    ) {

        String normalizedPosition = expectedPosition.replace(" ", "");

        boolean found = statusDTO.board().pieces().stream()
                .anyMatch(piece ->
                        TestDebugger.getStringRepresentation(piece)
                                .equals(expectedRepresentation)
                                &&
                                Objects.nonNull(piece.position())
                                &&
                                piece.position().toString()
                                        .replace(" ", "")
                                        .equals(normalizedPosition)
                );

        if (!found) {
            throw new AssertionError(String.format(
                    "Aucune pièce '%s' trouvée à la position %s",
                    expectedRepresentation,
                    expectedPosition
            ));
        }

        return this;
    }

    public StatusDTOAssertion reserveDoesNotContain(
            String pieceRepresentation
    ) {

        boolean found = statusDTO.assets().values().stream()
                .flatMap(assets -> assets.reserve().stream())
                .anyMatch(piece ->
                        TestDebugger.getStringRepresentation(piece)
                                .equals(pieceRepresentation)
                );

        if (found) {
            throw new AssertionError(String.format(
                    "La réserve contient encore la pièce : %s",
                    pieceRepresentation
            ));
        }

        return this;
    }

    public StatusDTOAssertion capturedContains(String... expectedRepresentations) {

        List<String> actualCaptured = statusDTO.assets().values().stream()
                .flatMap(assets -> assets.captured().stream())
                .map(TestDebugger::getStringRepresentation)
                .toList();

        List<String> missing = Arrays.stream(expectedRepresentations)
                .filter(expected -> !actualCaptured.contains(expected))
                .toList();

        if (!missing.isEmpty()) {
            throw new AssertionError(String.format(
                    """
                    Certaines pièces attendues ne sont pas présentes dans les captures.
                    
                    Manquantes : %s
                    Capturées  : %s
                    """,
                    missing,
                    actualCaptured
            ));
        }

        return this;
    }

}
