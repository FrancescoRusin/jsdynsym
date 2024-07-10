/*-
 * ========================LICENSE_START=================================
 * jsdynsym-control
 * %%
 * Copyright (C) 2023 - 2024 Eric Medvet
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
package io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers;

import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.util.GraphicsUtils;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.Objects;

public class RankBasedTrajectoryDrawer extends AbstractArenaBasedTrajectoryDrawer implements Drawer<MEIndividual[][]> {
  private final RBTConfiguration configuration;

  public record RBTConfiguration(
      Color segmentColor,
      Color arrowColor,
      float trajectoryThickness,
      double circleRadius,
      float segmentThickness,
      double pointThickness,
      double pointInternAlpha,
      double marginRate)
      implements Configuration {
    public RBTConfiguration() {
      this(
          Configuration.DEFAULT_SEGMENT_COLOR,
          Configuration.DEFAULT_ARROW_COLOR,
          Configuration.DEFAULT_TRAJECTORY_THICKNESS,
          Configuration.DEFAULT_CIRCLE_RADIUS,
          Configuration.DEFAULT_SEGMENT_THICKNESS,
          Configuration.DEFAULT_TRAJECTORY_THICKNESS * 1.3,
          .3,
          Configuration.DEFAULT_MARGIN_RATE);
    }
  }

  public RankBasedTrajectoryDrawer(Arena arena, RBTConfiguration configuration) {
    super(arena);
    this.configuration = configuration;
  }

  public RankBasedTrajectoryDrawer(Arena arena) {
    this(arena, new RBTConfiguration());
  }

  @Override
  public void draw(Graphics2D g, MEIndividual[][] individuals) {
    AffineTransform previousTransform = setTransform(g, arena, configuration);
    drawArena(g, configuration);
    g.setStroke(new BasicStroke(
        (float) (configuration.trajectoryThickness / g.getTransform().getScaleX())));
    if (Objects.isNull(individuals)) {
      return;
    }
    for (MEIndividual[] run : individuals) {
      Point basePoint = run[0].point();
      for (int i = 0; i < run.length; ++i) {
        do {
          ++i;
        } while (i < run.length && basePoint.equals(run[i].point()));
        Color color = getColor(Color.GREEN, Color.YELLOW, Color.RED, run[i - 1].relative_rank());
        Ellipse2D circle = new Ellipse2D.Double(
            basePoint.x() - configuration.circleRadius,
            basePoint.y() - configuration.circleRadius,
            2 * configuration.circleRadius,
            2 * configuration.circleRadius);
        g.setColor(GraphicsUtils.alphaed(color, configuration.pointInternAlpha));
        g.fill(circle);
        g.setColor(color);
        g.draw(circle);
        if (i < run.length) {
          g.setColor(configuration.arrowColor);
          drawArrow(g, basePoint, run[i].point());
          basePoint = run[i].point();
        }
      }
    }
    g.setTransform(previousTransform);
  }
}
