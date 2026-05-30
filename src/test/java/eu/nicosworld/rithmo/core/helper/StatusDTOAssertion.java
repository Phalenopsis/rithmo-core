package eu.nicosworld.rithmo.core.helper;

import eu.nicosworld.rithmo.core.game.GameStatusDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceDTO;
import eu.nicosworld.rithmo.core.game.dto.board.PieceShape;
import eu.nicosworld.rithmo.core.game.dto.decision.DecisionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.CaptureOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.PreCaptureOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.option.ReintroductionOptionDTO;
import eu.nicosworld.rithmo.core.game.dto.status.CaptureTypeDTO;
import eu.nicosworld.rithmo.core.game.dto.status.PlayerColorDTO;
import eu.nicosworld.rithmo.core.helper.assertions.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class StatusDTOAssertion {

    private final GameStatusDTO actual;
    private final StatusAssertionSupport support;

    private final DecisionAssertions decisions;
    private final OptionAssertions options;
    private final AssetAssertions assets;
    private final BoardAssertions board;
    private final GlobalAssertions status;

    private StatusDTOAssertion(GameStatusDTO actual) {
        this.actual = actual;
        this.support = new StatusAssertionSupport(actual);

        this.decisions = new DecisionAssertions(actual, this);
        this.options = new OptionAssertions(actual, this);
        this.assets = new AssetAssertions(actual, this);
        this.board = new BoardAssertions(actual, this);
        this.status = new GlobalAssertions(actual, this);
    }

    public static StatusDTOAssertion from(GameStatusDTO statusDTO) {
        return new StatusDTOAssertion(statusDTO);
    }

    public DecisionAssertions decisions() {
        return decisions;
    }

    public OptionAssertions options() {
        return options;
    }

    public AssetAssertions assets() {
        return assets;
    }

    public BoardAssertions board() {
        return board;
    }

    public GlobalAssertions status() {
        return status;
    }

    public StatusAssertionSupport support() {
        return support;
    }

    @Deprecated(forRemoval = false)
    public StatusDTOAssertion hasActivePlayer(PlayerColorDTO colorDTO) {
        return status()
                .hasActivePlayer(colorDTO)
                .and();
    }

    @Deprecated(forRemoval = false)
    public StatusDTOAssertion isInPreCapturePhase() {
        return status()
                .isInPreCapturePhase()
                .and();
    }

    @Deprecated
    public StatusDTOAssertion isInPostCapturePhase() {
        return status()
                .isInPostCapturePhase()
                .and();
    }

    @Deprecated
    public StatusDTOAssertion isInMovePhase() {
        return status()
                .isInMovePhase()
                .and();
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

    @Deprecated
    public StatusDTOAssertion haveSkipDecision() {
        return decisions()
                .hasSkipDecision()
                .and();
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

    @Deprecated
    public StatusDTOAssertion hasCaptureDecisionCount(int n) {
        return decisions()
                .hasCaptureDecisionCount(n)
                .and();
    }

    @Deprecated
    public StatusDTOAssertion canCaptureInOneDecision(
            String... pieceRepresentations
    ) {
        return decisions()
                .canCaptureInOneDecision(pieceRepresentations)
                .and();
    }

    @Deprecated
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

    @Deprecated
    public StatusDTOAssertion hasStrictMoveDecisionTo(
            String... expectedLandingPositions
    ) {
        return decisions()
                .hasStrictMoveDecisionTo(expectedLandingPositions)
                .and();
    }

    @Deprecated
    public StatusDTOAssertion hasMoveDecisionTo(
            String... expectedLandingPositions
    ) {
        return decisions()
                .hasMoveDecisionTo(expectedLandingPositions)
                .and();
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

    @Deprecated
    public StatusDTOAssertion hasNoReintroductionOptions() {
        return options().hasNoReintroductionOptions().and();
    }

    @Deprecated
    public StatusDTOAssertion hasPiece(
            String expectedRepresentation) {
        return board().hasPiece(expectedRepresentation).and();
    }

    @Deprecated
    public StatusDTOAssertion hasPiece(
            String expectedRepresentation,
            String expectedPosition
    ) {

        String expected =
                expectedRepresentation
                        + normalize(expectedPosition);

        return hasPiece(expected);
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

    @Deprecated
    public StatusDTOAssertion capturedContains(
            String... expectedRepresentations
    ) {
        return assets()
                .capturedContains(expectedRepresentations)
                .and();
    }

    @Deprecated
    public StatusDTOAssertion hasInReserve(
            String... expectedRepresentations
    ) {
        return assets()
                .hasInReserve(expectedRepresentations)
                .and();
    }

    @Deprecated
    public StatusDTOAssertion hasNOptions(int n) {
        return options()
                .hasOptionCount(n)
                .and();
    }

    @Deprecated
    public StatusDTOAssertion hasNDecisions(int n) {
        return decisions()
                .hasDecisionCount(n)
                .and();
    }

    @Deprecated
    public StatusDTOAssertion hasOnlyMoveDecisions() {
        return decisions()
                .hasOnlyMoveDecisions()
                .and();
    }

    @Deprecated
    public StatusDTOAssertion hasNOptionsFor(
            String pieceRepresentation,
            int n
    ) {
        return options()
                .hasOptionCountFor(pieceRepresentation, n)
                .and();
    }

    @Deprecated(forRemoval = false)
    public StatusDTOAssertion hasNDecisionsFor(
            String pieceRepresentation,
            int n
    ) {
        return decisions()
                .hasDecisionCountFor(pieceRepresentation, n)
                .and();
    }

    @Deprecated
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

    @Deprecated
    private String normalize(String value) {
        return value.replace(" ", "");
    }

    @Deprecated
    public StatusDTOAssertion hasCaptureSourcesFor(
            String targetRepresentation,
            String... expectedActors
    ) {
        return decisions()
                .hasCaptureSourcesFor(targetRepresentation,expectedActors)
                .and();
    }

    @Deprecated
    public StatusDTOAssertion hasCaptureCiblesFor(
            String actorRepresentation,
            String... expectedTargets
    ) {
        return decisions()
                .hasCaptureCiblesFor(actorRepresentation, expectedTargets)
                .and();
    }

    @Deprecated
    public StatusDTOAssertion cannotCaptureWith(
            String actorRepresentation,
            String targetRepresentation
    ) {
        return decisions()
                .cannotCaptureWith(actorRepresentation, targetRepresentation)
                .and();
    }

    public StatusDTOAssertion canPostCaptureWithByEncounter(String actorRepresentation, String... targetRepresentation) {
       return options()
               .canPostCaptureWithByEncounter(actorRepresentation, targetRepresentation)
               .and();
    }

    public StatusDTOAssertion canPostCaptureWithByAssault(String actorRepresentation, String... targetRepresentation) {
        return options()
                .canPostCaptureWithByAssault(actorRepresentation, targetRepresentation)
                .and();
    }

    public StatusDTOAssertion canPostCaptureWithByPower(String actorRepresentation, String... targetRepresentation) {
        return options()
                .canPostCaptureWithByPower(actorRepresentation, targetRepresentation)
                .and();
    }

    public StatusDTOAssertion canPostCaptureWithByAmbush(String actorRepresentation, String... targetRepresentation) {
        return options
                .canPostCaptureWithByAmbush(actorRepresentation, targetRepresentation)
                .and();
    }

    public StatusDTOAssertion canPreCaptureWithByEncounter(String actorRepresentation, String... targetRepresentation) {
        return options
                .canPreCaptureWithByEncounter(actorRepresentation, targetRepresentation)
                .and();
    }

    public StatusDTOAssertion canPreCaptureWithByAssault(String actorRepresentation, String... targetRepresentation) {
        return options
                .canPreCaptureWithByAssault(actorRepresentation, targetRepresentation)
                .and();
    }

    public StatusDTOAssertion canPreCaptureWithByPower(String actorRepresentation, String... targetRepresentation) {
        return options
                .canPreCaptureWithByPower(actorRepresentation, targetRepresentation)
                .and();
    }

    public StatusDTOAssertion canPreCaptureWithByAmbush(String actorRepresentation, String... targetRepresentation) {
        return options
                .canPreCaptureWithByAmbush(actorRepresentation, targetRepresentation)
                .and();
    }
}