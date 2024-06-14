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
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

public class BaseTrajectoryDrawer extends AbstractArenaBasedTrajectoryDrawer implements Drawer<Point[][]> {
  private final BTDConfiguration configuration;

  public enum Mode {
    FIXED_ARROW,
    FIXED_LINE,
    GRADIENT
  }

  public record BTDConfiguration(
      Mode mode,
      Color segmentColor,
      Color arrowColor,
      float trajectoryThickness,
      double circleRadius,
      float segmentThickness,
      double marginRate)
      implements Configuration {
    public BTDConfiguration(Mode mode) {
      this(
          mode,
          Configuration.DEFAULT_SEGMENT_COLOR,
          Configuration.DEFAULT_ARROW_COLOR,
          Configuration.DEFAULT_TRAJECTORY_THICKNESS,
          Configuration.DEFAULT_CIRCLE_RADIUS,
          Configuration.DEFAULT_SEGMENT_THICKNESS,
          Configuration.DEFAULT_MARGIN_RATE);
    }
  }

  public BaseTrajectoryDrawer(Arena arena, BTDConfiguration configuration) {
    super(arena);
    this.configuration = configuration;
  }

  public BaseTrajectoryDrawer(Arena arena, Mode mode) {
    this(arena, new BTDConfiguration(mode));
  }

  @Override
  public void draw(Graphics2D g, Point[][] trajectories) {
    AffineTransform previousTransform = setTransform(g, arena, configuration);
    drawArena(g, configuration);
    g.setStroke(new BasicStroke(
        (float) (configuration.trajectoryThickness / g.getTransform().getScaleX())));
    if (Objects.isNull(trajectories)) {
      return;
    }
    switch (configuration.mode) {
      case FIXED_ARROW:
        g.setColor(configuration.arrowColor);
        IntStream.range(0, trajectories.length).forEach(i -> IntStream.range(1, trajectories[i].length)
            .forEach(j -> drawArrow(g, trajectories[i][j - 1], trajectories[i][j])));

        break;
      case FIXED_LINE:
        g.setColor(configuration.arrowColor);
        for (Point[] trajectory : trajectories) {
          Path2D path = new Path2D.Double();
          path.moveTo(trajectory[0].x(), trajectory[0].y());
          Arrays.stream(Arrays.copyOfRange(trajectory, 1, trajectory.length))
              .forEach(p -> path.lineTo(p.x(), p.y()));
          g.draw(path);
        }
        break;
      case GRADIENT:
        for (Point[] trajectory : trajectories) {
          for (int i = 0; i < trajectory.length - 1; ++i) {
            double tick = i / (double) (trajectory.length - 1);
            g.setColor(getColor(Color.RED, Color.YELLOW, Color.GREEN, tick));
            g.draw(new Line2D.Double(
                trajectory[i].x(), trajectory[i].y(), trajectory[i + 1].x(), trajectory[i + 1].y()));
          }
        }
    }
    g.setTransform(previousTransform);
  }
}
