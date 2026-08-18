/*-
 * ========================LICENSE_START=================================
 * jsdynsym-core
 * %%
 * Copyright (C) 2023 - 2025 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */

package io.github.ericmedvet.jsdynsym.core.numerical.ann;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.NumericalParametrized;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import java.util.Arrays;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MultiLayerPerceptron implements MultivariateRealFunction, NumericalParametrized<MultiLayerPerceptron> {

  private final ActivationFunction activationFunction;
  private final double[][][] weights;
  private final int[] neurons;
  private final double[][] cachedActivation;
  private final double[] cachedInput;

  public MultiLayerPerceptron(
      ActivationFunction activationFunction,
      double[][][] weights,
      int[] neurons
  ) {
    this.activationFunction = activationFunction;
    this.weights = weights;
    this.neurons = neurons;
    this.cachedActivation = new double[neurons.length][];
    for (int i = 0; i < neurons.length; ++i) {
      this.cachedActivation[i] = new double[neurons[i]];
    }
    this.cachedInput = new double[neurons[0]];
    Arrays.fill(cachedInput, Double.POSITIVE_INFINITY);
    if (MLPUtils.flat(weights, neurons).length != MLPUtils.countWeights(neurons)) {
      throw new IllegalArgumentException(
          String.format(
              "Wrong number of weights: %d expected, %d found",
              MLPUtils.countWeights(neurons),
              MLPUtils.flat(weights, neurons).length
          )
      );
    }
  }

  public MultiLayerPerceptron(
      ActivationFunction activationFunction,
      int nOfInput,
      int[] innerNeurons,
      int nOfOutput,
      double[] weights
  ) {
    this(
        activationFunction,
        MLPUtils.unflat(weights, MLPUtils.countNeurons(nOfInput, innerNeurons, nOfOutput)),
        MLPUtils.countNeurons(nOfInput, innerNeurons, nOfOutput)
    );
  }

  public MultiLayerPerceptron(
      ActivationFunction activationFunction,
      int nOfInput,
      int[] innerNeurons,
      int nOfOutput
  ) {
    this(
        activationFunction,
        nOfInput,
        innerNeurons,
        nOfOutput,
        new double[MLPUtils.countWeights(MLPUtils.countNeurons(nOfInput, innerNeurons, nOfOutput))]
    );
  }

  @Override
  public double[] compute(double[] input) {
    MLPUtils.computeActivations(input, weights, activationFunction, cachedActivation);
    System.arraycopy(input, 0, cachedInput, 0, input.length);
    return Arrays.copyOf(cachedActivation[neurons.length - 1], cachedActivation[neurons.length - 1].length);
  }

  private double[][] activationValues(double[] input) {
    if (!Arrays.equals(input, cachedInput)) {
      MLPUtils.computeActivations(input, weights, activationFunction, cachedActivation);
      System.arraycopy(input, 0, cachedInput, 0, input.length);
    }
    double[][] result = new double[cachedActivation.length][];
    for (int i = 0; i < cachedActivation.length; ++i) {
      result[i] = Arrays.copyOf(cachedActivation[i], cachedActivation[i].length);
    }
    return result;
  }

  private double[][] derivedActivationValues(double[] input) {
    if (!Arrays.equals(input, cachedInput)) {
      MLPUtils.computeActivations(input, weights, activationFunction, cachedActivation);
      System.arraycopy(input, 0, cachedInput, 0, input.length);
    }
    double[][] result = new double[cachedActivation.length][];
    for (int i = 0; i < cachedActivation.length; ++i) {
      result[i] = Arrays.stream(cachedActivation[i]).boxed().mapToDouble(activationFunction::derivative).toArray();
    }
    return result;
  }

  public double[][] jacobianByWeights(double[] input) {
    final int nOfWeights = getParams().length;
    final double[][] activationValues = activationValues(input);
    final double[][] derivativeValues = derivedActivationValues(input);
    final int[] layers = IntStream.range(0, nOfLayers() - 1).map(this::sizeOfLayer).toArray();
    final double[][][] jacobianByWeight = new double[nOfLayers() - 1][][];
    for (int i = 0; i < weights.length; ++i) {
      jacobianByWeight[i] = new double[weights[i].length][];
      for (int j = 0; j < weights[i].length; ++j) {
        jacobianByWeight[i][j] = new double[weights[i][j].length];
      }
    }
    final double[][] jacobian = new double[nOfOutputs()][nOfWeights];
    for (int i = 0; i < nOfOutputs(); ++i) {

      //output layer: the only weights that matter for output i are the ones of the i-th neuron; this does not hold for the ones before
      for (int j = 0; j < i; ++j) {
        Arrays.fill(jacobianByWeight[layers.length - 1][j], 0);
      }
      for (int j = i + 1; j < nOfOutputs(); ++j) {
        Arrays.fill(jacobianByWeight[layers.length - 1][j], 0);
      }
      double[] prevDerivs = new double[layers[layers.length - 1]];
      for (int j = 0; j < layers[layers.length - 1]; ++j) {
        jacobianByWeight[layers.length - 1][i][j + 1] = derivativeValues[layers.length][i] * activationValues[layers.length - 1][j];
        prevDerivs[j] = derivativeValues[layers.length][i] * derivativeValues[layers.length - 1][j] * weights[layers.length - 1][i][j + 1];
      }
      jacobianByWeight[layers.length - 1][i][0] = derivativeValues[layers.length][i];

      //middle layers: chain rule
      for (int nLayer = layers.length - 2; nLayer >= 0; --nLayer) {
        for (int j = 0; j < layers[nLayer + 1]; ++j) {
          for (int k = 0; k < layers[nLayer]; ++k) {
            jacobianByWeight[nLayer][j][k + 1] = prevDerivs[j] * activationValues[nLayer][k];
          }
          jacobianByWeight[nLayer][j][0] = prevDerivs[j];
        }
        double[] newDerivs = new double[layers[nLayer]];
        for (int j = 0; j < layers[nLayer]; ++j) {
          double weightedSum = 0;
          for (int k = 0; k < prevDerivs.length; ++k) {
            weightedSum += prevDerivs[k] * weights[nLayer][k][j + 1];
          }
          newDerivs[j] = weightedSum * derivativeValues[nLayer][j];
        }
        prevDerivs = newDerivs;
      }

      //flatten the arrays and get the line of the Jacobian
      System.arraycopy(MLPUtils.flat(jacobianByWeight), 0, jacobian[i], 0, nOfWeights);
    }
    return jacobian;
  }

  public enum ActivationFunction implements DoubleUnaryOperator {
    RELU(x -> (x < 0) ? 0d : x, y -> (y > 0) ? 1d : 0d, new DoubleRange(0d, Double.POSITIVE_INFINITY)), SIGMOID(
        x -> 1d / (1d + Math.exp(-x)),
        y -> y * (1 - y),
        DoubleRange.UNIT
    ), SIN(Math::sin, null, DoubleRange.SYMMETRIC_UNIT), //the value of sin(x) alone does not uniquely determine dsin(x)/dx
    TANH(Math::tanh, y -> 1 - y * y, DoubleRange.SYMMETRIC_UNIT), SIGN(Math::signum, null, DoubleRange.SYMMETRIC_UNIT), //using signum for anything gradient-related is a bad idea
    IDENTITY(x -> x, y -> 1, DoubleRange.UNBOUNDED);

    private final DoubleUnaryOperator f;
    private final DoubleUnaryOperator df; //this is the derivative with the output of the neuron as input
    private final DoubleRange domain;

    ActivationFunction(DoubleUnaryOperator f, DoubleUnaryOperator df, DoubleRange domain) {
      this.f = f;
      this.df = df;
      this.domain = domain;
    }

    @Override
    public double applyAsDouble(double x) {
      return f.applyAsDouble(x);
    }

    public double derivative(double x) {
      return df.applyAsDouble(x);
    }

    public DoubleRange getDomain() {
      return domain;
    }

    public DoubleUnaryOperator getF() {
      return f;
    }

    public DoubleUnaryOperator getDF() {
      return df;
    }
  }

  @Override
  public double[] getParams() {
    return MLPUtils.flat(weights, neurons);
  }

  @Override
  public void setParams(double[] params) {
    double[][][] newWeights = MLPUtils.unflat(params, neurons);
    for (int l = 0; l < newWeights.length; l++) {
      for (int s = 0; s < newWeights[l].length; s++) {
        System.arraycopy(newWeights[l][s], 0, weights[l][s], 0, newWeights[l][s].length);
      }
    }
    cachedInput[0] = Double.POSITIVE_INFINITY;
  }

  public int sizeOfLayer(
      int indexOfLayer
  ) {
    return neurons[indexOfLayer];
  }

  @Override
  public int nOfInputs() {
    return sizeOfLayer(0);
  }

  public int nOfLayers() {
    return neurons.length;
  }

  @Override
  public int nOfOutputs() {
    return sizeOfLayer(neurons.length - 1);
  }

  @Override
  public String toString() {
    return "MLP-%s-%s"
        .formatted(
            activationFunction.toString().toLowerCase(),
            Arrays.stream(neurons).mapToObj(Integer::toString).collect(Collectors.joining(">"))
        );
  }
}