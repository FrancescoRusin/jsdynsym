package io.github.ericmedvet.jsdynsym.core.rl_2;

import io.github.ericmedvet.jnb.datastructure.NumericalParametrized;

import java.util.function.Function;

public interface RLCritic<S> extends Function<S, Double>, NumericalParametrized<RLCritic<S>> {
    double[] gradient(S state);
}
