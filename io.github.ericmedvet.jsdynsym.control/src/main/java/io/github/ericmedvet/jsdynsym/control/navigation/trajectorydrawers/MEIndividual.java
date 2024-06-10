package io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers;

import io.github.ericmedvet.jsdynsym.control.geometry.Point;

public record MEIndividual(Point point, double fitness, int rank, int bin1, int bin2) {
}
