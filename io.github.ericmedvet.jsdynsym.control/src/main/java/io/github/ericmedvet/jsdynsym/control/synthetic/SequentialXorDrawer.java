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
import io.github.ericmedvet.jsdynsym.control.synthetic.SequantialXor.Step;
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

public class SequentialXorDrawer implements Drawer<Simulation.Outcome<SequantialXor.Step>> {

  private final LinesPlotDrawer innerDrawer;

  public SequentialXorDrawer(Configuration configuration) {
    this.innerDrawer = new LinesPlotDrawer(configuration);
  }

  @Override
  public void draw(Graphics2D g, Outcome<Step> outcome) {
    XYDataSeriesPlot lp = new XYDataSeriesPlot(
        "XOR simulation",
        "",
        "",
        "t",
        "output",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            1,
            1,
            new TitledData<>(
                "",
                "",
                List.of(
                    XYDataSeries.of(
                        "ground truth",
                        outcome.snapshots()
                            .entrySet()
                            .stream()
                            .map(
                                e -> new Point(
                                    Value.of(e.getKey()),
                                    Value.of(e.getValue().groundTruthOutput())
                                )
                            )
                            .toList()
                    ),
                    XYDataSeries.of(
                        "agent",
                        outcome.snapshots()
                            .entrySet()
                            .stream()
                            .map(
                                e -> new Point(
                                    Value.of(e.getKey()),
                                    Value.of(e.getValue().groundTruthOutput())
                                )
                            )
                            .toList()
                    )
                )
            )
        )
    );
    innerDrawer.draw(g, lp);
  }
}
