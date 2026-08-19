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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface RLEpisodicTask<I, O, S> extends RLTask<I, O, S> {
  S initialize();

  boolean stopCondition(S state);

  default List<S> runEpisode(RLMethod<I, O> method, int nOfTicks, Consumer<S> listener) {
    S state = initialize();
    List<S> outcome = new ArrayList<>(List.of(state));
    listener.accept(state);
    for (int t = 0; t < nOfTicks && !stopCondition(state); ++t) {
      state = executeAction(state, method.step(t, computeNewInput(state), computeReward(state)));
      outcome.add(state);
      listener.accept(state);
    }
    return outcome;
  }
}
