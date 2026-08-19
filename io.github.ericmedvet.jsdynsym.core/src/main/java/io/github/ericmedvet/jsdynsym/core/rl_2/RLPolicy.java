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

import io.github.ericmedvet.jnb.core.Cacheable;
import io.github.ericmedvet.jnb.datastructure.NumericalParametrized;

public interface RLPolicy<S, A> extends NumericalParametrized<RLPolicy<S, A>> {
  @Cacheable
  int nOfInputs();

  @Cacheable
  int nOfOutputs();

  @Cacheable
  int nOfParams();

  A pickAction(S state);

  double[] logGradient(S state, A action);

  static <S> RLPolicy<S, Double> singleDoublePolicy(RLPolicy<S, double[]> policy) {
    return new RLPolicy<>() {

      @Override
      public int nOfInputs() {
        return policy.nOfInputs();
      }

      @Override
      public int nOfOutputs() {
        return 1;
      }

      @Override
      public int nOfParams() {
        return policy.nOfParams();
      }

      @Override
      public Double pickAction(S state) {
        return policy.pickAction(state)[0];
      }

      @Override
      public double[] logGradient(S state, Double action) {
        return policy.logGradient(state, new double[]{action});
      }

      @Override
      public double[] getParams() {
        return policy.getParams();
      }

      @Override
      public void setParams(double[] param) {
        policy.setParams(param);
      }
    };
  }
}
