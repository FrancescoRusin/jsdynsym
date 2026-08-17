package io.github.ericmedvet.jsdynsym.core.rl_2;

import io.github.ericmedvet.jnb.datastructure.NumericalParametrized;

public interface RLPolicy<S> extends NumericalParametrized<RLPolicy<S>> {
    double[] pickAction(S state);
    double[] logGradient(S state, double[] action);
}
