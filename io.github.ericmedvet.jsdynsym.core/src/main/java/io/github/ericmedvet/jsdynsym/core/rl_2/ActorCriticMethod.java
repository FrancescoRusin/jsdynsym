package io.github.ericmedvet.jsdynsym.core.rl_2;

import java.util.Arrays;
import java.util.Random;
import java.util.function.Supplier;
import java.util.stream.IntStream;

public class ActorCriticMethod implements RLMethod<ActorCriticMethod.MethodState> {

    public record MethodState(double[] actorWeights, double[] criticWeights) {}

    public enum Model {LINEAR, NEURAL, TILES}

    private final Supplier<MethodState> initialWeightsSupplier;
    private final RLPolicy<double[]> actor;
    private final RLCritic<double[]> critic;
    private final double[] lastObservation;
    private final double[] lastAction;
    private final double actorLearningRate;
    private final double criticLearningRate;
    private final double discountFactor;
    private double currentWeightDecay;

    private static final double DEFAULT_ACTOR_LR = 1e-4;
    private static final double DEFAULT_CRITIC_LR = 1e-3;
    private static final double DEFAULT_DISCOUNT_FACTOR = .99;

    public ActorCriticMethod(Supplier<MethodState> initialWeightsSupplier, int nOfInputs, int nOfOutputs, Model actorModel, Model criticModel,
                                double actorLearningRate, double criticLearningRate, double discountFactor) {
        this.initialWeightsSupplier = initialWeightsSupplier;
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

    public ActorCriticMethod(Supplier<MethodState> initialWeightsSupplier, int nOfInputs, int nOfOutputs, Model actorModel, Model criticModel) {
        this(initialWeightsSupplier, nOfInputs, nOfOutputs, actorModel, criticModel, DEFAULT_ACTOR_LR, DEFAULT_CRITIC_LR, DEFAULT_DISCOUNT_FACTOR);
    }

    public ActorCriticMethod(int nOfActorWeights, int nOfCriticWeights, Random random, int nOfInputs, int nOfOutputs, Model actorModel, Model criticModel) {
        this(() -> new MethodState(
                IntStream.range(0, nOfActorWeights).boxed().mapToDouble(d -> random.nextDouble()).toArray(),
                IntStream.range(0, nOfCriticWeights).boxed().mapToDouble(d -> random.nextDouble()).toArray()
                ), nOfInputs, nOfOutputs, actorModel, criticModel);
    }

    @Override
    public double[] step(double t, double[] input, double reward) {
        final double delta = reward + discountFactor * critic.apply(input) - critic.apply(lastObservation);
        final double[] criticGradient = critic.gradient(lastObservation);
        final double[] criticCurrParams = critic.getParams();
        critic.setParams(IntStream.range(0, criticGradient.length).mapToDouble(i -> criticCurrParams[i] + criticLearningRate * delta * criticGradient[i]).toArray());
        final double[] actorLogGradient = actor.logGradient(lastObservation, lastAction);
        final double[] actorCurrParams = actor.getParams();
        actor.setParams(IntStream.range(0, actorLogGradient.length).mapToDouble(
                i -> actorCurrParams[i] + actorLearningRate * delta * currentWeightDecay * actorLogGradient[i]
        ).toArray());
        System.arraycopy(input, 0, lastObservation, 0, lastObservation.length);
        final double[] newAction = actor.pickAction(input);
        System.arraycopy(newAction, 0, lastAction, 0, lastAction.length);
        currentWeightDecay *= discountFactor;
        return newAction;
    }

    @Override
    public MethodState getState() {
        return new MethodState(actor.getParams(), critic.getParams());
    }

    @Override
    public void reset() {
        MethodState weights = initialWeightsSupplier.get();
        this.actor.setParams(weights.actorWeights);
        this.critic.setParams(weights.criticWeights);
        this.currentWeightDecay = 1;
    }
}
