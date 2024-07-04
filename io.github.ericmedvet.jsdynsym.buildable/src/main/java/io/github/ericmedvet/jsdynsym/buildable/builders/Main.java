/*-
 * ========================LICENSE_START=================================
 * jsdynsym-buildable
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
/*
 * Copyright 2024 eric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.ericmedvet.jsdynsym.buildable.builders;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jsdynsym.control.SingleAgentTask;
import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jsdynsym.control.navigation.PointNavigationDrawer;
import io.github.ericmedvet.jsdynsym.control.navigation.PointNavigationEnvironment;
import io.github.ericmedvet.jsdynsym.control.navigation.VectorFieldDrawer;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.BaseTrajectoryDrawer;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.MEIndividual;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.MapElitesTrajectoryDrawer;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.RankBasedTrajectoryDrawer;
import io.github.ericmedvet.jsdynsym.core.numerical.MultiDimensionPolynomial;
import io.github.ericmedvet.jsdynsym.core.numerical.MultiDimensionPolynomial2D;
import io.github.ericmedvet.jviz.core.drawer.ImageBuilder;
import java.io.*;
import java.util.*;

@SuppressWarnings("unused, unchecked")
public class Main {
  public static void main(String[] args) throws Exception {
    polyTest();
  }

  public static void polyTest() {
    MultiDimensionPolynomial poly = new MultiDimensionPolynomial(3, 2, 2);
    poly.setParams(new double[] {
      1d, 1d, 0d, 1d, 0d, 0d, 1d, 0d, 0d, 0d,
      0d, 0d, 1d, 0d, 0d, 1d, 0d, 0d, 0d, 1d
    });
    System.out.println(Arrays.stream(poly.compute(.5, -1d, 1d)).boxed().toList());
  }

  public static void vectorFieldDraw() throws IOException, ClassNotFoundException {
    final VectorFieldDrawer drawer =
        new VectorFieldDrawer(Arena.Prepared.DECIMAL_MAZE.arena(), VectorFieldDrawer.Configuration.DEFAULT);
    final BufferedReader reader = new BufferedReader(new FileReader(
        "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/pointnav_me_poly_all.csv"));
    final MultiDimensionPolynomial2D polynomial = new MultiDimensionPolynomial2D(2, 3, true);
    reader.readLine();
    String line = reader.readLine();
    String bestGen = "";
    double bestFitness = Double.MAX_VALUE;
    int prevSeed = -10;
    while (Objects.nonNull(line)) {
      String[] splitLine = line.split(";");
      if (Double.parseDouble(splitLine[5]) < bestFitness) {
        bestGen = splitLine[splitLine.length - 1];
        bestFitness = Double.parseDouble(splitLine[5]);
      }
      if (Integer.parseInt(splitLine[2]) != prevSeed) {
        bestFitness = Double.MAX_VALUE;
        polynomial.setParams(((List<Double>) new ObjectInputStream(new ByteArrayInputStream(
                    Base64.getDecoder().decode(bestGen)))
                .readObject())
            .stream().mapToDouble(d -> d).toArray());
        drawer.save(
            new ImageBuilder.ImageInfo(500, 500),
            new File(
                "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/Drawings/pointnav_me_poly_vecfield_%d.png"
                    .formatted(prevSeed + 10)),
            polynomial);
        ++prevSeed;
      }
      line = reader.readLine();
    }
    polynomial.setParams(((List<Double>) new ObjectInputStream(
                new ByteArrayInputStream(Base64.getDecoder().decode(bestGen)))
            .readObject())
        .stream().mapToDouble(d -> d).toArray());
    drawer.save(
        new ImageBuilder.ImageInfo(500, 500),
        new File(
            "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/Drawings/pointnav_me_poly_vecfield_9.png"),
        polynomial);
  }

  public static void arenaDraw() throws IOException {
    final PointNavigationDrawer drawer = new PointNavigationDrawer(PointNavigationDrawer.Configuration.DEFAULT);
    drawer.save(
        new ImageBuilder.ImageInfo(500, 500),
        new File("/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/Drawings/labyrinth.png"),
        () -> new TreeMap<>(Map.of(
            0d,
            new SingleAgentTask.Step<>(
                new double[] {0d, 0d},
                new double[] {0d, 0d},
                new PointNavigationEnvironment.State(
                    new PointNavigationEnvironment.Configuration(
                        new DoubleRange(.5, .5),
                        new DoubleRange(.75, .75),
                        new DoubleRange(.5, .5),
                        new DoubleRange(.15, .15),
                        1,
                        1,
                        Arena.Prepared.DECIMAL_MAZE.arena(),
                        true,
                        new Random()),
                    new Point(.5, .15),
                    new Point(.5, .75),
                    0)))));
  }

  public static void baseDraw() throws IOException {
    final String alg = "me";
    final BufferedReader reader = new BufferedReader(new FileReader(
        "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/pointnav_me_poly_bests.csv"));
    BaseTrajectoryDrawer drawer =
        new BaseTrajectoryDrawer(Arena.Prepared.DECIMAL_MAZE.arena(), BaseTrajectoryDrawer.Mode.GRADIENT);
    Point[][] trajectories = new Point[10][400];
    reader.readLine();
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 400; ++j) {
        String[] splitLine = reader.readLine().split(",");
        trajectories[i][j] = new Point(Double.parseDouble(splitLine[6]), Double.parseDouble(splitLine[7]));
      }
    }
    drawer.save(
        new ImageBuilder.ImageInfo(500, 500),
        new File(
            "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/Drawings/pointnav_me_poly_opt_trajectory_grad.png"),
        trajectories);
    for (int i = 0; i < trajectories.length; ++i) {
      drawer.save(
          new ImageBuilder.ImageInfo(500, 500),
          new File(
              "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/Drawings/pointnav_me_poly_opt_trajectory_grad_%d.png"
                  .formatted(i)),
          new Point[][] {trajectories[i]});
    }
  }

  public static void MERankDraw() throws IOException {
    final BufferedReader individualReader = new BufferedReader(new FileReader(
        "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/pointnav_me_poly_bests.csv"));
    final BufferedReader sizeReader = new BufferedReader(new FileReader(
        "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/pointnav_me_poly_sizes.csv"));
    RankBasedTrajectoryDrawer drawer = new RankBasedTrajectoryDrawer(Arena.Prepared.DECIMAL_MAZE.arena());
    Pair<MEIndividual, Integer>[][] individuals = new Pair[10][400];
    individualReader.readLine();
    sizeReader.readLine();
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 400; ++j) {
        // seed;fitness;id;parent_id;absolute_rank;relative_rank;final_x;final_y;bin_x;bin_y
        String[] splitLine = individualReader.readLine().split(",");
        individuals[i][j] = new Pair<>(
            new MEIndividual(
                new Point(Double.parseDouble(splitLine[6]), Double.parseDouble(splitLine[7])),
                Double.parseDouble(splitLine[1]),
                Integer.parseInt(splitLine[4]),
                Integer.parseInt(splitLine[8]),
                Integer.parseInt(splitLine[9])),
            Integer.parseInt(sizeReader.readLine().split(";")[2]));
      }
    }
    drawer.save(
        new ImageBuilder.ImageInfo(500, 500),
        new File(
            "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/Drawings/pointnav_me_poly_opt_trajectory_rank.png"),
        individuals);
    for (int i = 0; i < individuals.length; ++i) {
      drawer.save(
          new ImageBuilder.ImageInfo(500, 500),
          new File(
              "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/Drawings/pointnav_me_poly_opt_trajectory_rank_%d.png"
                  .formatted(i)),
          new Pair[][] {individuals[i]});
    }
  }

  public static void STNDraw() throws IOException {
    final int nOfDescriptors = 10;
    final BufferedReader individualReader = new BufferedReader(new FileReader(
        "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/pointnav_me_poly_bests.csv"));
    MapElitesTrajectoryDrawer drawer = new MapElitesTrajectoryDrawer(
        Arena.Prepared.DECIMAL_MAZE.arena(),
        MapElitesTrajectoryDrawer.Mode.RANK,
        new Point(1d / nOfDescriptors, 1d / nOfDescriptors));
    Pair<MEIndividual, Integer>[][] individuals = new Pair[10][400];
    individualReader.readLine();
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 400; ++j) {
        // seed;fitness;id;parent_id;absolute_rank;relative_rank;final_x;final_y;bin_x;bin_y
        String[] splitLine = individualReader.readLine().split(",");
        individuals[i][j] = new Pair<>(
            new MEIndividual(
                new Point(Double.parseDouble(splitLine[6]), Double.parseDouble(splitLine[7])),
                Double.parseDouble(splitLine[1]),
                Integer.parseInt(splitLine[4]),
                Integer.parseInt(splitLine[8]),
                Integer.parseInt(splitLine[9])),
            Integer.parseInt(splitLine[5]));
      }
    }
    for (int i = 0; i < 10; ++i) {
      drawer.save(
          new ImageBuilder.ImageInfo(500, 500),
          new File(
              "/home/francescorusin/Desktop/Work/MapElites/Poly/Decimal_crop/Drawings/pointnav_me_poly_opt_trajectory_stn_rank_%d.png"
                  .formatted(i)),
          new Pair[][] {individuals[i]});
    }
  }
}
