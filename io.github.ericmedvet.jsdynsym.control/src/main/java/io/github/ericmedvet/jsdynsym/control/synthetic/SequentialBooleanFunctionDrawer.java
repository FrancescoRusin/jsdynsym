/*-
 * ========================LICENSE_START=================================
 * jsdynsym-control
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
package io.github.ericmedvet.jsdynsym.control.synthetic;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.jsdynsym.control.Simulation.Outcome;
import io.github.ericmedvet.jsdynsym.control.SingleAgentTask;
import io.github.ericmedvet.jsdynsym.control.synthetic.BooleanUtils.ScoreType;
import io.github.ericmedvet.jsdynsym.control.synthetic.SequentialBooleanFunction.State;
import io.github.ericmedvet.jsdynsym.core.rl.ReinforcementLearningAgent;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.plot.Value;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries.Point;
import io.github.ericmedvet.jviz.core.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot.TitledData;
import io.github.ericmedvet.jviz.core.plot.image.Configuration;
import io.github.ericmedvet.jviz.core.plot.image.LinesPlotDrawer;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Set;
import java.util.function.ToDoubleFunction;

public class SequentialBooleanFunctionDrawer implements Drawer<Simulation.Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>>> {

  private final LinesPlotDrawer innerDrawer;
  private final Set<ScoreType> scoreTypes;

  public SequentialBooleanFunctionDrawer(Configuration configuration, Set<ScoreType> scoreTypes) {
    this.innerDrawer = new LinesPlotDrawer(configuration);
    this.scoreTypes = scoreTypes;
  }

  private static XYDataSeries buildXYDS(
      Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>> outcome,
      String name,
      ToDoubleFunction<State> f
  ) {
    return XYDataSeries.of(
        name,
        outcome.snapshots()
            .entrySet()
            .stream()
            .map(
                e -> new Point(
                    Value.of(e.getKey()),
                    Value.of(f.applyAsDouble(e.getValue().state()))
                )
            )
            .toList()
    );
  }

  @Override
  public void draw(
      Graphics2D g,
      Outcome<SingleAgentTask.Step<ReinforcementLearningAgent.RewardedInput<double[]>, double[], State>> outcome
  ) {
    int nOfOutputs = outcome.snapshots().values().iterator().next().state().output().length;
    XYDataSeriesPlot lp = new XYDataSeriesPlot(
        "Sequential boolean function simulation",
        "",
        "",
        "t",
        "",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            1,
            nOfOutputs + (scoreTypes.isEmpty() ? 0 : 1),
            (x, y) -> switch (y) {
              case 0 -> new TitledData<>(
                  "",
                  "scores",
                  scoreTypes.stream()
                      .map(
                          scoreType -> buildXYDS(
                              outcome,
                              scoreType.name().toLowerCase(),
                              s -> BooleanUtils.computeScore(
                                  s.output(),
                                  s.groundTruthOutput(),
                                  scoreType
                              )
                          )
                      )
                      .toList()
              );
              default -> new TitledData<>(
                  "",
                  "%d-th output".formatted(y - (scoreTypes.isEmpty() ? 0 : 1)),
                  List.of(
                      buildXYDS(
                          outcome,
                          "agent",
                          s -> s.output()[y - (scoreTypes.isEmpty() ? 0 : 1)]
                      ),
                      buildXYDS(
                          outcome,
                          "ground truth",
                          s -> s.groundTruthOutput()[y - (scoreTypes.isEmpty() ? 0 : 1)]
                      )
                  )
              );
            }
        )
    );
    innerDrawer.draw(g, lp);
  }
}