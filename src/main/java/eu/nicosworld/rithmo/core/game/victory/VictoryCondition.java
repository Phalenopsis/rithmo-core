package eu.nicosworld.rithmo.core.game.victory;

import eu.nicosworld.rithmo.engine.victory.VictoryType;
import java.util.Set;

public enum VictoryCondition {
  BODY {
    @Override
    public boolean matches(Set<VictoryType> victories) {
      return victories.contains(VictoryType.BODY);
    }
  },

  GOODS {
    @Override
    public boolean matches(Set<VictoryType> victories) {
      return victories.contains(VictoryType.GOODS);
    }
  },

  LAWSUIT {
    @Override
    public boolean matches(Set<VictoryType> victories) {
      return victories.contains(VictoryType.LAWSUIT);
    }
  },

  BODY_AND_GOODS {
    @Override
    public boolean matches(Set<VictoryType> victories) {
      return victories.contains(VictoryType.BODY) && victories.contains(VictoryType.GOODS);
    }
  },

  BODY_AND_LAWSUIT {
    @Override
    public boolean matches(Set<VictoryType> victories) {
      return victories.contains(VictoryType.BODY) && victories.contains(VictoryType.LAWSUIT);
    }
  },

  GOODS_AND_LAWSUIT {
    @Override
    public boolean matches(Set<VictoryType> victories) {
      return victories.contains(VictoryType.GOODS) && victories.contains(VictoryType.LAWSUIT);
    }
  },
  BODY_AND_GOODS_AND_LAWSUIT {
    @Override
    public boolean matches(Set<VictoryType> victories) {
      return victories.contains(VictoryType.GOODS)
          && victories.contains(VictoryType.LAWSUIT)
          && victories.contains(VictoryType.BODY);
    }
  };

  public abstract boolean matches(Set<VictoryType> victories);

  public static VictoryCondition fromRule(VictoryType option) {
    return switch (option) {
      case LAWSUIT -> LAWSUIT;
      case BODY -> BODY;
      case GOODS -> GOODS;
    };
  }
}
