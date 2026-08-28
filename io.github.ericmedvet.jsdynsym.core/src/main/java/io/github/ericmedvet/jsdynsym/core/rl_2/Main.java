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

import io.github.ericmedvet.jsdynsym.core.numerical.LinearAlgebraUtils;
import io.github.ericmedvet.jsdynsym.core.rl_2.cartpole.CartPoleProblem;
import io.github.ericmedvet.jsdynsym.core.rl_2.cartpole.CartPoleVisualizer;
import org.lwjgl.system.MathUtil;

import java.util.Arrays;

public class Main {
  static void main(String[] args) {
    rlTest();
  }

  private static void rlTest() {
    CartPoleVisualizer visualizer = new CartPoleVisualizer();
    CartPoleProblem problem = new CartPoleProblem(0);
    RLMethod<double[], Double> method = RLMethod.singleDoubleMethod(
            new ActorCriticMethod(4, 1, ActorCriticMethod.Model.NEURAL, ActorCriticMethod.Model.NEURAL)
    );
    for (int i = 0; i < 100000; ++i) {
      problem.runEpisode(method, 1800, _ -> {
      });
      method.episodeReset();
      if (i % 100 == 0) {
        System.out.print("Current actor: ");
        System.out.println(Arrays.stream(method.getCurrentPolicyParams()).boxed().toList());
        System.out.print("Current critic: ");
        System.out.println(Arrays.stream(method.getCurrentCriticParams()).boxed().toList());
      }
    }
    problem.runEpisode(method, 1000, visualizer);
    visualizer.renderCache();
  }
}