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
import java.util.*;
import java.util.stream.IntStream;

public class MultiDimensionPolynomial
    implements MultivariateRealFunction, NumericalParametrized<MultiDimensionPolynomial2D> {
  private final int degree;
  private final int nOfInputs;
  private final double[][] weights;
  private final boolean clip;
  private final int[][] unflatDegrees;

  private static final TreeMap<Integer, Long> FACTORIAL_CACHE =
      new TreeMap<>(Map.ofEntries(Map.entry(0, 1L), Map.entry(1, 1L)));
  private static final TreeMap<Integer, TreeMap<Integer, Long>> COMBINATION_CACHE = new TreeMap<>();

  private static long factorial(int n) {
    if (n < 0) {
      return 0;
    }
    if (Objects.isNull(FACTORIAL_CACHE.get(n))) {
      FACTORIAL_CACHE.put(n, n * factorial(n - 1));
    }
    return FACTORIAL_CACHE.get(n);
  }

  private static long computeNOfCombinations(int nOfInputs, int degree) {
    if (Objects.isNull(COMBINATION_CACHE.get(nOfInputs))) {
      COMBINATION_CACHE.put(nOfInputs, new TreeMap<>());
    }
    if (Objects.isNull(COMBINATION_CACHE.get(nOfInputs).get(degree))) {
      long total = 0;
      for (int i = 0; i <= degree; ++i) {
        total += factorial(nOfInputs + i - 1) / (factorial(i) * factorial(nOfInputs - 1));
      }
      COMBINATION_CACHE.get(nOfInputs).put(degree, total);
    }
    return COMBINATION_CACHE.get(nOfInputs).get(degree);
  }

  private static int[][] coefficientPossibilities(int nOfInputs, int degree) {
    if (nOfInputs == 1) {
      int[][] possibilities = new int[degree + 1][1];
      IntStream.range(0, degree + 1).forEach(i -> possibilities[i][0] = i);
      return possibilities;
    }
    final int[][] possibilities = new int[(int) computeNOfCombinations(nOfInputs, degree)][nOfInputs];
    int index = -1;
    for (int i = 0; i <= degree; ++i) {
      final int[][] otherVarsPossibilities = coefficientPossibilities(nOfInputs - 1, degree - i);
      for (int[] otherVars : otherVarsPossibilities) {
        possibilities[++index][0] = i;
        System.arraycopy(otherVars, 0, possibilities[index], 1, otherVars.length);
      }
    }
    return possibilities;
  }

  public MultiDimensionPolynomial(int nOfInputs, int nOfOutputs, int degree, boolean clip) {
    this.degree = degree;
    this.nOfInputs = nOfInputs;
    this.weights = new double[nOfOutputs][(int) computeNOfCombinations(nOfInputs, degree)];
    this.unflatDegrees = coefficientPossibilities(nOfInputs, degree);
    this.clip = clip;
  }

  public MultiDimensionPolynomial(int nOfInputs, int nOfOutputs, int degree) {
    this(nOfInputs, nOfOutputs, degree, false);
  }

  @Override
  public double[] compute(double... input) {
    if (input.length != nOfInputs) {
      throw new IllegalArgumentException("Bruh");
    }
    // compute all the powers in advance to save computational time
    double[][] inputPowers = new double[nOfInputs][degree + 1];
    for (int i = 0; i < nOfInputs; ++i) {
      inputPowers[i][0] = 1d;
    }
    for (int p = 1; p < degree + 1; ++p) {
      for (int i = 0; i < nOfInputs; ++i) {
        inputPowers[i][p] = inputPowers[i][p - 1] * input[i];
      }
    }
    // compute the result
    double[] result = new double[nOfOutputs()];
    Arrays.fill(result, 0d);
    for (int i = 0; i < nOfOutputs(); ++i) {
      int index = -1;
      for (int[] unflatDegree : unflatDegrees) {
        double partialCount = 1;
        for (int k = 0; k < nOfInputs; ++k) {
          partialCount *= inputPowers[k][unflatDegree[k]];
        }
        result[i] += partialCount * weights[i][++index];
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
    return nOfInputs;
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

  public double[][] unflat(double[] flatWeights, int nOfOutputs) {
    if (flatWeights.length % nOfOutputs != 0
        || flatWeights.length / nOfOutputs != computeNOfCombinations(nOfInputs, degree)) {
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
