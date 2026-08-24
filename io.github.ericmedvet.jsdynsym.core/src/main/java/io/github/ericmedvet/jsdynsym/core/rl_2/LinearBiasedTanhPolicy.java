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

import io.github.ericmedvet.jsdynsym.core.numerical.LinearAlgebraUtils;
import java.util.Arrays;

public class LinearBiasedTanhPolicy extends GaussianNoisePolicy {
  private final double[][] weights;
  private final double[] biases;

  public LinearBiasedTanhPolicy(int nOfInputs, int nOfOutputs, double noiseSigma, int seed) {
    super(noiseSigma, seed);
    this.weights = new double[nOfOutputs][nOfInputs];
    this.biases = new double[nOfOutputs];
  }

  public LinearBiasedTanhPolicy(int nOfInputs, int nOfOutputs, int seed) {
    this(nOfInputs, nOfOutputs, DEFAULT_NOISE_SIGMA, seed);
  }

  public LinearBiasedTanhPolicy(int nOfInputs, int nOfOutputs) {
    this(nOfInputs, nOfOutputs, -1);
  }

  @Override
  public double[] meanAction(double[] state) {
    return Arrays.stream(LinearAlgebraUtils.sum(LinearAlgebraUtils.product(weights, state), biases))
        .map(Math::tanh)
        .toArray();
  }

  @Override
  protected double[][] deterministicJacobian(double[] state) {
    double[] meanActionDerivative = Arrays.stream(meanAction(state)).map(d -> 1 - d * d).toArray();
    double[][] jacobian = new double[nOfOutputs()][nOfParams()];
    for (int i = 0; i < nOfOutputs(); ++i) {
      Arrays.fill(jacobian[i], 0);
      for (int j = 0; j < nOfInputs(); ++j) {
        jacobian[i][j] = state[j] * meanActionDerivative[i];
      }
      jacobian[i][nOfInputs()] = meanActionDerivative[i];
    }
    return jacobian;
  }

  @Override
  public int nOfInputs() {
    return weights[0].length;
  }

  @Override
  public int nOfOutputs() {
    return weights.length;
  }

  @Override
  public int nOfParams() {
    return nOfOutputs() * (nOfInputs() + 1);
  }

  @Override
  public double[] getParams() {
    final double[] flatParams = new double[nOfParams()];
    int index = -1;
    for (int i = 0; i < nOfOutputs(); ++i) {
      for (int j = 0; j < nOfInputs(); ++j) {
        flatParams[++index] = weights[i][j];
      }
      flatParams[++index] = biases[i];
    }
    return flatParams;
  }

  @Override
  public void setParams(double[] param) {
    if (param.length != nOfParams()) {
      throw new IllegalArgumentException(
          "Wrong number of parameters; found %d, needed %d".formatted(param.length, nOfParams())
      );
    }
    int index = -1;
    for (int i = 0; i < nOfOutputs(); ++i) {
      for (int j = 0; j < nOfInputs(); ++j) {
        weights[i][j] = param[++index];
      }
      biases[i] = param[++index];
    }
  }
}
