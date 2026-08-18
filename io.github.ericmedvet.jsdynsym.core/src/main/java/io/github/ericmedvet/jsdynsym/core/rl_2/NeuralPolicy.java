/*-
 * ========================LICENSE_START=================================
 * jsdynsym-core
 * %%
 * Copyright (C) 2023 - 2026 Eric Medvet
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
package io.github.ericmedvet.jsdynsym.core.rl_2;

import io.github.ericmedvet.jsdynsym.core.numerical.ann.MLPUtils;
import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;
import java.util.Arrays;
import java.util.stream.IntStream;

public class NeuralPolicy extends GaussianNoisePolicy {
  private final MultiLayerPerceptron mlp;

  NeuralPolicy(int nOfInputs, int[] innerLayers, int nOfOutputs, double noiseSigma, int seed) {
    super(noiseSigma, seed);
    this.mlp = new MultiLayerPerceptron(
        MultiLayerPerceptron.ActivationFunction.TANH,
        nOfInputs,
        innerLayers,
        nOfOutputs
    );
  }

  NeuralPolicy(int nOfInputs, int[] innerLayers, int nOfOutputs, int seed) {
    this(nOfInputs, innerLayers, nOfOutputs, GaussianNoisePolicy.DEFAULT_NOISE_SIGMA, seed);
  }

  NeuralPolicy(int nOfInputs, int[] innerLayers, int nOfOutputs) {
    this(nOfInputs, innerLayers, nOfOutputs, -1);
  }

  @Override
  public int nOfInputs() {
    return mlp.nOfInputs();
  }

  @Override
  public int nOfOutputs() {
    return mlp.nOfOutputs();
  }

  @Override
  public int nOfParams() {
    return mlp.getParams().length;
  }

  @Override
  public double[] meanAction(double[] state) {
    return mlp.apply(state);
  }

  @Override
  protected double[][] deterministicJacobian(double[] state) {
    final double[][] activationValues = mlp.activationValues(state);
    final double[][] derivativeValues = mlp.derivedActivationValues(state);
    final double[][][] weights = mlp.getWeights();
    final int[] layers = IntStream.range(0, mlp.nOfLayers() - 1).map(mlp::sizeOfLayer).toArray();
    final double[][][] jacobianByWeight = new double[mlp.nOfLayers() - 1][][];
    for (int i = 0; i < weights.length; ++i) {
      jacobianByWeight[i] = new double[weights[i].length][];
      for (int j = 0; j < weights[i].length; ++j) {
        jacobianByWeight[i][j] = new double[weights[i][j].length];
      }
    }
    final double[][] jacobian = new double[nOfOutputs()][nOfParams()];
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
      System.arraycopy(MLPUtils.flat(jacobianByWeight), 0, jacobian[i], 0, nOfParams());
    }
    return jacobian;
  }

  @Override
  public double[] getParams() {
    return mlp.getParams();
  }

  @Override
  public void setParams(double[] param) {
    mlp.setParams(param);
  }
}
