/*-
 * ========================LICENSE_START=================================
 * jsdynsym-control
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
package io.github.ericmedvet.jsdynsym.control.synthetic;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.rl.NumericalReinforcementLearningAgent;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

public class SequantialXor implements Simulation<NumericalReinforcementLearningAgent<?>, SequantialXor.Step, Simulation.Outcome<SequantialXor.Step>> {
  private final List<double[]> cases;
  private final RewardType rewardType;
  private final boolean resetAgent;

  public SequantialXor(List<double[]> cases, RewardType rewardType, boolean resetAgent) {
    this.cases = cases;
    this.rewardType = rewardType;
    this.resetAgent = resetAgent;
  }

  @Override
  public Optional<NumericalReinforcementLearningAgent<?>> example() {
    return Optional.of(NumericalReinforcementLearningAgent.from(MultivariateRealFunction.from(2, 1)));
  }

  public static double computeError(double output, double gtOutput, RewardType rewardType) {
    return switch (rewardType) {
      case BOOLEAN_ERROR -> (output * gtOutput > 0) ? 1d : -1d;
      case ERROR -> gtOutput * output;
      case TRUNCATED_ERROR -> Math.max(gtOutput * output, 1);
    };
  }

  @Override
  public Outcome<Step> simulate(NumericalReinforcementLearningAgent<?> agent, double dT, DoubleRange tRange) {
    SortedMap<Double, Step> steps = new TreeMap<>();
    int c = 0;
    double reward = 0;
    if (resetAgent) {
      agent.reset();
    }
    for (double t = tRange.min(); t <= tRange.max(); t += dT) {
      double[] inputs = cases.get(c++ % cases.size());
      double output = agent.step(t, inputs, reward)[0];
      double gtOutput = ((inputs[0] > 0) ^ (inputs[1] > 0)) ? 1 : -1;
      reward = computeError(output, gtOutput, rewardType);
      steps.put(t, new Step(inputs, output, gtOutput, reward));
    }
    return Outcome.of(steps);
  }

  @Override
  public String toString() {
    return "SequantialXor[%d]".formatted(cases.size());
  }

  public enum RewardType {
    BOOLEAN_ERROR, TRUNCATED_ERROR, ERROR
  }

  public record Step(double[] inputs, double output, double groundTruthOutput, double reward) {
  }
}
