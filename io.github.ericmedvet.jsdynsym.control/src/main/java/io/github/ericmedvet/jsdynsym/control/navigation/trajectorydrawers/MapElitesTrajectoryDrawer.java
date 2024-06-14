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

public class MapElitesTrajectoryDrawer extends AbstractArenaBasedTrajectoryDrawer implements Drawer<MEIndividual[][]> {
  private final METConfiguration configuration;

  public enum Mode {
    BASE, BOINK
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
              Math.min(descriptorTick.x(), descriptorTick.y()) * .2,
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
  public void draw(Graphics2D g, MEIndividual[][] individuals) {
    //TODO RIFARE PER PIÙ RUN INSIEME INVECE CHE SOLO UNA
    AffineTransform previousTransform = setTransform(g, arena, configuration);
    drawArena(g, configuration);
    int[][] visitCounter = new int[(int) Math.ceil(arena.xExtent() / configuration.descriptorTick.x() - 0.0001)]
            [(int) Math.ceil(arena.xExtent() / configuration.descriptorTick.x() - 0.0001)];
    List<Integer>[][] rankCounter = new List[visitCounter.length][visitCounter[0].length];
    if (Objects.isNull(individuals) || individuals.length == 0) {
      return;
    }
    Arrays.stream(visitCounter).forEach(a -> Arrays.fill(a, 0));
    for (int i = 0; i < rankCounter.length; ++i) {
      for (int j = 0; j < rankCounter[0].length; ++j) {
        rankCounter[i][j] = new ArrayList<>();
      }
    }
    for (MEIndividual[] run : individuals) {
      for (MEIndividual individual : run) {
        ++visitCounter[individual.bin1()][individual.bin2()];
        rankCounter[individual.bin1()][individual.bin2()].add(individual.rank());
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
    final double minFitness = Math.max(Arrays.stream(rankCounter).flatMap(Arrays::stream)
            .flatMap(List::stream).min(Comparator.comparingInt(i -> i)).orElse(0), 20);
    final double maxFitness = Arrays.stream(rankCounter).flatMap(Arrays::stream)
            .flatMap(List::stream).max(Comparator.comparingInt(i -> i)).orElse(20);
    switch (configuration.mode) {
      case BASE:
        for (int i = 0; i < visitCounter.length; ++i) {
          for (int j = 0; j < visitCounter[i].length; ++j) {
            if (visitCounter[i][j] != 0) {
              double visitPercentage = (visitCounter[i][j] - 1) * invertedMaximumVisits;
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
            }
          }
        }
        break;
      case BOINK:
        for (int i = 0; i < visitCounter.length; ++i) {
          for (int j = 0; j < visitCounter[i].length; ++j) {
            if (visitCounter[i][j] != 0) {
              double rankPercentage = rankCounter[i][j].stream().mapToInt(d -> d).average().orElse(0);
              Color color = getColor(Color.GREEN, Color.YELLOW, Color.RED, rankPercentage);
              Ellipse2D circle = new Ellipse2D.Double(
                      configuration.descriptorTick.x() * (i + .5) - configuration.circleRadius,
                      configuration.descriptorTick.y() * (j + .5) - configuration.circleRadius,
                      2 * configuration.circleRadius,
                      2 * configuration.circleRadius);
              g.setColor(GraphicsUtils.alphaed(color, configuration.pointInternAlpha));
              g.fill(circle);
              g.setColor(color);
              g.draw(circle);
            }
          }
        }
    }
    int prevBinX = individuals[0].bin1();
    int prevBinY = individuals[0].bin2();
    Map<Pair<Integer, Integer>, Integer> locations = new HashMap<>();
    g.setColor(configuration.arrowColor);
    for (int i = 1; i < individuals.length; ++i) {
      if (individuals[i].bin1() == prevBinX
              && individuals[i].bin2() == prevBinY
              && individuals[i].fitness() != individuals[i - 1].fitness()) {
        Pair<Integer, Integer> p = new Pair<>(prevBinX, prevBinY);
        if (locations.containsKey(p)) {
          locations.put(p, locations.get(p) + 1);
        } else {
          locations.put(p, 1);
        }
      } else {
        Point p1 = new Point(
                (prevBinX + .5) * configuration.descriptorTick.x(),
                (prevBinY + .5) * configuration.descriptorTick.y());
        Point p2 = new Point(
                (individuals[i].bin1() + .5) * configuration.descriptorTick.x(),
                (individuals[i].bin2() + .5) * configuration.descriptorTick.y());
        Point outOfCircleDirection = p2.diff(p1);
        outOfCircleDirection =
                outOfCircleDirection.scale(configuration.circleRadius / outOfCircleDirection.magnitude());
        p1 = p1.sum(outOfCircleDirection);
        p2 = p2.diff(outOfCircleDirection);
        drawArrow(g, p1, p2);
        prevBinX = individuals[i].bin1();
        prevBinY = individuals[i].bin2();
      }
    }
    g.setColor(configuration.tickColor);
    for (Pair<Integer, Integer> p : locations.keySet()) {
      Point posPoint = new Point(
              (p.first() + .5) * configuration.descriptorTick.x(),
              (p.second() + .5) * configuration.descriptorTick.y());
      double angleTick = 2 * Math.PI / Math.max(2, locations.get(p));
      for (int j = 0; j < locations.get(p); ++j) {
        Point rotatedCircleSurface = new Point(
                Math.cos(-Math.PI / 7 + angleTick * j), Math.sin(-Math.PI / 7 + angleTick * j))
                .scale(configuration.circleRadius);
        Point rotatedArrowStart = rotatedCircleSurface.scale(.5
                * Math.max(configuration.descriptorTick.x(), configuration.descriptorTick.y())
                / configuration.circleRadius);
        drawArrow(g, posPoint.sum(rotatedArrowStart), posPoint.sum(rotatedCircleSurface));
      }
    }
    g.setTransform(previousTransform);
  }
}