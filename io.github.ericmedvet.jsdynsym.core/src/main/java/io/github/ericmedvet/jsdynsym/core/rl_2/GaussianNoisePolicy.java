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

import java.util.Arrays;
import java.util.Random;
import java.util.stream.IntStream;

public abstract class GaussianNoisePolicy implements RLPolicy<double[], double[]> {
  protected final double noiseSigma;
  private final Random random;
  public static final double DEFAULT_NOISE_SIGMA = .5;

  GaussianNoisePolicy(double noiseSigma, int seed) {
    this.noiseSigma = noiseSigma;
    this.random = seed >= 0 ? new Random(seed) : new Random();
  }

  public abstract double[] meanAction(double[] state);

  protected abstract double[][] deterministicJacobian(double[] state);

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
    final double[] meanAction = meanAction(state);
    final double[][] deterministicJacobian = deterministicJacobian(state);
    final int nOfParams = nOfParams();
    double[] logGradient = new double[nOfParams];
    Arrays.fill(logGradient, 0);
    for (int i = 0; i < meanAction.length; ++i) {
      final int iCopy = i;
      double[] actionIContribution = IntStream.range(0, nOfParams)
          .mapToDouble(
              j -> (action[iCopy] - meanAction[iCopy]) * deterministicJacobian[iCopy][j] / (noiseSigma * noiseSigma)
          )
          .toArray();
      for (int j = 0; j < nOfParams; ++j) {
        logGradient[j] += actionIContribution[j];
      }
    }
    return logGradient;
  }
}
