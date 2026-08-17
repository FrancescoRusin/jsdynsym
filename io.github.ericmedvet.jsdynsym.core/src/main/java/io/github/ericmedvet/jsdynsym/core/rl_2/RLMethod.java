package io.github.ericmedvet.jsdynsym.core.rl_2;

import io.github.ericmedvet.jsdynsym.core.DynamicalSystem;
import io.github.ericmedvet.jsdynsym.core.numerical.NumericalDynamicalSystem;

public interface RLMethod<P> extends DynamicalSystem<RLMethod.InputAndPrevReward, double[], P> {
    record InputAndPrevReward(double[] input, double reward) {}

    double[] step(double t, double[] input, double reward);

    @Override
    default double[] step(double t, InputAndPrevReward inputAndReward) {
        return step(t, inputAndReward.input(), inputAndReward.reward());
    }

    static <P> RLMethod<P> from(NumericalDynamicalSystem<P> dynamicalSystem) {
        return new RLMethod<>() {
            @Override
            public double[] step(double t, double[] input, double reward) {
                return dynamicalSystem.step(t, input);
            }

            @Override
            public P getState() {
                return dynamicalSystem.getState();
            }

            @Override
            public void reset() {
                dynamicalSystem.reset();
            }

            @Override
            public String toString() {
                return dynamicalSystem.toString();
            }
        };
    }
}
