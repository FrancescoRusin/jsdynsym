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

import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jsdynsym.control.navigation.TrajectoryDrawer;
import io.github.ericmedvet.jviz.core.drawer.ImageBuilder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class Main {
  public static void main(String[] args) throws IOException {
    final String alg = "me";
    final BufferedReader reader = new BufferedReader(new FileReader(
            String.format("/home/francescorusin/Desktop/Work/MapElites/Poly/pointnav_%s_poly_bests.csv", alg)));
    TrajectoryDrawer drawer = new TrajectoryDrawer(Arena.Prepared.E_MAZE.arena(), TrajectoryDrawer.MovementMode.LINE, TrajectoryDrawer.ColorMode.GRADIENT);
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
                    String.format("/home/francescorusin/Desktop/Work/MapElites/Poly/Drawings/%s/pointnav_%s_poly_opt_trajectory_grad.png", alg.toUpperCase(), alg)),
            trajectories);
    for (int i = 0; i < trajectories.length; ++i) {
      drawer.save(new ImageBuilder.ImageInfo(500, 500),
              new File(String.format("/home/francescorusin/Desktop/Work/MapElites/Poly/Drawings/%s/pointnav_%s_poly_opt_trajectory_grad_%d.png", alg.toUpperCase(), alg, i)),
              new Point[][]{trajectories[i]});
    }
  }
}
