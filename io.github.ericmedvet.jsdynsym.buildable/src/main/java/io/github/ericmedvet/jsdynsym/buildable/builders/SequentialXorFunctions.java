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
import io.github.ericmedvet.jsdynsym.control.synthetic.SequantialXor;
import java.util.List;
import java.util.function.Function;

@Discoverable(prefixTemplate = "dynamicalSystem|dynSys|ds.environment|env|e.sxor")
public class SequentialXorFunctions {
  private SequentialXorFunctions() {
  }

  @Cacheable
  public static <X> FormattedNamedFunction<X, Double> avgError(
      @Param(value = "name", iS = "avg[{rewardType}]") String name,
      @Param(value = "of", dNPM = "f.identity()") Function<X, Simulation.Outcome<SequantialXor.Step>> beforeF,
      @Param(value = "format", dS = "%+5.3f") String format,
      @Param(value = "rewardType", dS = "unlimited") SequantialXor.RewardType rewardType,
      @Param("caseIndexes") List<Integer> caseIndexes
  ) {
    Function<Simulation.Outcome<SequantialXor.Step>, Double> f = o -> {
      List<SequantialXor.Step> stepList = o.snapshots()
          .values()
          .stream()
          .toList();
      return caseIndexes.stream()
          .map(stepList::get)
          .mapToDouble(s -> SequantialXor.computeError(s.output(), s.groundTruthOutput(), rewardType))
          .average()
          .orElse(0d);
    };
    return FormattedNamedFunction.from(f, format, name)
        .compose(beforeF);
  }
}
