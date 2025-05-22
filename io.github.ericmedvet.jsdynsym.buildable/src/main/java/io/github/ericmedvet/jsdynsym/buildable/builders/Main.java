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

package io.github.ericmedvet.jsdynsym.buildable.builders;

import io.github.ericmedvet.jnb.core.NamedBuilder;
import io.github.ericmedvet.jsdynsym.control.Simulation;
import io.github.ericmedvet.jsdynsym.control.SingleAgentTask;
import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.NavigationEnvironment;
import io.github.ericmedvet.jsdynsym.control.navigation.PointNavigationEnvironment;
import io.github.ericmedvet.jsdynsym.control.navigation.State;
import io.github.ericmedvet.jsdynsym.core.StatelessSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.MultiDimensionPolynomial;
import io.github.ericmedvet.jsdynsym.core.numerical.MultivariateRealFunction;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.ann.MultiLayerPerceptron;

import java.io.*;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class Main {
  private static final String path = "C:\\Users\\Francesco\\Desktop\\Università\\Dottorato\\Ricerca\\Locality\\";

  public static void main(String[] args) throws IOException {
    Locale.setDefault(Locale.US);
    for (String controller : List.of("mlp", "poly")) {
      for (String arena : List.of("blocky", "maze", "empty")) {
        pointSearch("%s-avgD".formatted(arena), controller);
        //robotSearch("%s-avgD".formatted(arena), controller);
      }
    }
  }

  private static Object base64Deserializer(String serialized) {
    byte[] bytes = Base64.getDecoder().decode(serialized);
    try (ObjectInputStream oois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return oois.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }

  private record Individual(double[] genotype, double fitness, double c1, double c2) {
  }

  private static String getArena(String exp) {
    if (exp.contains("blocky")) {
      return "BLOCKY_MAZE";
    }
    if (exp.contains("maze")) {
      return "DECIMAL_MAZE";
    }
    if (exp.contains("empty")) {
      return "EMPTY";
    }
    return "";
  }

  @SuppressWarnings("unchecked")
  public static void pointSearch(String exp, String contrString) throws IOException {
    NamedBuilder<?> nb = NamedBuilder.fromDiscovery();
    Supplier<SingleAgentTask<NumericalDynamicalSystem<?>, double[], double[], PointNavigationEnvironment.State>> taskSupplier =
            () -> (SingleAgentTask<NumericalDynamicalSystem<?>, double[], double[], PointNavigationEnvironment.State>)
                    nb.build(
                            "ds.sat.fromEnvironment(" +
                                    "  environment = ds.e.pointNavigation(" +
                                    "    arena = %s;".formatted(getArena(exp)) +
                                    "    initialRobotXRange = m.range(min = 0.5; max = 0.5);" +
                                    "    initialRobotYRange = m.range(min = 0.75; max = 0.75);" +
                                    "    robotMaxV = 0.05" +
                                    "  );" +
                                    "  tRange = m.range(min = 0.0; max = 30.0);" +
                                    "  dT = 0.1" +
                                    ")"
                    );
    final Function<double[], MultivariateRealFunction> controllerSupplier = switch (contrString) {
      case "mlp" -> d -> {
        MultiLayerPerceptron mlp = ((NumericalDynamicalSystems.Builder<MultiLayerPerceptron, StatelessSystem.State>)
                nb.build("ds.num.mlp(innerLayerRatio = 1.0)")).apply(2, 2);
        mlp.setParams(d);
        return mlp;
      };
      case "poly" -> d -> {
        MultiDimensionPolynomial poly = ((NumericalDynamicalSystems.Builder<MultiDimensionPolynomial, StatelessSystem.State>)
                nb.build("ds.num.mdPolynomial(degree = 3)")).apply(2, 2);
        poly.setParams(d);
        return poly;
      };
      default -> null;
    };
    final Function<Simulation.Outcome<SingleAgentTask.Step<double[], double[], PointNavigationEnvironment.State>>, Point> finalPos =
            o -> o.snapshots().get(o.snapshots().lastKey()).state().robotPosition();
    final Function<Simulation.Outcome<SingleAgentTask.Step<double[], double[], PointNavigationEnvironment.State>>, Double> avgD =
            o -> o.snapshots().values().stream().mapToDouble(s -> s.state().robotPosition().distance(s.state().targetPosition())).average().orElseThrow();
    final BufferedReader reader = new BufferedReader(new FileReader("%s\\Csv\\point-%s-all.csv".formatted(path, exp)));
    String line = reader.readLine();
    List<String> splitLine = Arrays.stream(line.split(";")).toList();
    final int solverIndex = splitLine.indexOf("run.solver.name");
    final int seedIndex = splitLine.indexOf("run.randomGenerator.seed");
    final int itIndex = splitLine.indexOf("state→n.iterations");
    final int fitnessIndex = splitLine.indexOf("individual→quality→behavior.quality→avg.dist");
    final int c1Index = splitLine.indexOf("individual→coords→[0]→bin");
    final int v1Index = splitLine.indexOf("individual→coords→[0]→value");
    final int genotypeIndex = splitLine.indexOf("individual→genotype→to.base64");
    Individual[][][][] individuals = new Individual[10][10][20][20];
    while (Objects.nonNull(line = reader.readLine())) {
      String[] aLine = line.split(";");
      if (aLine[solverIndex].contains(contrString)) {
        individuals[-1 - Integer.parseInt(aLine[seedIndex])][Integer.parseInt(aLine[itIndex]) / 10 - 1]
                [Integer.parseInt(aLine[c1Index])][Integer.parseInt(aLine[c1Index + 1])] =
                new Individual(
                        ((List<Double>) base64Deserializer(aLine[genotypeIndex])).stream().mapToDouble(d -> d).toArray(),
                        Double.parseDouble(aLine[fitnessIndex]),
                        Double.parseDouble(aLine[v1Index]),
                        Double.parseDouble(aLine[v1Index + 1])
                );
      }
    }
    //ExecutorService threader = Executors.newFixedThreadPool(10);
    //final BufferedWriter writer = new BufferedWriter(new FileWriter("%s\\Csv\\point-%s-%s-paths.csv".formatted(path, exp, contrString)));
    //writer.write("seed;gen;p1.x;p1.y;p2.x;p2.y;path.og;path.ticks\n");
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 10; ++j) {
        for (int x = 0; x < 20; ++x) {
          for (int y = 0; y < 20; ++y) {
            if (Objects.nonNull(individuals[i][j][x][y])) {
              final Individual i1 = individuals[i][j][x][y];
              MultivariateRealFunction controller = Objects.requireNonNull(controllerSupplier).apply(i1.genotype);
              double res = avgD.apply(taskSupplier.get().simulate(controller));
              if (Math.abs(res - i1.fitness) > 1e-4) {
                System.out.println("PORCA MADONNA");
              }
              /*for (int[] neighs : List.of(new int[]{1, 0}, new int[]{0, 1})) {
                if (x + neighs[0] < 20 && y + neighs[1] < 20 && Objects.nonNull(individuals[i][j][x + neighs[0]][y + neighs[1]])) {
                  final Individual i2 = individuals[i][j][x + neighs[0]][y + neighs[1]];
                  final List<Callable<Point>> intermediates = new ArrayList<>();
                  for (int k = 1; k < 11; ++k) {
                    final double tick = k / 11d;
                    intermediates.add(() -> {
                      final double[] newGenotype = IntStream.range(0, i1.genotype.length)
                              .mapToDouble(index -> i1.genotype[index] * (1d - tick) + i2.genotype[index] * tick)
                              .toArray();
                      MultivariateRealFunction controller = Objects.requireNonNull(controllerSupplier).apply(newGenotype);
                      return finalPos.apply(taskSupplier.get().simulate(controller));
                    });
                  }
                  List<Point> results = new ArrayList<>();
                  results.add(new Point(i1.c1, i1.c2));
                  try {
                    results.addAll(threader.invokeAll(intermediates).stream().map(f -> {
                      try {
                        return f.get();
                      } catch (Exception e) {
                        throw new RuntimeException(e);
                      }
                    }).toList());
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                  results.add(new Point(i2.c1, i2.c2));
                  final double ogDist = Math.sqrt(Math.pow(i1.c1 - i2.c1, 2) + Math.pow(i1.c2 - i2.c2, 2));
                  final double tickDist = IntStream.range(0, results.size() - 1)
                          .mapToDouble(index -> results.get(index).distance(results.get(index + 1)))
                          .sum();
                  //seed;gen;p1.x;p1.y;p2.x;p2.y;path.og;path.ticks;mid.points
                  writer.write("%d;%d;%d;%d;%d;%d;%f;%f\n".formatted(i, j, x, y, x + neighs[0], y + neighs[1], ogDist, tickDist));
                }
              }*/
            }
          }
        }
      }
    }
    //threader.shutdown();
    //writer.close();
  }

  @SuppressWarnings("unchecked")
  public static void robotSearch(String exp, String contrString) throws IOException {
    NamedBuilder<?> nb = NamedBuilder.fromDiscovery();
    Supplier<SingleAgentTask<NumericalDynamicalSystem<?>, double[], double[], NavigationEnvironment.State>> taskSupplier =
            () -> (SingleAgentTask<NumericalDynamicalSystem<?>, double[], double[], NavigationEnvironment.State>)
                    nb.build(
                            "ds.sat.fromEnvironment(" +
                                    "  environment = ds.e.navigation(" +
                                    "    arena = %s;".formatted(getArena(exp)) +
                                    "    initialRobotXRange = m.range(min = 0.5; max = 0.5);" +
                                    "    initialRobotYRange = m.range(min = 0.75; max = 0.75);" +
                                    "    robotMaxV = 0.05;" +
                                    "    nOfSensors = 4" +
                                    "  );" +
                                    "  tRange = m.range(min = 0.0; max = 30.0);" +
                                    "  dT = 0.1" +
                                    ")"
                    );
    final Function<double[], MultivariateRealFunction> controllerSupplier = switch (contrString) {
      case "mlp" -> d -> {
        MultiLayerPerceptron mlp = ((NumericalDynamicalSystems.Builder<MultiLayerPerceptron, StatelessSystem.State>)
                nb.build("ds.num.mlp(innerLayerRatio = 1.0)")).apply(7, 2);
        mlp.setParams(d);
        return mlp;
      };
      case "poly" -> d -> {
        MultiDimensionPolynomial poly = ((NumericalDynamicalSystems.Builder<MultiDimensionPolynomial, StatelessSystem.State>)
                nb.build("ds.num.mdPolynomial(degree = 3)")).apply(7, 2);
        poly.setParams(d);
        return poly;
      };
      default -> null;
    };
    final Function<Simulation.Outcome<SingleAgentTask.Step<double[], double[], NavigationEnvironment.State>>, Point> finalPos =
            o -> o.snapshots().get(o.snapshots().lastKey()).state().robotPosition();
    final BufferedReader reader = new BufferedReader(new FileReader("%s\\Csv\\robot-%s-all.csv".formatted(path, exp)));
    String line = reader.readLine();
    List<String> splitLine = Arrays.stream(line.split(";")).toList();
    final int solverIndex = splitLine.indexOf("run.solver.name");
    final int seedIndex = splitLine.indexOf("run.randomGenerator.seed");
    final int itIndex = splitLine.indexOf("state→n.iterations");
    final int fitnessIndex = splitLine.indexOf("individual→quality→behavior.quality→avg.dist");
    final int c1Index = splitLine.indexOf("individual→coords→[0]→bin");
    final int v1Index = splitLine.indexOf("individual→coords→[0]→value");
    final int genotypeIndex = splitLine.indexOf("individual→genotype→to.base64");
    Individual[][][][] individuals = new Individual[10][10][20][20];
    while (Objects.nonNull(line = reader.readLine())) {
      String[] aLine = line.split(";");
      if (aLine[solverIndex].contains(contrString)) {
        individuals[-1 - Integer.parseInt(aLine[seedIndex])][Integer.parseInt(aLine[itIndex]) / 10 - 1]
                [Integer.parseInt(aLine[c1Index])][Integer.parseInt(aLine[c1Index + 1])] =
                new Individual(
                        ((List<Double>) base64Deserializer(aLine[genotypeIndex])).stream().mapToDouble(d -> d).toArray(),
                        Double.parseDouble(aLine[fitnessIndex]),
                        Double.parseDouble(aLine[v1Index]),
                        Double.parseDouble(aLine[v1Index + 1])
                );
      }
    }
    ExecutorService threader = Executors.newFixedThreadPool(10);
    final BufferedWriter writer = new BufferedWriter(new FileWriter("%s\\Csv\\robot-%s-%s-paths.csv".formatted(path, exp, contrString)));
    writer.write("seed;gen;p1.x;p1.y;p2.x;p2.y;path.og;path.ticks\n");
    for (int i = 0; i < 10; ++i) {
      for (int j = 0; j < 10; ++j) {
        for (int x = 0; x < 20; ++x) {
          for (int y = 0; y < 20; ++y) {
            if (Objects.nonNull(individuals[i][j][x][y])) {
              final Individual i1 = individuals[i][j][x][y];
              for (int[] neighs : List.of(new int[]{1, 0}, new int[]{0, 1})) {
                if (x + neighs[0] < 20 && y + neighs[1] < 20 && Objects.nonNull(individuals[i][j][x + neighs[0]][y + neighs[1]])) {
                  final Individual i2 = individuals[i][j][x + neighs[0]][y + neighs[1]];
                  final List<Callable<Point>> intermediates = new ArrayList<>();
                  for (int k = 1; k < 11; ++k) {
                    final double tick = k / 11d;
                    intermediates.add(() -> {
                      final double[] newGenotype = IntStream.range(0, i1.genotype.length)
                              .mapToDouble(index -> i1.genotype[index] * (1d - tick) + i2.genotype[index] * tick)
                              .toArray();
                      MultivariateRealFunction controller = Objects.requireNonNull(controllerSupplier).apply(newGenotype);
                      return finalPos.apply(taskSupplier.get().simulate(controller));
                    });
                  }
                  List<Point> results = new ArrayList<>();
                  results.add(new Point(i1.c1, i1.c2));
                  try {
                    results.addAll(threader.invokeAll(intermediates).stream().map(f -> {
                      try {
                        return f.get();
                      } catch (Exception e) {
                        throw new RuntimeException(e);
                      }
                    }).toList());
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                  results.add(new Point(i2.c1, i2.c2));
                  final double ogDist = Math.sqrt(Math.pow(i1.c1 - i2.c1, 2) + Math.pow(i1.c2 - i2.c2, 2));
                  final double tickDist = IntStream.range(0, results.size() - 1)
                          .mapToDouble(index -> results.get(index).distance(results.get(index + 1)))
                          .sum();
                  //controller;seed;gen;p1.x;p1.y;p2.x;p2.y;path.og;path.ticks;mid.points
                  writer.write("%d;%d;%d;%d;%d;%d;%f;%f\n".formatted(i, j, x, y, x + neighs[0], y + neighs[1], ogDist, tickDist));
                }
              }
            }
          }
        }
      }
    }
    threader.shutdown();
    writer.close();
  }
}
