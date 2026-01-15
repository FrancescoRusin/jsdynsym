/*-
 * ========================LICENSE_START=================================
 * jsdynsym-buildable
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
package io.github.ericmedvet.jsdynsym.buildable.builders;

import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.core.Discoverable;
import io.github.ericmedvet.jnb.core.Param;
import io.github.ericmedvet.jnb.datastructure.FormattedNamedFunction;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.jsdynsym.control.SingleAgentTask;
import io.github.ericmedvet.jsdynsym.control.SingleAgentTask.Step;
import io.github.ericmedvet.jsdynsym.control.synthetic.SequentialXor;
import io.github.ericmedvet.jsdynsym.control.synthetic.SequentialXor.State;
import io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Discoverable(prefixTemplate = "dynamicalSystem|dynSys|ds.environment|env|e.sxor")
public class SequentialXorFunctions {
  private SequentialXorFunctions() {
  }

  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> avgScore(
      @Param(value = "name", iS = "avg[{rewardType}]") String name,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>>> beforeF,
      @Param(value = "format", dS = "%+5.3f") String format,
      @Param(value = "rewardType", dS = "unlimited") SequentialXor.RewardType rewardType,
      @Param("caseIndexes") List<Integer> caseIndexes
  ) {
    Function<Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>>, Double> f = o -> {
      List<State> states = o.snapshots()
          .values()
          .stream()
          .map(Step::state)
          .toList();
      return caseIndexes.stream()
          .map(i -> states.get((i < 0) ? (states.size() + i) : i))
          .mapToDouble(s -> SequentialXor.computeError(s.output(), s.groundTruthOutput(), rewardType))
          .average()
          .orElse(0d);
    };
    return FormattedNamedFunction.from(f, format, name)
        .compose(beforeF);
  }

  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> avgScoreDelta(
      @Param(value = "name", iS = "avg.delta[{rewardType}]") String name,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>>> beforeF,
      @Param(value = "format", dS = "%+5.3f") String format,
      @Param(value = "rewardType", dS = "unlimited") SequentialXor.RewardType rewardType,
      @Param("firstIndexes") List<Integer> firstIndexes,
      @Param("secondIndexes") List<Integer> secondIndexes
  ) {
    Function<Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>>, Double> f = o -> o
        .snapshots()
        .values()
        .stream()
        .collect(Collectors.groupingBy(s -> SequentialXor.stringInputs(s.observation().input())))
        .values()
        .stream()
        .mapToDouble(steps -> {
          double firstAvg = firstIndexes.stream()
              .map(i -> steps.get((i < 0) ? (steps.size() + i) : i))
              .mapToDouble(
                  s -> SequentialXor.computeError(s.state().output(), s.state().groundTruthOutput(), rewardType)
              )
              .average()
              .orElse(0);
          double secondAvg = secondIndexes.stream()
              .map(i -> steps.get((i < 0) ? (steps.size() + i) : i))
              .mapToDouble(
                  s -> SequentialXor.computeError(s.state().output(), s.state().groundTruthOutput(), rewardType)
              )
              .average()
              .orElse(0);
          return firstAvg - secondAvg;
        })
        .average()
        .orElse(0);
    return FormattedNamedFunction.from(f, format, name)
        .compose(beforeF);
  }

  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> avgScoreVariation(
      @Param(value = "name", iS = "avg.delta[{rewardType}]") String name,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>>> beforeF,
      @Param(value = "format", dS = "%+5.3f") String format,
      @Param(value = "rewardType", dS = "unlimited") SequentialXor.RewardType rewardType,
      @Param("indexes") List<Integer> indexes
  ) {
    Function<Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>>, Double> f = o -> o
        .snapshots()
        .values()
        .stream()
        .collect(Collectors.groupingBy(s -> SequentialXor.stringInputs(s.observation().input())))
        .values()
        .stream()
        .mapToDouble(steps -> {
          double[] scores = indexes.stream()
              .map(i -> steps.get((i < 0) ? (steps.size() + i) : i))
              .mapToDouble(
                  s -> SequentialXor.computeError(s.state().output(), s.state().groundTruthOutput(), rewardType)
              )
              .toArray();
          double avg = 0;
          for (double v : scores) {
            avg += v;
          }
          avg /= scores.length;
          double numerator = 0;
          for (double v : scores) {
            numerator += (v - avg) * (v - avg);
          }
          return Math.sqrt(numerator / scores.length);
        })
        .average()
        .orElse(0);
    return FormattedNamedFunction.from(f, format, name)
        .compose(beforeF);
  }
}