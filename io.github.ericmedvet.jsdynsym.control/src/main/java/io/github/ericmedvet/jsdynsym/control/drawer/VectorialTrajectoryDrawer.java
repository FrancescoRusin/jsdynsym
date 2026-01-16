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
package io.github.ericmedvet.jsdynsym.control.drawer;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Grid;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot;
import io.github.ericmedvet.jviz.core.plot.TrajectoryPlot.Data.ReductionType;
import io.github.ericmedvet.jviz.core.plot.Value;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries;
import io.github.ericmedvet.jviz.core.plot.XYDataSeries.Point;
import io.github.ericmedvet.jviz.core.plot.XYDataSeriesPlot;
import io.github.ericmedvet.jviz.core.plot.XYPlot.TitledData;
import io.github.ericmedvet.jviz.core.plot.image.Configuration;
import io.github.ericmedvet.jviz.core.plot.image.LinesPlotDrawer;
import io.github.ericmedvet.jviz.core.plot.image.TrajectoryPlotDrawer;
import java.awt.Graphics2D;
import java.util.Map;
import java.util.SortedMap;
import java.util.stream.IntStream;

public class VectorialTrajectoryDrawer implements Drawer<SortedMap<Double, double[]>> {

  private final TrajectoryPlot.Data.ReductionType reductionType;
  private final Drawer<Pair<TrajectoryPlot, XYDataSeriesPlot>> combinedDrawer;

  public VectorialTrajectoryDrawer(Configuration configuration, ReductionType reductionType) {
    this.reductionType = reductionType;
    combinedDrawer = Drawer.paired(
        new TrajectoryPlotDrawer(configuration),
        new LinesPlotDrawer(configuration),
        Arrangement.VERTICAL,
        -1
    );
  }

  private Pair<TrajectoryPlot, XYDataSeriesPlot> buildPair(
      SortedMap<Double, double[]> data
  ) {
    String varNameFormat = "v%" + (int) Math.ceil(Math.log10(data.firstEntry().getValue().length)) + "d";
    TrajectoryPlot tp = new TrajectoryPlot(
        "Trajectory (%s)".formatted(reductionType),
        "",
        "",
        "x1",
        "x2",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            1,
            1,
            new TitledData<>(
                "",
                "",
                TrajectoryPlot.Data.from(Map.of("values", data), reductionType)
            )
        )
    );
    XYDataSeriesPlot lp = new XYDataSeriesPlot(
        "Original data",
        "",
        "",
        "t",
        "value",
        DoubleRange.UNBOUNDED,
        DoubleRange.UNBOUNDED,
        Grid.create(
            1,
            1,
            new TitledData<>(
                "",
                "",
                IntStream.range(0, data.firstEntry().getValue().length)
                    .mapToObj(
                        i -> XYDataSeries.of(
                            varNameFormat.formatted(i),
                            data.entrySet()
                                .stream()
                                .map(e -> new Point(Value.of(e.getKey()), Value.of(e.getValue()[i])))
                                .toList()
                        )
                    )
                    .toList()
            )
        )
    );
    return new Pair<>(tp, lp);
  }

  @Override
  public void draw(Graphics2D g, SortedMap<Double, double[]> data) {
    combinedDrawer.draw(g, buildPair(data));
  }

  @Override
  public ImageInfo imageInfo(SortedMap<Double, double[]> data) {
    return combinedDrawer.imageInfo(buildPair(data));
  }
}