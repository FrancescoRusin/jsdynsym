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
import io.github.ericmedvet.jsdynsym.control.SingleRLAgentTask;
import io.github.ericmedvet.jsdynsym.control.synthetic.BooleanUtils.ScoreType;
import io.github.ericmedvet.jsdynsym.control.synthetic.SequentialBooleanFunction.State;
import io.github.ericmedvet.jsdynsym.core.bool.BooleanFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.rl.NumericalReinforcementLearningAgent;
import io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent.RewardedInput;
import java.util.List;
import java.util.Optional;
import java.util.SortedMap;
import java.util.TreeMap;

public class SequentialBooleanFunction<CS> implements SingleRLAgentTask<NumericalReinforcementLearningAgent<? extends CS>, double[], double[], CS, State> {

  private final List<Case> cases;
  private final BooleanFunction target;
  private final ScoreType scoreType;
  private final boolean resetAgent;

  public SequentialBooleanFunction(
      List<boolean[]> cases,
      BooleanFunction target,
      ScoreType scoreType,
      boolean resetAgent
  ) {
    this.cases = cases.stream()
        .map(
            bitString -> new Case(
                BooleanUtils.bitStringToDoubleString(bitString),
                BooleanUtils.bitStringToDoubleString(target.compute(bitString))
            )
        )
        .toList();
    this.target = target;
    this.scoreType = scoreType;
    this.resetAgent = resetAgent;
  }

  @Override
  public Optional<NumericalReinforcementLearningAgent<? extends CS>> example() {
    return Optional.of(
        NumericalReinforcementLearningAgent.from(
            NumericalDynamicalSystem.from(target.nOfInputs(), target.nOfOutputs())
        )
    );
  }

  @Override
  public Outcome<Step<RewardedInput<double[]>, double[], State>> simulate(
      NumericalReinforcementLearningAgent<? extends CS> agent,
      double dT,
      DoubleRange tRange,
      Listener<Timed<CS>> agentStateListener
  ) {
    SortedMap<Double, Step<RewardedInput<double[]>, double[], State>> states = new TreeMap<>();
    int c = 0;
    double reward = 0;
    if (resetAgent) {
      agent.reset();
    }
    for (double t = tRange.min(); t < tRange.max(); t += dT) {
      Case bCase = cases.get(c++ % cases.size());
      double[] output = agent.step(t, bCase.input, reward);
      agentStateListener.listen(new Timed<>(t, agent.getState()));
      reward = BooleanUtils.computeScore(output, bCase.output, scoreType);
      states.put(
          t,
          new Step<>(
              new RewardedInput<>(bCase.input, reward),
              output,
              new State(bCase.output, bCase.output)
          )
      );
    }
    agentStateListener.done();
    return Outcome.of(states);
  }

  @Override
  public String toString() {
    return "SequentialBF[%d->%d;%d]".formatted(
        target.nOfInputs(),
        target.nOfOutputs(),
        cases.size()
    );
  }

  private record Case(double[] input, double[] output) {
  }

  public record State(double[] output, double[] groundTruthOutput) {
  }
}