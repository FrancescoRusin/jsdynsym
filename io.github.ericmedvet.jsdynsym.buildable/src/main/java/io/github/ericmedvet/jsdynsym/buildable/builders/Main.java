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

import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.BaseTrajectoryDrawer;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.MEIndividual;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.MapElitesTrajectoryDrawer;
import io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers.RankBasedTrajectoryDrawer;
import io.github.ericmedvet.jviz.core.drawer.ImageBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException {
    STNDraw();
  }

  public static void baseDraw() throws IOException {
    final String alg = "me";
    final BufferedReader reader = new BufferedReader(new FileReader(
            "/home/francescorusin/Desktop/Work/MapElites/Poly/pointnav_%s_poly_bests.csv".formatted(alg)));
    BaseTrajectoryDrawer drawer = new BaseTrajectoryDrawer(Arena.Prepared.E_MAZE.arena(), BaseTrajectoryDrawer.Mode.GRADIENT);
    Point[][] trajectories = new Point[10][400];
    reader.readLine();
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 400; ++j) {
        String[] splitLine = reader.readLine().split(";");
        trajectories[i][j] = new Point(Double.parseDouble(splitLine[5]), Double.parseDouble(splitLine[6]));
      }
    }
    drawer.save(new ImageBuilder.ImageInfo(500, 500),
            new File(
                    "/home/francescorusin/Desktop/Work/MapElites/Poly/Drawings/%s/pointnav_%s_poly_opt_trajectory_grad.png".formatted(alg.toUpperCase(), alg)),
            trajectories);
    for (int i = 0; i < trajectories.length; ++i) {
      drawer.save(new ImageBuilder.ImageInfo(500, 500),
              new File("/home/francescorusin/Desktop/Work/MapElites/Poly/Drawings/%s/pointnav_%s_poly_opt_trajectory_grad_%d.png".formatted(alg.toUpperCase(), alg, i)),
              new Point[][]{trajectories[i]});
    }
  }

  public static void MERankDraw() throws IOException {
    final BufferedReader individualReader = new BufferedReader(new FileReader(
            "/home/francescorusin/Desktop/Work/MapElites/Poly/pointnav_me_poly_bests.csv"));
    final BufferedReader sizeReader = new BufferedReader(new FileReader(
            "/home/francescorusin/Desktop/Work/MapElites/Poly/pointnav_me_poly_sizes.csv"));
    RankBasedTrajectoryDrawer drawer = new RankBasedTrajectoryDrawer(Arena.Prepared.E_MAZE.arena());
    Pair<MEIndividual, Integer>[][] individuals = new Pair[10][400];
    individualReader.readLine();
    sizeReader.readLine();
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 400; ++j) {
        //seed;fitness;id;parent_id;rank;final_x;final_y
        String[] splitLine = individualReader.readLine().split(";");
        individuals[i][j] = new Pair<>(new MEIndividual(new Point(Double.parseDouble(splitLine[5]), Double.parseDouble(splitLine[6])),
                Double.parseDouble(splitLine[1]), Integer.parseInt(splitLine[4]),
                (int) Math.floor(Double.parseDouble(splitLine[5]) * 20),
                (int) Math.floor(Double.parseDouble(splitLine[6]) * 20)), 
                Integer.parseInt(sizeReader.readLine().split(";")[2])
        );
      }
    }
    drawer.save(new ImageBuilder.ImageInfo(500, 500),
            new File("/home/francescorusin/Desktop/Work/MapElites/Poly/Drawings/ME/pointnav_me_poly_opt_trajectory_rank.png"),
            individuals);
    for (int i = 0; i < individuals.length; ++i) {
      drawer.save(new ImageBuilder.ImageInfo(500, 500),
              new File("/home/francescorusin/Desktop/Work/MapElites/Poly/Drawings/ME/pointnav_me_poly_opt_trajectory_rank_%d.png".formatted(i)),
              new Pair[][]{individuals[i]});
    }
  }

  public static void STNDraw() throws IOException {
    final BufferedReader reader = new BufferedReader(new FileReader(
            "/home/francescorusin/Desktop/Work/MapElites/Poly/pointnav_me_poly_bests.csv"));
    MapElitesTrajectoryDrawer drawer = new MapElitesTrajectoryDrawer(Arena.Prepared.FLAT_MAZE.arena(), new Point(.05, .05));
    MEIndividual[][] individuals = new MEIndividual[10][400];
    reader.readLine();
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 400; ++j) {
        //seed;fitness;id;parent_id;rank;final_x;final_y
        String[] splitLine = reader.readLine().split(";");
        individuals[i][j] = new MEIndividual(new Point(Double.parseDouble(splitLine[5]), Double.parseDouble(splitLine[6])),
                Double.parseDouble(splitLine[1]), Integer.parseInt(splitLine[4]),
                (int) Math.floor(Double.parseDouble(splitLine[5]) * 20),
                (int) Math.floor(Double.parseDouble(splitLine[6]) * 20));
      }
    }
    for (int i = 0; i < individuals.length; ++i) {
      drawer.save(new ImageBuilder.ImageInfo(500, 500),
              new File("/home/francescorusin/Desktop/Work/MapElites/Poly/Drawings/ME/pointnav_me_poly_opt_trajectory_stn_%d.png".formatted(i)),
              individuals[i]);
    }
  }
}