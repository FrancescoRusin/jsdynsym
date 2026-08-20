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

public interface RLMethod<S, A> {
  double[] getCurrentPolicyParams();
  double[] getCurrentCriticParams();

  void reset();

  void episodeReset();

  A step(double t, S input, double reward, boolean terminal);

  static <S> RLMethod<S, Double> singleDoubleMethod(RLMethod<S, double[]> method) {
    return new RLMethod<>() {
      @Override
      public double[] getCurrentPolicyParams() {
        return method.getCurrentPolicyParams();
      }

      @Override
      public double[] getCurrentCriticParams() {return method.getCurrentCriticParams();}

      @Override
      public void reset() {
        method.reset();
      }

      public void episodeReset() {
        method.episodeReset();
      }

      @Override
      public Double step(double t, S input, double reward, boolean terminal) {
        return method.step(t, input, reward, terminal)[0];
      }
    };
  }
}
