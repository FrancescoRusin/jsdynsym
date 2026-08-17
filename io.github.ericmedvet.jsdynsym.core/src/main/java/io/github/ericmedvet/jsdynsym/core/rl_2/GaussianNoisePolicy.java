package io.github.ericmedvet.jsdynsym.core.rl_2;

import java.util.Random;
import java.util.stream.IntStream;

public abstract class GaussianNoisePolicy implements RLPolicy<double[]> {
    private final double noiseSigma;
    private final Random random;
    public static final double DEFAULT_NOISE_SIGMA = 1;

    GaussianNoisePolicy(double noiseSigma, int seed) {
        this.noiseSigma = noiseSigma;
        this.random = seed >= 0 ? new Random(seed) : new Random();
    }

    abstract double[] meanAction(double[] state);

    abstract double[] deterministicGradient(double[] state);

    @Override
    public double[] pickAction(double[] state) {
        double[] action = meanAction(state);
        for (int i = 0; i < action.length; ++i) {
            action[i] += random.nextGaussian() * noiseSigma;
        }
        return action;
    }

    @Override
    public double[] logGradient(double[] state, double[] action) {
        double[] meanAction = meanAction(state);
        double[] deterministicGradient = deterministicGradient(state);
        return IntStream.range(0, meanAction.length).mapToDouble(i -> (action[i] - meanAction[i]) * deterministicGradient[i] / (noiseSigma * noiseSigma)).toArray();
    }
}
