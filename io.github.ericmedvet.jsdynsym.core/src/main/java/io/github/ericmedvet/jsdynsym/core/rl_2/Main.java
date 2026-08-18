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
import java.util.stream.IntStream;

public class Main {
  static void main(String[] args) {
    final int nOfOutputs = 3;
    final double epsilon = 1e-10;
    NeuralPolicy policy = new NeuralPolicy(4, new int[]{3, 2}, nOfOutputs);
    final int nOfParams = policy.nOfParams();
    final double[] state = new double[]{-.3, .5, .2, .1};
    policy.setParams(IntStream.range(0, nOfParams).mapToDouble(i -> i / (double) nOfParams).toArray());
    double[] baseOutput = policy.meanAction(state);
    double[] baseParams = policy.getParams();
    double[][] approximateJacobian = new double[nOfOutputs][baseParams.length];
    for (int j = 0; j < baseParams.length; ++j) {
      double[] newParams = Arrays.copyOf(baseParams, baseParams.length);
      newParams[j] += epsilon;
      policy.setParams(newParams);
      double[] newOutput = policy.meanAction(state);
      for (int i = 0; i < nOfOutputs; ++i) {
        approximateJacobian[i][j] = (newOutput[i] - baseOutput[i]) / epsilon;
      }
    }
    double[][] computedJacobian = policy.deterministicJacobian(state);
    Arrays.stream(approximateJacobian).forEach(a -> System.out.println(Arrays.stream(a).boxed().toList()));
    System.out.printf("TRUE vs COMPUTED: max=%f", IntStream.range(0, nOfOutputs).mapToDouble(i -> IntStream.range(0, baseParams.length).mapToDouble(j -> Math.abs(approximateJacobian[i][j] - computedJacobian[i][j])).max().orElseThrow()).max().orElseThrow());
  }
}
