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
import io.github.ericmedvet.jsdynsym.control.SingleAgentTask;
import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jsdynsym.control.navigation.PointNavigationDrawer;
import io.github.ericmedvet.jsdynsym.control.navigation.PointNavigationEnvironment;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.BaseTrajectoryDrawer;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.MEIndividual;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.MapElitesTrajectoryDrawer;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.RankBasedTrajectoryDrawer;
import io.github.ericmedvet.jsdynsym.core.numerical.MultiDimensionPolynomial;
import io.github.ericmedvet.jviz.core.drawer.ImageBuilder;
import java.io.*;
import java.util.*;

@SuppressWarnings("unused")
public class Main {
  private static final String path = "/home/francescorusin/Desktop/Work/MapElites/GA/";

  public static void main(String[] args) throws Exception {
    for (String robot : List.of("pointnav_", "robotnav_")) {
      for (String arena : List.of("blocky_", "maze_")) {
        for (String controller : List.of("poly", "nn", "tree")) {
          //STNDraw(robot + arena + controller);
          GabrielaRefactor(robot + arena + controller, true);
        }
      }
    }
  }

  public static void polyTest() {
    MultiDimensionPolynomial poly = new MultiDimensionPolynomial(3, 2, 2);
    poly.setParams(new double[]{
            1d, 1d, 0d, 1d, 0d, 0d, 1d, 0d, 0d, 0d,
            0d, 0d, 1d, 0d, 0d, 1d, 0d, 0d, 0d, 1d
    });
    System.out.println(Arrays.stream(poly.compute(.5, -1d, 1d)).boxed().toList());
  }

  public static void arenaDraw(Arena.Prepared arena) throws IOException {
    final PointNavigationDrawer drawer = new PointNavigationDrawer(PointNavigationDrawer.Configuration.DEFAULT);
    drawer.save(
            new ImageBuilder.ImageInfo(500, 500),
            new File(path + arena.name() + ".png"),
            () -> new TreeMap<>(Map.of(
                    0d,
                    new SingleAgentTask.Step<>(
                            new double[]{0d, 0d},
                            new double[]{0d, 0d},
                            new PointNavigationEnvironment.State(
                                    new PointNavigationEnvironment.Configuration(
                                            new DoubleRange(.5, .5),
                                            new DoubleRange(.75, .75),
                                            new DoubleRange(.5, .5),
                                            new DoubleRange(.15, .15),
                                            1,
                                            1,
                                            arena.arena(),
                                            true,
                                            new Random()),
                                    new Point(.5, .15),
                                    new Point(.5, .75),
                                    0)))));
  }

  // BUGGED!
  public static void baseDraw(String file) throws IOException {
    final BufferedReader reader = new BufferedReader(new FileReader(path + "Csv/" + file + "_bests.csv"));
    final String controllerString = extractController(file);
    final Arena arena = extractArena(file);
    BaseTrajectoryDrawer drawer = new BaseTrajectoryDrawer(arena, BaseTrajectoryDrawer.Mode.GRADIENT);
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
            new File(path + "Drawings/%s/%s_opt_trajectory_grad.png".formatted(controllerString, file)),
            trajectories);
    for (int i = 0; i < trajectories.length; ++i) {
      drawer.save(
              new ImageBuilder.ImageInfo(500, 500),
              new File(path + "Drawings/%s/%s_opt_trajectory_grad_%d.png".formatted(controllerString, file, i)),
              new Point[][]{trajectories[i]});
    }
  }

  // BUGGED!
  public static void MERankDraw(String file) throws IOException {
    final BufferedReader individualReader = new BufferedReader(new FileReader(path + "Csv/" + file + "_bests.csv"));
    final BufferedReader sizeReader = new BufferedReader(new FileReader(path + "Csv/" + file + "_sizes.csv"));
    final String controllerString = extractController(file);
    final Arena arena = extractArena(file);
    RankBasedTrajectoryDrawer drawer = new RankBasedTrajectoryDrawer(arena);
    MEIndividual[][] individuals = new MEIndividual[10][400];
    individualReader.readLine();
    sizeReader.readLine();
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 400; ++j) {
        // seed;fitness;id;parent_id;absolute_rank;relative_rank;final_x;final_y;bin_x;bin_y
        String[] splitLine = individualReader.readLine().split(",");
        individuals[i][j] = new MEIndividual(
                new Point(Double.parseDouble(splitLine[6]), Double.parseDouble(splitLine[7])),
                Double.parseDouble(splitLine[1]),
                Integer.parseInt(splitLine[4]),
                Double.parseDouble(splitLine[5]),
                Integer.parseInt(splitLine[8]),
                Integer.parseInt(splitLine[9]));
      }
    }
    drawer.save(
            new ImageBuilder.ImageInfo(500, 500),
            new File(path + "Drawings/%s/%s_opt_trajectory_rank.png".formatted(controllerString, file)),
            individuals);
    for (int i = 0; i < individuals.length; ++i) {
      drawer.save(
              new ImageBuilder.ImageInfo(500, 500),
              new File(path + "Drawings/%s/%s_opt_trajectory_rank_%d.png".formatted(controllerString, file, i)),
              new MEIndividual[][]{individuals[i]});
    }
  }

  public static void STNDraw(String file) throws IOException {
    final int nOfDescriptors = 10;
    final BufferedReader individualReader = new BufferedReader(new FileReader(path + "Csv/" + file + "_bests.csv"));
    final String controllerString = extractController(file);
    final Arena arena = extractArena(file);
    MapElitesTrajectoryDrawer drawer = new MapElitesTrajectoryDrawer(
            arena, MapElitesTrajectoryDrawer.Mode.RANK, new Point(1d / nOfDescriptors, 1d / nOfDescriptors));
    MEIndividual[][] individuals = new MEIndividual[10][500];
    individualReader.readLine();
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 500; ++j) {
        // seed;fitness;id;parent_id;absolute_rank;relative_rank;final_x;final_y;bin_x;bin_y
        String[] splitLine = individualReader.readLine().split(","); // ; for GA
        individuals[i][j] = new MEIndividual(
                new Point(Double.parseDouble(splitLine[6]), Double.parseDouble(splitLine[7])),
                Double.parseDouble(splitLine[1]),
                Integer.parseInt(splitLine[4]),
                Double.parseDouble(splitLine[5]),
                Integer.parseInt(splitLine[8]),
                Integer.parseInt(splitLine[9]));
        /*individuals[i][j] = new MEIndividual(
                new Point(Double.parseDouble(splitLine[5]), Double.parseDouble(splitLine[6])),
                Double.parseDouble(splitLine[1]),
                Integer.parseInt(splitLine[4]),
                Double.parseDouble(splitLine[4]) / 199,
                (int) (Double.parseDouble(splitLine[5]) * 10), (int) (Double.parseDouble(splitLine[6]) * 10)
        );*/
      }
    }
    for (int i = 0; i < 10; ++i) {
      drawer.save(
              new ImageBuilder.ImageInfo(500, 500),
              new File(path
                      + "Drawings/%s/%s_opt_trajectory_stn_rank_%d.png".formatted(controllerString, file, i)),
              new MEIndividual[][]{individuals[i]});
    }
  }

  private static Arena extractArena(String expFile) {
    return switch (expFile.split("_")[1]) {
      case "barrier" -> Arena.Prepared.A_BARRIER.arena();
      case "maze" -> Arena.Prepared.DECIMAL_MAZE.arena();
      case "blocky" -> Arena.Prepared.BLOCKY_MAZE.arena();
      default -> Arena.Prepared.EMPTY.arena();
    };
  }

  private static String extractController(String expFile) {
    String arenaString = expFile.split("_")[2];
    if (arenaString.equals("nn")) {
      arenaString = "NN";
    }
    return arenaString.substring(0, 1).toUpperCase() + arenaString.substring(1);
  }

  public static void GabrielaRefactor(String file, boolean GA) throws IOException {
    final int nOfDescriptors = 10;
    final BufferedReader individualReader = new BufferedReader(new FileReader(path + "Csv/" + file + "_bests.csv"));
    final BufferedWriter individualWriter = new BufferedWriter(new FileWriter(path + "Csv/Upload/" + file + (GA ? "_ga" : "_me") + ".csv"));
    individualWriter.write("seed,fitness,id,parent_id,absolute_rank,relative_rank,final_x,final_y,bin_1,bin_2\n");
    final String controllerString = extractController(file);
    final Arena arena = extractArena(file);
    MEIndividual ind;
    individualReader.readLine();
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 500; ++j) {
        // seed;fitness;id;parent_id;absolute_rank;relative_rank;final_x;final_y;bin_x;bin_y
        String[] splitLine = individualReader.readLine().split(GA ? ";" : ","); // ; for GA
        ind = GA ? new MEIndividual(
                new Point(Double.parseDouble(splitLine[5]), Double.parseDouble(splitLine[6])),
                Double.parseDouble(splitLine[1]),
                Integer.parseInt(splitLine[4]),
                Double.parseDouble(splitLine[4]) / 199,
                (int) (Double.parseDouble(splitLine[5]) * nOfDescriptors),
                (int) (Double.parseDouble(splitLine[6]) * nOfDescriptors)
        ) : new MEIndividual(
                new Point(Double.parseDouble(splitLine[6]), Double.parseDouble(splitLine[7])),
                Double.parseDouble(splitLine[1]),
                Integer.parseInt(splitLine[4]),
                Double.parseDouble(splitLine[5]),
                Integer.parseInt(splitLine[8]),
                Integer.parseInt(splitLine[9]));
        individualWriter.write("%d,%f,%d,%d,%d,%f,%f,%f,%d,%d\n".formatted(
                        i, ind.fitness(), Integer.parseInt(splitLine[2]), Integer.parseInt(splitLine[3]),
                        ind.absolute_rank(), ind.relative_rank(), ind.point().x(), ind.point().y(), ind.bin1(), ind.bin2()
                )
        );
      }
    }
    individualWriter.close();
  }
}
