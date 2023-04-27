/*
 * Copyright 2023 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jsdynsym.core.rl;

import io.github.ericmedvet.jsdynsym.core.TimeInvariantDynamicalSystem;

public interface NumericalTimeInvariantReinforcementLearningAgent<S> extends NumericalReinforcementLearningAgent<S>,
    TimeInvariantDynamicalSystem<ReinforcementLearningAgent.RewardedInput<double[]>, double[], S> {

  double[] step(double reward, double[] input);

  @Override
  default double[] step(double t, RewardedInput<double[]> rewardedInput) {
    return step(rewardedInput.reward(), rewardedInput.input());
  }

  @Override
  default double[] step(RewardedInput<double[]> input) {
    return step(input.reward(), input.input());
  }

  @Override
  default double[] step(double t, double reward, double[] input) {
    return step(reward, input);
  }
}