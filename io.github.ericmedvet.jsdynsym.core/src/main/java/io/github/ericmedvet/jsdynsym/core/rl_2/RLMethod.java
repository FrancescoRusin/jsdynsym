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

import io.github.ericmedvet.jsdynsym.core.DynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;

public interface RLMethod<P> extends DynamicalSystem<RLMethod.InputAndPrevReward, double[], P> {
  record InputAndPrevReward(double[] input, double reward) {}

  double[] step(double t, double[] input, double reward);

  @Override
  default double[] step(double t, InputAndPrevReward inputAndReward) {
    return step(t, inputAndReward.input(), inputAndReward.reward());
  }

  static <P> RLMethod<P> from(NumericalDynamicalSystem<P> dynamicalSystem) {
    return new RLMethod<>() {
      @Override
      public double[] step(double t, double[] input, double reward) {
        return dynamicalSystem.step(t, input);
      }

      @Override
      public P getState() {
        return dynamicalSystem.getState();
      }

      @Override
      public void reset() {
        dynamicalSystem.reset();
      }

      @Override
      public String toString() {
        return dynamicalSystem.toString();
      }
    };
  }
}
