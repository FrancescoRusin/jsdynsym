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
package io.github.ericmedvet.jsdynsym.core.rl_2;

import io.github.ericmedvet.jnb.datastructure.DoubleRange;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ActorCriticMethod implements RLMethod<double[], double[]> {

  public record MethodState(double[] actorWeights, double[] criticWeights) {}

  public enum Model { LINEAR, NEURAL, TILES }

  private final RLPolicy<double[], double[]> actor;
  private final RLCritic<double[]> critic;
  private final Random rng;
  private final double[] lastObservation;
  private final double[] lastAction;
  private final double actorLearningRate;
  private final double criticLearningRate;
  private final double discountFactor;
  private double currentWeightDecay;

  private static final double DEFAULT_ACTOR_LR = 1e-4;
  private static final double DEFAULT_CRITIC_LR = 1e-3;
  private static final double DEFAULT_DISCOUNT_FACTOR = .99;

  public ActorCriticMethod(
      int nOfInputs,
      int nOfOutputs,
      Model actorModel,
      Model criticModel,
      double actorLearningRate,
      double criticLearningRate,
      double discountFactor,
      int randomSeed
  ) {
    this.rng = randomSeed >= 0 ? new Random(randomSeed) : new Random();
    this.lastObservation = new double[nOfInputs];
    Arrays.fill(this.lastObservation, 0);
    this.lastAction = new double[nOfOutputs];
    Arrays.fill(this.lastAction, 0);
    this.actorLearningRate = actorLearningRate;
    this.criticLearningRate = criticLearningRate;
    this.discountFactor = discountFactor;
    this.actor = switch (actorModel) {
      case LINEAR -> new LinearPolicy(nOfInputs, nOfOutputs);
      case NEURAL -> null;
      case TILES -> null;
    };
    this.critic = switch (criticModel) {
      case LINEAR -> new LinearCritic(nOfInputs);
      case NEURAL -> null;
      case TILES -> null;
    };
    reset();
  }

  public ActorCriticMethod(
      int nOfInputs,
      int nOfOutputs,
      Model actorModel,
      Model criticModel,
      int randomSeed
  ) {
    this(
        nOfInputs,
        nOfOutputs,
        actorModel,
        criticModel,
        DEFAULT_ACTOR_LR,
        DEFAULT_CRITIC_LR,
        DEFAULT_DISCOUNT_FACTOR,
            randomSeed
    );
  }

  public ActorCriticMethod(
          int nOfInputs,
          int nOfOutputs,
          Model actorModel,
          Model criticModel
  ) {
    this(
            nOfInputs,
            nOfOutputs,
            actorModel,
            criticModel,
            DEFAULT_ACTOR_LR,
            DEFAULT_CRITIC_LR,
            DEFAULT_DISCOUNT_FACTOR,
            -1
    );
  }

  @Override
  public double[] step(double t, double[] input, double reward) {
    final double delta = reward + discountFactor * critic.apply(input) - critic.apply(lastObservation);
    final double[] criticGradient = critic.gradient(lastObservation);
    final double[] criticCurrParams = critic.getParams();
    critic.setParams(
        IntStream.range(0, criticGradient.length)
            .mapToDouble(i -> criticCurrParams[i] + criticLearningRate * delta * criticGradient[i])
            .toArray()
    );
    final double[] actorLogGradient = actor.logGradient(lastObservation, lastAction);
    final double[] actorCurrParams = actor.getParams();
    actor.setParams(
        IntStream.range(0, actorLogGradient.length)
            .mapToDouble(
                i -> actorCurrParams[i] + actorLearningRate * delta * currentWeightDecay * actorLogGradient[i]
            )
            .toArray()
    );
    System.arraycopy(input, 0, lastObservation, 0, lastObservation.length);
    final double[] newAction = actor.pickAction(input);
    System.arraycopy(newAction, 0, lastAction, 0, lastAction.length);
    currentWeightDecay *= discountFactor;
    return newAction;
  }

  @Override
  public double[] getCurrentPolicyParams() {
    return actor.getParams();
  }

  public double[] getCurrentCriticParams() {return critic.getParams();}

  @Override
  public void reset() {
    this.actor.randomize(rng, DoubleRange.SYMMETRIC_UNIT);
    this.critic.randomize(rng, DoubleRange.SYMMETRIC_UNIT);
    this.currentWeightDecay = 1;
  }

  @Override
  public void episodeReset() {
    this.currentWeightDecay = 1;
  }
}
