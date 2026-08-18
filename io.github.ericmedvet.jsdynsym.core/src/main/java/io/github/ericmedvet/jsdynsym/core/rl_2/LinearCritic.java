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
      throw new IllegalArgumentException(
          "Wrong number of parameters; found %d, needed %d".formatted(param.length, weights.length)
      );
    }
    System.arraycopy(param, 0, weights, 0, weights.length);
  }

  @Override
  public Double apply(double[] state) {
    return LinearAlgebraUtils.dotProduct(state, weights);
  }
}
