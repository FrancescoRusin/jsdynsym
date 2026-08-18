package io.github.ericmedvet.jsdynsym.core.rl_2;

import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;

public class NeuralCritic implements RLCritic<double[]> {
    private final MultiLayerPerceptron mlp;

    public NeuralCritic(int nOfInputs, int[] innerLayers, MultiLayerPerceptron.ActivationFunction activationFunction) {
        this.mlp = new MultiLayerPerceptron(
                activationFunction,
                nOfInputs,
                innerLayers,
                1
        );
    }

    public NeuralCritic(int nOfInputs, int[] innerLayers) {
        this(nOfInputs, innerLayers, MultiLayerPerceptron.ActivationFunction.TANH);
    }

    @Override
    public double[] gradient(double[] state) {
        return mlp.jacobianByWeights(state)[0];
    }

    @Override
    public double[] getParams() {
        return mlp.getParams();
    }

    @Override
    public void setParams(double[] param) {
        mlp.setParams(param);
    }

    @Override
    public Double apply(double[] state) {
        return mlp.apply(state)[0];
    }
}
