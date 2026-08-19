/*-
 * ========================LICENSE_START=================================
 * jsdynsym-core
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
package io.github.ericmedvet.jsdynsym.core.rl_2.cartpole;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;
import io.github.ericmedvet.jsdynsym.core.rl_2.RLEpisodicTask;
import java.util.Random;

public class CartPoleProblem implements RLEpisodicTask<double[], Double, CartPoleProblem.CartPoleState> {
  public record CartPoleState(double x, double cartVelocity, double poleAngle, double poleAngVelocity) {}

  private final double poleLength;
  private final double poleMass;
  private final double cartMass;
  private final DoubleRange fRange;
  private final double maxX;
  private final double g;
  private final double timetick;
  private final Random rng;

  private static final double DEFAULT_POLE_LENGTH = 1;
  private static final double DEFAULT_POLE_MASS = .1;
  private static final double DEFAULT_CART_MASS = 1;
  private static final double DEFAULT_MAX_F = 1;
  private static final double DEFAULT_MAX_X = 9;
  private static final double G = 9.81;
  private static final double DEFAULT_TIMETICK = .1;

  public CartPoleProblem(
      double maxF,
      double maxX,
      double poleLength,
      double poleMass,
      double cartMass,
      double g,
      double timetick,
      int seed
  ) {
    this.poleLength = poleLength;
    this.poleMass = poleMass;
    this.cartMass = cartMass;
    this.fRange = new DoubleRange(-maxF, maxF);
    this.maxX = maxX;
    this.g = g;
    this.timetick = timetick;
    this.rng = seed >= 0 ? new Random(seed) : new Random();
  }

  public CartPoleProblem(double maxF, int seed) {
    this(maxF, DEFAULT_MAX_X, DEFAULT_POLE_LENGTH, DEFAULT_POLE_MASS, DEFAULT_CART_MASS, G, DEFAULT_TIMETICK, seed);
  }

  public CartPoleProblem(int seed) {
    this(DEFAULT_MAX_F, seed);
  }

  public CartPoleProblem() {
    this(-1);
  }

  private double linearAcc(CartPoleState state, double action) {
    final double x = state.x;
    final double theta = state.poleAngle;
    final double dx = state.cartVelocity;
    final double dTheta = state.poleAngVelocity;
    final double sinTheta = Math.sin(theta);
    final double cosTheta = Math.cos(theta);
    return (action + poleMass * sinTheta * (poleLength * dTheta * dTheta + g * cosTheta)) / (cartMass + poleMass * sinTheta * sinTheta);
  }

  private double angularAcc(CartPoleState state, double action) {
    final double x = state.x;
    final double theta = state.poleAngle;
    final double dx = state.cartVelocity;
    final double dTheta = state.poleAngVelocity;
    final double sinTheta = Math.sin(theta);
    final double cosTheta = Math.cos(theta);
    return (-action * cosTheta - poleMass * poleLength * dTheta * dTheta * cosTheta * sinTheta + (poleMass + cartMass) * g * sinTheta) / (poleLength * (cartMass + poleMass * sinTheta * sinTheta));
  }

  private double[] rungeKuttaF(CartPoleState state, double action) {
    return new double[]{state.cartVelocity, linearAcc(state, action), state.poleAngVelocity, angularAcc(state, action)};
  }

  @Override
  public double[] computeNewInput(CartPoleState state) {
    return new double[]{state.x, state.cartVelocity, state.poleAngle, state.poleAngVelocity};
  }

  @Override
  public double computeReward(CartPoleState state) {
    return 1;
  }

  @Override
  public CartPoleState executeAction(CartPoleState currentState, Double action) {
    double x = currentState.x;
    double dx = currentState.cartVelocity;
    double theta = currentState.poleAngle;
    double dTheta = currentState.poleAngVelocity;
    double[][] rkFactors = new double[4][4];
    System.arraycopy(rungeKuttaF(currentState, action), 0, rkFactors[0], 0, 4);
    for (int i = 1; i < 3; ++i) {
      System.arraycopy(
          rungeKuttaF(
              new CartPoleState(
                  x + rkFactors[i - 1][0] * timetick / 2,
                  dx + rkFactors[i - 1][1] * timetick / 2,
                  theta + rkFactors[i - 1][2] * timetick / 2,
                  dTheta + rkFactors[i - 1][3] * timetick / 2
              ),
              action
          ),
          0,
          rkFactors[i],
          0,
          4
      );
    }
    System.arraycopy(
        rungeKuttaF(
            new CartPoleState(
                x + rkFactors[2][0] * timetick,
                dx + rkFactors[2][1] * timetick,
                theta + rkFactors[2][2] * timetick,
                dTheta + rkFactors[2][3] * timetick
            ),
            action
        ),
        0,
        rkFactors[3],
        0,
        4
    );
    double[] newState = new double[]{x, dx, theta, dTheta};
    for (int i = 0; i < 4; ++i) {
      newState[i] += (rkFactors[0][i] + 2 * rkFactors[1][i] + 2 * rkFactors[2][i] + rkFactors[3][i]) * timetick / 6;
    }
    return new CartPoleState(newState[0], fRange.clip(newState[1]), newState[2], newState[3]);
  }

  @Override
  public CartPoleState initialize() {
    return new CartPoleState(0, rng.nextGaussian() * .3, rng.nextGaussian() * Math.PI / 18, 0);
  }

  @Override
  public boolean stopCondition(CartPoleState state) {
    return Math.abs(state.poleAngle) >= Math.PI / 2 || Math.abs(state.x) >= maxX;
  }
}
