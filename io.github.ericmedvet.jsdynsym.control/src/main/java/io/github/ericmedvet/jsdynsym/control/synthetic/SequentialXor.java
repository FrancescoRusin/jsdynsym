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
import io.github.ericmedvet.jnb.datastructure.Listener;
import io.github.ericmedvet.jsdynsym.control.SingleAgentTask;
import io.github.ericmedvet.jsdynsym.control.SingleRLAgentTask;
import io.github.ericmedvet.jsdynsym.control.synthetic.SequentialXor.State;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.rl.NumericalReinforcementLearningAgent;
import io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent;
import io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent.RewardedInput;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class SequentialXor<CS> implements SingleRLAgentTask<NumericalReinforcementLearningAgent<? extends CS>, double[], double[], CS, State> {

  private final List<double[]> cases;
  private final RewardType rewardType;
  private final boolean resetAgent;

  public SequentialXor(List<double[]> cases, RewardType rewardType, boolean resetAgent) {
    this.cases = cases;
    this.rewardType = rewardType;
    this.resetAgent = resetAgent;
  }

  public static double computeError(double output, double gtOutput, RewardType rewardType) {
    return switch (rewardType) {
      case BOOLEAN -> (output * gtOutput > 0) ? 1d : -1d;
      case UNLIMITED -> gtOutput * output;
      case LIMITED -> (gtOutput > 0) ? Math.min(1, output) : Math.min(1, -output);
    };
  }

  @Override
  public Optional<NumericalReinforcementLearningAgent<? extends CS>> example() {
    return Optional.of(
        NumericalReinforcementLearningAgent.from(NumericalDynamicalSystem.from(2, 1))
    );
  }

  @Override
  public Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>> simulate(
      NumericalReinforcementLearningAgent<? extends CS> agent,
      double dT,
      DoubleRange tRange,
      Listener<Timed<CS>> agentStateListener
  ) {
    SortedMap<Double, SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>> states = new TreeMap<>();
    int c = 0;
    double reward = 0;
    if (resetAgent) {
      agent.reset();
    }
    for (double t = tRange.min(); t < tRange.max(); t += dT) {
      double[] inputs = cases.get(c++ % cases.size());
      double[] outputs = agent.step(t, inputs, reward);
      agentStateListener.listen(new Timed<>(t, agent.getState()));
      double output = outputs[0];
      double gtOutput = ((inputs[0] > 0) ^ (inputs[1] > 0)) ? 1 : -1;
      reward = computeError(output, gtOutput, rewardType);
      states.put(
          t,
          new SingleAgentTask.Step<>(
              new RewardedInput<>(inputs, reward),
              outputs,
              new State(output, gtOutput)
          )
      );
    }
    agentStateListener.done();
    return Outcome.of(states);
  }

  @Override
  public String toString() {
    return "SequantialXor[%d]".formatted(cases.size());
  }

  public enum RewardType {
    BOOLEAN, LIMITED, UNLIMITED
  }

  public static String stringInputs(double[] inputs) {
    return Arrays.stream(inputs).mapToObj(i -> (i < 0) ? "0" : "1").collect(Collectors.joining());
  }

  public record State(double output, double groundTruthOutput) {

  }
}