package io.github.ericmedvet.jsdynsym.core.vectorial;

import io.github.ericmedvet.jsdynsym.core.composed.AbstractComposed;

import java.util.Arrays;
import java.util.random.RandomGenerator;

public class Noised<S> extends AbstractComposed<VectorialDynamicalSystem<S>> implements VectorialDynamicalSystem<S> {

  private final double inputSigma;
  private final double outputSigma;
  private final RandomGenerator randomGenerator;

  public Noised(
      VectorialDynamicalSystem<S> inner,
      double inputSigma,
      double outputSigma,
      RandomGenerator randomGenerator
  ) {
    super(inner);
    this.inputSigma = inputSigma;
    this.outputSigma = outputSigma;
    this.randomGenerator = randomGenerator;
  }

  @Override
  public S getState() {
    return inner().getState();
  }

  @Override
  public void reset() {
    inner().getState();
  }

  @Override
  public double[] step(double t, double[] input) {
    double[] noisedInput = input;
    if (inputSigma > 0) {
      noisedInput = Arrays.stream(input).map(v -> v + randomGenerator.nextGaussian(0, inputSigma)).toArray();
    }
    double[] noisedOutput = inner().step(t, noisedInput);
    if (outputSigma > 0) {
      noisedOutput = Arrays.stream(noisedOutput).map(v -> v + randomGenerator.nextGaussian(0, outputSigma)).toArray();
    }
    return noisedOutput;
  }

  @Override
  public int nOfInputs() {
    return inner().nOfInputs();
  }

  @Override
  public int nOfOutputs() {
    return inner().nOfOutputs();
  }
}