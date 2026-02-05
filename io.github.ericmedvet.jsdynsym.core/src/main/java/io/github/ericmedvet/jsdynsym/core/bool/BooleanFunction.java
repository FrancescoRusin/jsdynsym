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
package io.github.ericmedvet.jsdynsym.core.bool;

import io.github.ericmedvet.jnb.datastructure.NamedFunction;
import io.github.ericmedvet.jsdynsym.core.TimeInvariantStatelessSystem;
import java.util.function.Function;

public interface BooleanFunction extends TimeInvariantStatelessSystem<boolean[], boolean[]> {

  int nOfInputs();

  int nOfOutputs();

  boolean[] compute(boolean... input);

  @Override
  default boolean[] step(boolean[] input) {
    return compute(input);
  }

  static BooleanFunction from(int nOfInputs, int nOfOutputs) {
    return from(
        NamedFunction.from(
            input -> new boolean[nOfOutputs],
            "falses[%d->%d]".formatted(nOfInputs, nOfOutputs)
        ),
        nOfInputs,
        nOfOutputs
    );
  }

  static BooleanFunction from(
      Function<boolean[], boolean[]> f,
      int nOfInputs,
      int nOfOutputs
  ) {
    return new BooleanFunction() {
      @Override
      public boolean[] compute(boolean... input) {
        return f.apply(input);
      }

      @Override
      public int nOfInputs() {
        return nOfInputs;
      }

      @Override
      public int nOfOutputs() {
        return nOfOutputs;
      }

      @Override
      public String toString() {
        return f.toString();
      }
    };
  }

}
