package io.github.ericmedvet.jsdynsym.core.rl_2;

import io.github.ericmedvet.jsdynsym.core.numerical.LinearAlgebraUtils;

import java.util.Arrays;

public class LinearPolicy extends GaussianNoisePolicy {
    private final double[][] weights;

    public LinearPolicy(int nOfInputs, int nOfOutputs, double noiseSigma, int seed) {
        super(noiseSigma, seed);
        this.weights = new double[nOfOutputs][nOfInputs];
    }

    public LinearPolicy(int nOfInputs, int nOfOutputs, int seed) {
        this(nOfInputs, nOfOutputs, DEFAULT_NOISE_SIGMA, seed);
    }

    public LinearPolicy(int nOfInputs, int nOfOutputs) {
        this(nOfInputs, nOfOutputs, -1);
    }

    @Override
    double[] meanAction(double[] state) {
        return LinearAlgebraUtils.product(weights, state);
    }

    @Override
    double[] deterministicGradient(double[] state) {
        return state;
    }

    @Override
    public double[] getParams() {
        return Arrays.stream(weights).flatMap(a -> Arrays.stream(a).boxed()).mapToDouble(d -> d).toArray();
    }

    @Override
    public void setParams(double[] param) {
        if (param.length != weights.length * weights[0].length) {
            throw new IllegalArgumentException("Wrong number of parameters; found %d, needed %d".formatted(param.length, weights.length * weights[0].length));
        }
        int index = -1;
        for (int i = 0; i < weights.length; ++i) {
            for (int j = 0; j < weights[0].length; ++j) {
                weights[i][j] = param[++index];
            }
        }
    }
}
