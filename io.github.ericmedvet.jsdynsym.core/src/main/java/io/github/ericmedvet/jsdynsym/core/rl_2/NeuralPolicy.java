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

import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;

public class NeuralPolicy extends GaussianNoisePolicy {
  private final MultiLayerPerceptron mlp;

  NeuralPolicy(
      int nOfInputs,
      int[] innerLayers,
      int nOfOutputs,
      MultiLayerPerceptron.ActivationFunction activationFunction,
      double noiseSigma,
      int seed
  ) {
    super(noiseSigma, seed);
    this.mlp = new MultiLayerPerceptron(
        activationFunction,
        nOfInputs,
        innerLayers,
        nOfOutputs
    );
  }

  NeuralPolicy(int nOfInputs, int[] innerLayers, int nOfOutputs, double noiseSigma, int seed) {
    this(nOfInputs, innerLayers, nOfOutputs, MultiLayerPerceptron.ActivationFunction.TANH, noiseSigma, seed);
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
    return mlp.jacobianByWeights(state);
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
