/*-
 * ========================LICENSE_START=================================
 * jsdynsym-core
 * %%
 * Copyright (C) 2023 - 2025 Eric Medvet
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

package io.github.ericmedvet.jsdynsym.core;

import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jnb.datastructure.Pair;

public interface DynamicalSystem<I, O, S> {

  S getState();

  void reset();

  O step(double t, I input);

  default <O2, S2> DynamicalSystem<I, O2, Pair<S, S2>> andThen(DynamicalSystem<O, O2, S2> other) {
    DynamicalSystem<I, O, S> thisDS = this;
    return new DynamicalSystem<>() {
      @Override
      public Pair<S, S2> getState() {
        return new Pair<>(thisDS.getState(), other.getState());
      }

      @Override
      public void reset() {
        thisDS.reset();
        other.reset();
      }

      @Override
      public O2 step(double t, I input) {
        O o = thisDS.step(t, input);
        return other.step(t, o);
      }

      @Override
      public String toString() {
        return thisDS + NamedFunction.NAME_JOINER + other;
      }
    };
  }
}