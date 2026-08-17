package io.github.ericmedvet.jsdynsym.core.rl_2;

import io.github.ericmedvet.jsdynsym.core.numerical.LinearAlgebraUtils;

import java.util.Arrays;

public class LinearCritic implements RLCritic<double[]> {
    private final double[] weights;

    public LinearCritic(int nOfInputs) {
        this.weights = new double[nOfInputs];
    }

    @Override
    public double[] gradient(double[] state) {
        return state;
    }

    @Override
    public double[] getParams() {
        return Arrays.copyOf(weights, weights.length);
    }

    @Override
    public void setParams(double[] param) {
        if (param.length != weights.length) {
            throw new IllegalArgumentException("Wrong number of parameters; found %d, needed %d".formatted(param.length, weights.length));
        }
        System.arraycopy(param, 0, weights, 0, weights.length);
    }

    @Override
    public Double apply(double[] state) {
        return LinearAlgebraUtils.dotProduct(state, weights);
    }
}
