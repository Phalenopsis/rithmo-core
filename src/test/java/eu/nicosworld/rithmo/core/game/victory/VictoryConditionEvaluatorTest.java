package eu.nicosworld.rithmo.core.game.victory;

import static org.assertj.core.api.Assertions.assertThat;

import eu.nicosworld.rithmo.engine.model.Player;
import eu.nicosworld.rithmo.engine.model.victory.BodyVictory;
import eu.nicosworld.rithmo.engine.model.victory.GoodsVictory;
import eu.nicosworld.rithmo.engine.model.victory.LawsuitVictory;
import eu.nicosworld.rithmo.engine.model.victory.Victory;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class VictoryConditionEvaluatorTest {
  @ParameterizedTest
  @MethodSource("casesWithMultiplesConditions")
  void testEvaluate_multipleConditions(
      Set<VictoryCondition> conditions, List<Victory> victories, boolean expected) {
    VictoryConditionEvaluator evaluator = new VictoryConditionEvaluator(conditions);
    boolean result = evaluator.isSatisfied(victories);

    assertThat(result).isEqualTo(expected);
  }

  static Stream<Arguments> casesWithMultiplesConditions() {
    return Stream.of(

        // BODY

        Arguments.of(Set.of(VictoryCondition.BODY), List.of(body()), true),
        Arguments.of(Set.of(VictoryCondition.BODY), List.of(goods()), false),

        // GOODS

        Arguments.of(Set.of(VictoryCondition.GOODS), List.of(goods()), true),
        Arguments.of(Set.of(VictoryCondition.GOODS), List.of(body()), false),

        // LAWSUIT

        Arguments.of(Set.of(VictoryCondition.LAWSUIT), List.of(lawsuit()), true),
        Arguments.of(Set.of(VictoryCondition.LAWSUIT), List.of(body()), false),

        // BODY OR GOODS

        Arguments.of(Set.of(VictoryCondition.BODY, VictoryCondition.GOODS), List.of(body()), true),
        Arguments.of(Set.of(VictoryCondition.BODY, VictoryCondition.GOODS), List.of(goods()), true),
        Arguments.of(
            Set.of(VictoryCondition.BODY, VictoryCondition.GOODS), List.of(lawsuit()), false),

        // BODY OR GOODS OR LAWSUIT

        Arguments.of(
            Set.of(VictoryCondition.BODY, VictoryCondition.GOODS, VictoryCondition.LAWSUIT),
            List.of(body()),
            true),
        Arguments.of(
            Set.of(VictoryCondition.BODY, VictoryCondition.GOODS, VictoryCondition.LAWSUIT),
            List.of(goods()),
            true),
        Arguments.of(
            Set.of(VictoryCondition.BODY, VictoryCondition.GOODS, VictoryCondition.LAWSUIT),
            List.of(lawsuit()),
            true),
        Arguments.of(
            Set.of(VictoryCondition.BODY, VictoryCondition.GOODS, VictoryCondition.LAWSUIT),
            List.of(),
            false));
  }

  @ParameterizedTest
  @MethodSource("cases")
  void testEvaluate_condition(
      VictoryCondition condition, List<Victory> victories, boolean expected) {
    VictoryConditionEvaluator evaluator = new VictoryConditionEvaluator(Set.of(condition));
    boolean result = evaluator.isSatisfied(victories);

    assertThat(result).isEqualTo(expected);
  }

  static Stream<Arguments> cases() {
    return Stream.of(
        // BODY
        Arguments.of(VictoryCondition.BODY, List.of(body()), true),
        Arguments.of(VictoryCondition.BODY, List.of(goods()), false),
        Arguments.of(VictoryCondition.BODY, List.of(lawsuit()), false),

        // GOODS
        Arguments.of(VictoryCondition.GOODS, List.of(goods()), true),
        Arguments.of(VictoryCondition.GOODS, List.of(body()), false),
        Arguments.of(VictoryCondition.GOODS, List.of(lawsuit()), false),

        // LAWSUIT
        Arguments.of(VictoryCondition.LAWSUIT, List.of(lawsuit()), true),
        Arguments.of(VictoryCondition.LAWSUIT, List.of(body()), false),
        Arguments.of(VictoryCondition.LAWSUIT, List.of(goods()), false),

        // BODY_AND_GOODS
        Arguments.of(VictoryCondition.BODY_AND_GOODS, List.of(body()), false),
        Arguments.of(VictoryCondition.BODY_AND_GOODS, List.of(goods()), false),
        Arguments.of(VictoryCondition.BODY_AND_GOODS, List.of(body(), goods()), true),
        Arguments.of(VictoryCondition.BODY_AND_GOODS, List.of(body(), goods(), lawsuit()), true),

        // BODY_AND_LAWSUIT
        Arguments.of(VictoryCondition.BODY_AND_LAWSUIT, List.of(body()), false),
        Arguments.of(VictoryCondition.BODY_AND_LAWSUIT, List.of(lawsuit()), false),
        Arguments.of(VictoryCondition.BODY_AND_LAWSUIT, List.of(body(), lawsuit()), true),
        Arguments.of(VictoryCondition.BODY_AND_LAWSUIT, List.of(body(), goods(), lawsuit()), true),

        // GOODS_AND_LAWSUIT
        Arguments.of(VictoryCondition.GOODS_AND_LAWSUIT, List.of(goods()), false),
        Arguments.of(VictoryCondition.GOODS_AND_LAWSUIT, List.of(lawsuit()), false),
        Arguments.of(VictoryCondition.GOODS_AND_LAWSUIT, List.of(goods(), lawsuit()), true),
        Arguments.of(VictoryCondition.GOODS_AND_LAWSUIT, List.of(body(), goods(), lawsuit()), true),

        // BODY_GOODS_AND_LAWSUIT
        Arguments.of(VictoryCondition.BODY_AND_GOODS_AND_LAWSUIT, List.of(body()), false),
        Arguments.of(VictoryCondition.BODY_AND_GOODS_AND_LAWSUIT, List.of(body(), goods()), false),
        Arguments.of(
            VictoryCondition.BODY_AND_GOODS_AND_LAWSUIT, List.of(body(), lawsuit()), false),
        Arguments.of(
            VictoryCondition.BODY_AND_GOODS_AND_LAWSUIT, List.of(goods(), lawsuit()), false),
        Arguments.of(
            VictoryCondition.BODY_AND_GOODS_AND_LAWSUIT,
            List.of(body(), goods(), lawsuit()),
            true));
  }

  private static BodyVictory body() {
    return new BodyVictory(Player.WHITE, 3, 2);
  }

  private static GoodsVictory goods() {
    return new GoodsVictory(Player.WHITE, 30, 20);
  }

  private static LawsuitVictory lawsuit() {
    return new LawsuitVictory(Player.WHITE, 5, 4);
  }
}
