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

import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.util.GraphicsUtils;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.*;
import java.util.List;

@SuppressWarnings("unchecked")
public class MapElitesTrajectoryDrawer extends AbstractArenaBasedTrajectoryDrawer
    implements Drawer<Pair<MEIndividual, Integer>[][]> {
  private final METConfiguration configuration;

  public enum Mode {
    BASE,
    RANK
  }

  public record METConfiguration(
      Mode mode,
      Point descriptorTick,
      Color segmentColor,
      Color arrowColor,
      Color tickColor,
      float trajectoryThickness,
      float circleThickness,
      double circleRadius,
      float segmentThickness,
      double pointInternAlpha,
      double marginRate)
      implements Configuration {
    public METConfiguration(Mode mode, Point descriptorTick) {
      this(
          mode,
          descriptorTick,
          Configuration.DEFAULT_SEGMENT_COLOR,
          Configuration.DEFAULT_ARROW_COLOR,
          Color.ORANGE,
          Configuration.DEFAULT_TRAJECTORY_THICKNESS,
          1,
          Math.min(descriptorTick.x(), descriptorTick.y()) * (mode == Mode.BASE ? .2 : .4),
          Configuration.DEFAULT_SEGMENT_THICKNESS,
          1,
          Configuration.DEFAULT_MARGIN_RATE);
    }
  }

  public MapElitesTrajectoryDrawer(Arena arena, METConfiguration configuration) {
    super(arena);
    this.configuration = configuration;
  }

  public MapElitesTrajectoryDrawer(Arena arena, Mode mode, Point descriptorTick) {
    this(arena, new METConfiguration(mode, descriptorTick));
  }

  @Override
  public void draw(Graphics2D g, Pair<MEIndividual, Integer>[][] individualsAndSizes) {
    if (Objects.isNull(individualsAndSizes) || individualsAndSizes.length == 0) {
      return;
    }
    AffineTransform previousTransform = setTransform(g, arena, configuration);
    drawArena(g, configuration);
    int[][] visitCounter = new int[(int) Math.ceil(arena.xExtent() / configuration.descriptorTick.x() - 0.0001)]
        [(int) Math.ceil(arena.xExtent() / configuration.descriptorTick.x() - 0.0001)];
    double[][] circleRadiusPerPosition = new double[visitCounter.length][visitCounter[0].length];
    List<Double>[][] rankCounter = new List[visitCounter.length][visitCounter[0].length];
    Arrays.stream(visitCounter).forEach(a -> Arrays.fill(a, 0));
    Arrays.stream(circleRadiusPerPosition).forEach(a -> Arrays.fill(a, 0));
    for (int i = 0; i < rankCounter.length; ++i) {
      for (int j = 0; j < rankCounter[0].length; ++j) {
        rankCounter[i][j] = new ArrayList<>();
      }
    }
    for (Pair<MEIndividual, Integer>[] run : individualsAndSizes) {
      for (Pair<MEIndividual, Integer> individual : run) {
        ++visitCounter[individual.first().bin1()][individual.first().bin2()];
        rankCounter[individual.first().bin1()][individual.first().bin2()].add(
            (double) individual.first().rank() / individual.second());
      }
    }
    g.setStroke(new BasicStroke(
        (float) (configuration.circleThickness / g.getTransform().getScaleX())));
    final double invertedMaximumVisits = 1d
        / Math.max(
            Arrays.stream(visitCounter)
                    .map(a -> Arrays.stream(a).max().orElse(1))
                    .max(Comparator.comparingInt(i -> i))
                    .orElse(1)
                - 1,
            1);
    // draw position circles
    switch (configuration.mode) {
      case BASE:
        for (int i = 0; i < visitCounter.length; ++i) {
          for (int j = 0; j < visitCounter[i].length; ++j) {
            if (visitCounter[i][j] != 0) {
              double visitPercentage = visitCounter[i][j] * invertedMaximumVisits;
              Color color = getColor(Color.GREEN, Color.YELLOW, Color.RED, visitPercentage);
              Ellipse2D circle = new Ellipse2D.Double(
                  configuration.descriptorTick.x() * (i + .5) - configuration.circleRadius,
                  configuration.descriptorTick.y() * (j + .5) - configuration.circleRadius,
                  2 * configuration.circleRadius,
                  2 * configuration.circleRadius);
              g.setColor(GraphicsUtils.alphaed(color, configuration.pointInternAlpha));
              g.fill(circle);
              g.setColor(color);
              g.draw(circle);
              circleRadiusPerPosition[i][j] = configuration.circleRadius;
            }
          }
        }
        break;
      case RANK:
        for (int i = 0; i < visitCounter.length; ++i) {
          for (int j = 0; j < visitCounter[i].length; ++j) {
            if (visitCounter[i][j] != 0) {
              double rankPercentage = rankCounter[i][j].stream()
                  .mapToDouble(d -> d)
                  .max()
                  .orElse(0);
              double visitPercentage = visitCounter[i][j] * invertedMaximumVisits;
              double circleRadius = configuration.circleRadius * (visitPercentage * .7 + .3);
              Color color = getColor(Color.GREEN, Color.YELLOW, Color.RED, rankPercentage);
              Ellipse2D circle = new Ellipse2D.Double(
                  configuration.descriptorTick.x() * (i + .5) - circleRadius,
                  configuration.descriptorTick.y() * (j + .5) - circleRadius,
                  2 * circleRadius,
                  2 * circleRadius);
              g.setColor(GraphicsUtils.alphaed(color, configuration.pointInternAlpha));
              g.fill(circle);
              g.setColor(color);
              g.draw(circle);
              circleRadiusPerPosition[i][j] = circleRadius;
            }
          }
        }
    }

    Map<Pair<Integer, Integer>, Integer> locations = new HashMap<>();
    g.setColor(configuration.arrowColor);
    // draw arrows
    for (Pair<MEIndividual, Integer>[] run : individualsAndSizes) {
      int prevBinX = run[0].first().bin1();
      int prevBinY = run[0].first().bin2();
      for (int i = 1; i < run.length; ++i) {
        int currBinX = run[i].first().bin1();
        int currBinY = run[i].first().bin2();
        if (currBinX == prevBinX
            && currBinY == prevBinY
            && run[i].first().fitness() != run[i - 1].first().fitness()) {
          // a surprise tool that will help us later
          Pair<Integer, Integer> p = new Pair<>(prevBinX, prevBinY);
          if (locations.containsKey(p)) {
            locations.put(p, locations.get(p) + 1);
          } else {
            locations.put(p, 1);
          }
        } else if (currBinX != prevBinX || currBinY != prevBinY) {
          Point p1 = new Point(
              (prevBinX + .5) * configuration.descriptorTick.x(),
              (prevBinY + .5) * configuration.descriptorTick.y());
          Point p2 = new Point(
              (currBinX + .5) * configuration.descriptorTick.x(),
              (currBinY + .5) * configuration.descriptorTick.y());
          Point outOfCircleDirection = p2.diff(p1);
          outOfCircleDirection = outOfCircleDirection.scale(1 / outOfCircleDirection.magnitude());
          Point outOfCircleDirection1 =
              outOfCircleDirection.scale(circleRadiusPerPosition[prevBinX][prevBinY]);
          Point outOfCircleDirection2 =
              outOfCircleDirection.scale(circleRadiusPerPosition[currBinX][currBinY]);
          p1 = p1.sum(outOfCircleDirection1);
          p2 = p2.diff(outOfCircleDirection2);
          drawArrow(g, p1, p2);
          prevBinX = currBinX;
          prevBinY = currBinY;
        }
      }
    }
    // draw ticks
    g.setColor(configuration.tickColor);
    for (Pair<Integer, Integer> p : locations.keySet()) {
      Point posPoint = new Point(
          (p.first() + .5) * configuration.descriptorTick.x(),
          (p.second() + .5) * configuration.descriptorTick.y());
      double angleTick = 2 * Math.PI / Math.max(2, locations.get(p));
      for (int j = 0; j < locations.get(p); ++j) {
        Point rotatedCircleSurface = new Point(
                Math.cos(-Math.PI / 7 + angleTick * j), Math.sin(-Math.PI / 7 + angleTick * j))
            .scale(circleRadiusPerPosition[p.first()][p.second()]);
        Point rotatedArrowStart = rotatedCircleSurface.scale((rotatedCircleSurface.magnitude()
                + .2 * Math.max(configuration.descriptorTick.x(), configuration.descriptorTick.y()))
            / rotatedCircleSurface.magnitude());
        drawArrow(g, posPoint.sum(rotatedArrowStart), posPoint.sum(rotatedCircleSurface));
      }
    }
    g.setTransform(previousTransform);
  }
}
