/*-
 * ========================LICENSE_START=================================
 * jsdynsym-core
 * %%
 * Copyright (C) 2023 - 2024 Eric Medvet
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
package io.github.ericmedvet.jsdynsym.core.numerical;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.NumericalParametrized;
import java.util.Arrays;

public class MultiDimensionPolynomial2D
    implements MultivariateRealFunction, NumericalParametrized<MultiDimensionPolynomial2D> {
  private final int degree;
  private final double[][] weights;
  private final boolean clip;

  public MultiDimensionPolynomial2D(double[][] weights, boolean clip) {
    this.degree = (int) (Math.sqrt(1 + 4 * weights[0].length) - 1) / 2;
    for (double[] w : weights) {
      if (w.length != degree) {
        throw new IllegalArgumentException("Bruh");
      }
    }
    this.weights = weights;
    this.clip = clip;
  }

  public MultiDimensionPolynomial2D(double[][] weights) {
    this(weights, false);
  }

  public MultiDimensionPolynomial2D(int nOfOutputs, int degree, boolean clip) {
    this.degree = degree;
    this.weights = new double[nOfOutputs][(degree + 1) * (degree + 2) / 2];
    this.clip = clip;
  }

  public MultiDimensionPolynomial2D(int nOfOutputs, int degree) {
    this(nOfOutputs, degree, false);
  }

  @Override
  public double[] compute(double... input) {
    if (input.length != 2) {
      throw new IllegalArgumentException("Bruh");
    }
    // compute all the powers in advance to save computational time
    double[][] inputPowers = new double[2][degree + 1];
    inputPowers[0][0] = inputPowers[1][0] = 1d;
    for (int p = 1; p < degree + 1; ++p) {
      inputPowers[0][p] = inputPowers[0][p - 1] * input[0];
      inputPowers[1][p] = inputPowers[1][p - 1] * input[1];
    }
    // compute the result
    double[] result = new double[nOfOutputs()];
    Arrays.fill(result, 0d);
    for (int i = 0; i < nOfOutputs(); ++i) {
      int index = -1;
      for (int p = 0; p < degree + 1; ++p) {
        for (int k = 0; k < p + 1; ++k) {
          result[i] += inputPowers[0][p - k] * inputPowers[1][k] * weights[i][++index];
        }
      }
    }
    if (clip) {
      for (int i = 0; i < nOfOutputs(); ++i) {
        result[i] = DoubleRange.SYMMETRIC_UNIT.clip(result[i]);
      }
    }
    return result;
  }

  @Override
  public int nOfInputs() {
    return 2;
  }

  @Override
  public int nOfOutputs() {
    return weights.length;
  }

  public static double[] flat(double[][] unflatWeights) {
    double[] result = new double[unflatWeights.length * unflatWeights[0].length];
    int index = -1;
    for (double[] unflatWeight : unflatWeights) {
      for (int j = 0; j < unflatWeights[0].length; ++j) {
        result[++index] = unflatWeight[j];
      }
    }
    return result;
  }

  public static double[][] unflat(double[] flatWeights, int nOfOutputs) {
    if (flatWeights.length % nOfOutputs != 0) {
      throw new IllegalArgumentException("Bruh");
    }
    double[][] result = new double[nOfOutputs][flatWeights.length / nOfOutputs];
    int index = -1;
    for (int i = 0; i < nOfOutputs; ++i) {
      for (int j = 0; j < result[i].length; ++j) {
        result[i][j] = flatWeights[++index];
      }
    }
    return result;
  }

  @Override
  public double[] getParams() {
    return flat(weights);
  }

  @Override
  public void setParams(double[] doubles) {
    double[][] newWeights = unflat(doubles, nOfOutputs());
    for (int i = 0; i < nOfOutputs(); ++i) {
      System.arraycopy(newWeights[i], 0, weights[i], 0, weights[i].length);
    }
  }
}
