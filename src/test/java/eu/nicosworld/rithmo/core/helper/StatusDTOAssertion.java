package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceShape;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.CaptureOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PreCaptureOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.ReintroductionOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.SkipOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.status.CaptureTypeDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PhaseDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.assertions.*;
import eu.nicosworld.rithmo.engine.model.Position;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusDTOAssertion {

    private final GameStatusDTO actual;

    private StatusDTOAssertion(GameStatusDTO actual) {
        this.actual = actual;
    }

    public static StatusDTOAssertion from(GameStatusDTO statusDTO) {
        return new StatusDTOAssertion(statusDTO);
    }

    public DecisionAssertions decisions() {
        return new DecisionAssertions(actual, this);
    }

    public OptionAssertions options() {
        return new OptionAssertions(actual, this);
    }

    public AssetAssertions assets() {
        return new AssetAssertions(actual, this);
    }

    public BoardAssertions board() {
        return new BoardAssertions(actual, this);
    }

    public GlobalAssertions global() {
        return new GlobalAssertions(actual, this);
    }


    public StatusDTOAssertion hasActivePlayer(PlayerColorDTO colorDTO) {
        assertThat(actual.currentPlayer())
                .isEqualTo(colorDTO);

        return this;
    }

    public StatusDTOAssertion isInPreCapturePhase() {

        assertThat(actual.phase())
                .isEqualTo(PhaseDTO.PRE_CAPTURE);

        return this;
    }

    public StatusDTOAssertion isInPostCapturePhase() {

        assertThat(actual.phase())
                .isEqualTo(PhaseDTO.POST_CAPTURE);

        return this;
    }

    public StatusDTOAssertion isInMovePhase() {

        assertThat(actual.phase())
                .isEqualTo(PhaseDTO.MOVE);

        return this;
    }

    public StatusDTOAssertion dontHaveSkipDecision() {

        assertThat(
                actual.possibleDecisions()
                        .stream()
                        .filter(DecisionDTO::skip)
                        .toList()
        ).isEmpty();

        return this;
    }

    public StatusDTOAssertion haveSkipDecision() {

        assertThat(
                actual.possibleDecisions()
                        .stream()
                        .filter(DecisionDTO::skip)
                        .toList()
        ).isNotEmpty();

        return this;
    }

    public StatusDTOAssertion haveAllDecisionsWithActor(String actorRepresentation) {

        PieceDTO actor = PieceRepresentationHelper.findPieceOrComponent(
                actual,
                actorRepresentation
        );

        assertThat(
                actual.possibleDecisions()
                        .stream()
                        .allMatch(d -> actor.id().equals(d.actorId()))
        ).isTrue();

        return this;
    }

    /**
     * Count only capture decisions.
     * No skip, move or reintroduction decisions.
     */
    public StatusDTOAssertion hasCaptureDecisionCount(int n) {

        long actual = this.actual.possibleDecisions()
                .stream()
                .filter(d -> !d.skip())
                .filter(d ->
                        d.capturedIdList() != null
                                && !d.capturedIdList().isEmpty()
                )
                .count();

        assertThat(actual)
                .isEqualTo(n);

        return this;
    }

    public StatusDTOAssertion canCaptureInOneDecision(
            String... pieceRepresentations
    ) {

        List<String> expected = Arrays.stream(pieceRepresentations)
                .sorted()
                .toList();

        boolean found = actual.possibleDecisions()
                .stream()
                .filter(d ->
                        d.capturedIdList() != null
                                && !d.capturedIdList().isEmpty()
                )
                .anyMatch(decision -> {

                    List<String> captured = decision.capturedIdList()
                            .stream()
                            .map(this::findPieceRepresentationById)
                            .filter(Objects::nonNull)
                            .sorted()
                            .toList();

                    return captured.equals(expected);
                });

        if (!found) {

            throw new AssertionError(String.format(
                    """
                    Aucune décision ne permet de capturer exactement :

                    %s

                    Décisions possibles :
                    %s
                    """,
                    expected,
                    formatPossibleDecisionsForError()
            ));
        }

        return this;
    }

    private String formatPossibleDecisionsForError() {

        return actual.possibleDecisions()
                .stream()
                .filter(d -> !d.skip())
                .map(d -> {

                    if (d.capturedIdList() == null) {
                        return "";
                    }

                    return d.capturedIdList()
                            .stream()
                            .map(this::findPieceRepresentationById)
                            .collect(Collectors.joining(", "));
                })
                .filter(s -> !s.isBlank())
                .collect(Collectors.joining(" | "));
    }

    public StatusDTOAssertion havePyramidComposedBy(
            PlayerColorDTO color,
            String... expectedComponents
    ) {

        PieceDTO pyramid = actual.board().pieces()
                .stream()
                .filter(p -> p.owner().equals(color))
                .filter(p -> p.shape().equals(PieceShape.PYRAMID))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Aucune pyramide trouvée pour : " + color
                        )
                );

        List<String> actual = pyramid.components()
                .stream()
                .map(PieceRepresentationHelper::toShortRepresentation)
                .sorted()
                .toList();

        List<String> expected = Arrays.stream(expectedComponents)
                .sorted()
                .toList();

        if (!actual.equals(expected)) {

            throw new AssertionError(String.format(
                    """
                    La pyramide %s n'a pas la composition attendue.

                    Attendus : %s
                    Actuels  : %s
                    """,
                    color,
                    expected,
                    actual
            ));
        }

        return this;
    }

    public StatusDTOAssertion hasStrictMoveDecisionTo(
            String... expectedLandingPositions
    ) {
        return hasMoveDecisionTo(true, expectedLandingPositions);
    }

    public StatusDTOAssertion hasMoveDecisionTo(
            String... expectedLandingPositions
    ) {
        return hasMoveDecisionTo(false, expectedLandingPositions);
    }

    public StatusDTOAssertion hasMoveDecisionTo(
            boolean strict,
            String... expectedLandingPositions
    ) {

        Set<String> actualLandings = actual.possibleDecisions()
                .stream()
                .map(DecisionDTO::landing)
                .filter(Objects::nonNull)
                .map(Position::toString)
                .map(this::normalize)
                .collect(Collectors.toSet());

        Set<String> expectedLandings = Arrays.stream(expectedLandingPositions)
                .map(this::normalize)
                .collect(Collectors.toSet());

        Set<String> missing = new HashSet<>(expectedLandings);
        missing.removeAll(actualLandings);

        if (!missing.isEmpty()) {

            throw new AssertionError(String.format(
                    """
                    Certaines destinations de mouvement sont manquantes.

                    Manquantes : %s
                    Disponibles : %s
                    """,
                    missing,
                    actualLandings
            ));
        }

        if (strict) {

            Set<String> extras = new HashSet<>(actualLandings);
            extras.removeAll(expectedLandings);

            if (!extras.isEmpty()) {

                throw new AssertionError(String.format(
                        """
                        Mode strict : destinations inattendues.

                        En trop : %s
                        """,
                        extras
                ));
            }
        }

        return this;
    }

    public StatusDTOAssertion havePyramidValue(
            PlayerColorDTO color,
            int expectedValue
    ) {

        PieceDTO pyramid = actual.board().pieces()
                .stream()
                .filter(p -> p.owner().equals(color))
                .filter(p -> p.shape().equals(PieceShape.PYRAMID))
                .findFirst()
                .orElseThrow(() ->
                        new AssertionError(
                                "Aucune pyramide trouvée pour " + color
                        )
                );

        int actualValue = pyramid.value();

        if (actualValue != expectedValue) {

            throw new AssertionError(String.format(
                    """
                    Valeur incorrecte pour la pyramide %s

                    Attendue : %d
                    Actuelle : %d
                    """,
                    color,
                    expectedValue,
                    actualValue
            ));
        }

        return this;
    }

    public StatusDTOAssertion hasReintroductionOptionsForActivePlayer() {

        PlayerColorDTO color = actual.currentPlayer();

        boolean exists = actual.possibleOptions()
                .values()
                .stream()
                .flatMap(Set::stream)
                .anyMatch(option ->
                        option instanceof ReintroductionOptionDTO ro
                                && ro.pieceDTO().owner().equals(color)
                );

        if (!exists) {

            throw new AssertionError(
                    "Aucune option de réintroduction pour " + color
            );
        }

        return this;
    }

    public StatusDTOAssertion allReintroductionOptionsComeFromReserve() {

        Map<String, Set<String>> reserveByPlayer =
                actual.assets()
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                e -> e.getKey().name(),
                                e -> e.getValue()
                                        .reserve()
                                        .stream()
                                        .map(PieceDTO::id)
                                        .collect(Collectors.toSet())
                        ));

        actual.possibleOptions()
                .values()
                .stream()
                .flatMap(Set::stream)
                .filter(ReintroductionOptionDTO.class::isInstance)
                .map(ReintroductionOptionDTO.class::cast)
                .forEach(option -> {

                    String owner = option.pieceDTO().owner().name();
                    String pieceId = option.pieceDTO().id();

                    Set<String> reserveIds = reserveByPlayer.get(owner);

                    if (reserveIds == null || !reserveIds.contains(pieceId)) {

                        throw new AssertionError(
                                "Réintroduction invalide : "
                                        + PieceRepresentationHelper.toRepresentation(
                                        option.pieceDTO()
                                )
                        );
                    }
                });

        return this;
    }

    public StatusDTOAssertion hasNoReintroductionOptions() {

        boolean exists = actual.possibleOptions()
                .values()
                .stream()
                .flatMap(Set::stream)
                .anyMatch(ReintroductionOptionDTO.class::isInstance);

        if (exists) {

            throw new AssertionError("""
                    Des options de réintroduction sont présentes.
                    """);
        }

        return this;
    }

    public StatusDTOAssertion hasPiece(
            String expectedRepresentation,
            String expectedPosition
    ) {

        String expected =
                expectedRepresentation
                        + normalize(expectedPosition);

        boolean found = actual.board().pieces()
                .stream()
                .map(PieceRepresentationHelper::toRepresentation)
                .anyMatch(expected::equals);

        if (!found) {

            throw new AssertionError(String.format(
                    """
                    Aucune pièce trouvée.

                    Attendue : %s
                    """,
                    expected
            ));
        }

        return this;
    }

    public StatusDTOAssertion reserveDoesNotContain(
            String pieceRepresentation
    ) {

        boolean found = actual.assets()
                .values()
                .stream()
                .flatMap(a -> a.reserve().stream())
                .map(PieceRepresentationHelper::toShortRepresentation)
                .anyMatch(pieceRepresentation::equals);

        if (found) {

            throw new AssertionError(
                    "La réserve contient encore : "
                            + pieceRepresentation
            );
        }

        return this;
    }

    public StatusDTOAssertion capturedContains(
            String... expectedRepresentations
    ) {

        List<String> actual = this.actual.assets()
                .values()
                .stream()
                .flatMap(a -> a.captured().stream())
                .map(PieceRepresentationHelper::toShortRepresentation)
                .toList();

        List<String> missing = Arrays.stream(expectedRepresentations)
                .filter(expected -> !actual.contains(expected))
                .toList();

        if (!missing.isEmpty()) {

            throw new AssertionError(String.format(
                    """
                    Certaines pièces manquent dans les captures.

                    Manquantes : %s
                    Capturées  : %s
                    """,
                    missing,
                    actual
            ));
        }

        return this;
    }

    public StatusDTOAssertion hasInReserve(
            String... expectedRepresentations
    ) {

        List<String> actual = this.actual.assets()
                .values()
                .stream()
                .flatMap(a -> a.reserve().stream())
                .map(PieceRepresentationHelper::toShortRepresentation)
                .toList();

        List<String> missing = Arrays.stream(expectedRepresentations)
                .filter(expected -> !actual.contains(expected))
                .toList();

        if (!missing.isEmpty()) {

            throw new AssertionError(String.format(
                    """
                    Certaines pièces manquent dans la réserve.

                    Manquantes : %s
                    Réserve    : %s
                    """,
                    missing,
                    actual
            ));
        }

        return this;
    }

    public StatusDTOAssertion hasNOptions(int n) {

        int actual = this.actual.possibleOptions()
                .values()
                .stream()
                .mapToInt(Set::size)
                .sum();

        assertThat(actual)
                .isEqualTo(n);

        return this;
    }

    public StatusDTOAssertion hasNDecisions(int n) {

        assertThat(actual.possibleDecisions())
                .hasSize(n);

        return this;
    }

    public StatusDTOAssertion hasOnlyMoveDecisions() {

        assertThat(actual.possibleDecisions())
                .allMatch(d ->
                        !d.skip()
                                && d.capturedIdList().isEmpty()
                                && d.landing() != null
                );

        return this;
    }

    public StatusDTOAssertion hasNOptionsFor(
            String pieceRepresentation,
            int n
    ) {

        PieceDTO piece = PieceRepresentationHelper.findPieceOrComponent(
                actual,
                pieceRepresentation
        );

        long actualCount = actual.possibleOptions()
                .getOrDefault(piece, Collections.emptySet())
                .stream()
                .filter(option -> !(option instanceof SkipOptionDTO))
                .count();

        if (actualCount != n) {

            throw new AssertionError(String.format(
                    """
                    Nombre d'options incorrect pour %s

                    Attendu : %d
                    Actuel  : %d
                    """,
                    pieceRepresentation,
                    n,
                    actualCount
            ));
        }

        return this;
    }

    public StatusDTOAssertion hasNDecisionsFor(
            String pieceRepresentation,
            int n
    ) {

        PieceDTO piece = PieceRepresentationHelper.findPieceOrComponent(
                actual,
                pieceRepresentation
        );

        long actualCount = actual.possibleDecisions()
                .stream()
                .filter(d -> !d.skip())
                .filter(d -> piece.id().equals(d.actorId()))
                .count();

        if (actualCount != n) {

            throw new AssertionError(String.format(
                    """
                    Nombre de décisions incorrect pour %s

                    Attendu : %d
                    Actuel  : %d
                    """,
                    pieceRepresentation,
                    n,
                    actualCount
            ));
        }

        return this;
    }

    private String findPieceRepresentationById(String id) {

        return actual.board().pieces()
                .stream()
                .flatMap(piece -> {

                    List<PieceDTO> all = new ArrayList<>();
                    all.add(piece);

                    if (piece.shape() == PieceShape.PYRAMID) {
                        all.addAll(piece.components());
                    }

                    return all.stream();
                })
                .filter(piece -> id.equals(piece.id()))
                .map(PieceRepresentationHelper::toRepresentation)
                .findFirst()
                .orElse(null);
    }

    private String normalize(String value) {
        return value.replace(" ", "");
    }

    public StatusDTOAssertion hasCaptureSourcesFor(
            String targetRepresentation,
            String... expectedActors
    ) {

        String targetId = PieceRepresentationHelper.findId(actual, targetRepresentation);

        Set<String> actualActors = actual.possibleDecisions().stream()
                .filter(d -> !d.skip())
                .filter(d -> d.capturedIdList() != null
                        && d.capturedIdList().contains(targetId))
                .map(DecisionDTO::actorId)
                .collect(Collectors.toSet());

        Set<String> expected = Arrays.stream(expectedActors)
                .map(rep -> PieceRepresentationHelper.findId(actual, rep))
                .collect(Collectors.toSet());

        if (!actualActors.equals(expected)) {
            throw new AssertionError(String.format(
                    """
                    Sources de capture incorrectes pour %s
    
                    Attendus : %s
                    Actuels  : %s
                    """,
                    targetRepresentation,
                    Arrays.toString(expectedActors),
                    actualActors
            ));
        }

        return this;
    }

    public StatusDTOAssertion hasCaptureCiblesFor(
            String actorRepresentation,
            String... expectedTargets
    ) {

        String actorId = PieceRepresentationHelper.findId(actual, actorRepresentation);

        Set<String> actualTargets = actual.possibleDecisions().stream()
                .filter(d -> !d.skip())
                .filter(d -> actorId.equals(d.actorId()))
                .flatMap(d -> d.capturedIdList().stream())
                .collect(Collectors.toSet());

        Set<String> expected = Arrays.stream(expectedTargets)
                .map(rep -> PieceRepresentationHelper.findId(actual, rep))
                .collect(Collectors.toSet());

        if (!actualTargets.equals(expected)) {
            throw new AssertionError(String.format(
                    """
                    Cibles de capture incorrectes pour %s
    
                    Attendus : %s
                    Actuels  : %s
                    """,
                    actorRepresentation,
                    Arrays.toString(expectedTargets),
                    actualTargets
            ));
        }

        return this;
    }

    public StatusDTOAssertion cannotCaptureWith(
            String actorRepresentation,
            String targetRepresentation
    ) {

        String actorId = PieceRepresentationHelper.findId(actual, actorRepresentation);
        String targetId = PieceRepresentationHelper.findId(actual, targetRepresentation);

        boolean exists = actual.possibleDecisions().stream()
                .filter(d -> !d.skip())
                .filter(d -> actorId.equals(d.actorId()))
                .anyMatch(d ->
                        d.capturedIdList() != null
                                && d.capturedIdList().contains(targetId)
                );

        if (exists) {
            throw new AssertionError(String.format(
                    """
                    Capture interdite détectée
    
                    Actor : %s
                    Target: %s
    
                    Une décision existe alors qu'elle ne devrait pas.
                    """,
                    actorRepresentation,
                    targetRepresentation
            ));
        }

        return this;
    }

    private <T> StatusDTOAssertion checkOption(
            String actorRepresentation,
            Class<T> optionClass,
            Predicate<T> matcher
    ) {
        PieceDTO pieceDTO = PieceRepresentationHelper.findPieceOrComponent(
                actual,
                actorRepresentation
        );

        boolean exists = actual.possibleOptions().get(pieceDTO)
                .stream()
                .filter(optionClass::isInstance)
                .map(optionClass::cast)
                .anyMatch(matcher);

        if (!exists) {
            throw new AssertionError("Option non trouvée");
        }

        return this;
    }

    private StatusDTOAssertion canPreCaptureWithBy(String actorRepresentation, String targetRepresentation, CaptureTypeDTO captureTypeDTO) {
        String targetId = PieceRepresentationHelper.findId(actual, targetRepresentation);
        return checkOption(actorRepresentation,
                PreCaptureOptionDTO.class,
                o -> o.target().id().equals(targetId) && o.type().equals(captureTypeDTO));
    }

    private StatusDTOAssertion canPostCaptureWithBy(String actorRepresentation, String targetRepresentation, CaptureTypeDTO captureTypeDTO) {
        String targetId = PieceRepresentationHelper.findId(actual, targetRepresentation);
        return checkOption(actorRepresentation,
                CaptureOptionDTO.class,
                o -> o.target().id().equals(targetId) && o.type().equals(captureTypeDTO));
    }

    public StatusDTOAssertion canPostCaptureWithByEncounter(String actorRepresentation, String targetRepresentation) {
       return canPostCaptureWithBy(actorRepresentation, targetRepresentation, CaptureTypeDTO.ENCOUNTER);
    }

    public StatusDTOAssertion canPostCaptureWithByAssault(String actorRepresentation, String targetRepresentation) {
        return canPostCaptureWithBy(actorRepresentation, targetRepresentation, CaptureTypeDTO.ASSAULT);
    }

    public StatusDTOAssertion canPostCaptureWithByPower(String actorRepresentation, String targetRepresentation) {
        return canPostCaptureWithBy(actorRepresentation, targetRepresentation, CaptureTypeDTO.POWER);
    }

    public StatusDTOAssertion canPostCaptureWithByAmbush(String actorRepresentation, String targetRepresentation) {
        return canPostCaptureWithBy(actorRepresentation, targetRepresentation, CaptureTypeDTO.AMBUSH);
    }

    public StatusDTOAssertion canPreCaptureWithByEncounter(String actorRepresentation, String targetRepresentation) {
        return canPreCaptureWithBy(actorRepresentation, targetRepresentation, CaptureTypeDTO.ENCOUNTER);
    }

    public StatusDTOAssertion canPreCaptureWithByAssault(String actorRepresentation, String targetRepresentation) {
        return canPreCaptureWithBy(actorRepresentation, targetRepresentation, CaptureTypeDTO.ASSAULT);
    }

    public StatusDTOAssertion canPreCaptureWithByPower(String actorRepresentation, String targetRepresentation) {
        return canPreCaptureWithBy(actorRepresentation, targetRepresentation, CaptureTypeDTO.POWER);
    }

    public StatusDTOAssertion canPreCaptureWithByAmbush(String actorRepresentation, String targetRepresentation) {
        return canPreCaptureWithBy(actorRepresentation, targetRepresentation, CaptureTypeDTO.AMBUSH);
    }
}