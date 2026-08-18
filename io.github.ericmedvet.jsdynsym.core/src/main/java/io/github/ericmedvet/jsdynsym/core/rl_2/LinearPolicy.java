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
import org.jspecify.annotations.NullMarked;

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
  public double[] meanAction(double[] state) {
    return LinearAlgebraUtils.product(weights, state);
  }

  @Override
  protected double[][] deterministicJacobian(double[] state) {
    double[][] jacobian = new double[weights.length][nOfParams()];
    for (int i = 0; i < weights.length; ++i) {
      Arrays.fill(jacobian[i], 0);
      for (int j = 0; j < weights.length; ++j) {
        jacobian[i * weights.length + j][j] = weights[i][j];
      }
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
    return weights.length * weights[0].length;
  }

  @Override
  @NullMarked
  public double[] getParams() {
    return Arrays.stream(weights).flatMap(a -> Arrays.stream(a).boxed()).mapToDouble(d -> d).toArray();
  }

  @Override
  public void setParams(double[] param) {
    if (param.length != weights.length * weights[0].length) {
      throw new IllegalArgumentException(
          "Wrong number of parameters; found %d, needed %d".formatted(param.length, weights.length * weights[0].length)
      );
    }
    int index = -1;
    for (int i = 0; i < weights.length; ++i) {
      for (int j = 0; j < weights[0].length; ++j) {
        weights[i][j] = param[++index];
      }
    }
  }
}
