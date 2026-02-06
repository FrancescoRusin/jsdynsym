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
import io.github.ericmedvet.jsdynsym.control.synthetic.BooleanUtils.ScoreType;
import io.github.ericmedvet.jsdynsym.control.synthetic.SequentialBooleanFunction.State;
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
  private final ScoreType scoreType;
  private final boolean resetAgent;

  public SequentialXor(List<double[]> cases, ScoreType scoreType, boolean resetAgent) {
    this.cases = cases;
    this.scoreType = scoreType;
    this.resetAgent = resetAgent;
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
      double[] input = cases.get(c++ % cases.size());
      double[] output = agent.step(t, input, reward);
      agentStateListener.listen(new Timed<>(t, agent.getState()));
      double[] gtOutput = new double[]{((input[0] > 0) ^ (input[1] > 0)) ? 1 : -1};
      reward = BooleanUtils.computeScore(output, gtOutput, scoreType);
      states.put(
          t,
          new SingleAgentTask.Step<>(
              new RewardedInput<>(input, reward),
              output,
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

  public static String stringInputs(double[] inputs) {
    return Arrays.stream(inputs).mapToObj(i -> (i < 0) ? "0" : "1").collect(Collectors.joining());
  }

}