package io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers;

import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.util.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.Comparator;

public class MapElitesTrajectoryDrawer extends AbstractArenaBasedTrajectoryDrawer implements Drawer<MEIndividual[][]> {
    private final METConfiguration configuration;

    public record METConfiguration(
            Point descriptorTick,
            Color segmentColor,
            float trajectoryThickness,
            double circleRadius,
            float segmentThickness,
            double pointInternAlpha,
            double marginRate
    ) implements Configuration {
        public METConfiguration(Point descriptorTick) {
            this(descriptorTick,
                    Configuration.DEFAULT_SEGMENT_COLOR,
                    Configuration.DEFAULT_TRAJECTORY_THICKNESS,
                    Configuration.DEFAULT_CIRCLE_RADIUS,
                    Configuration.DEFAULT_SEGMENT_THICKNESS,
                    .3,
                    Configuration.DEFAULT_MARGIN_RATE
                    );
        }
    }

    public MapElitesTrajectoryDrawer(Arena arena, METConfiguration configuration) {
        super(arena);
        this.configuration = configuration;
    }

    public MapElitesTrajectoryDrawer(Arena arena, Point descriptorTick) {
        this(arena, new METConfiguration(descriptorTick));
    }

    @Override
    public void draw(Graphics2D g, MEIndividual[][] individuals) {
        //TODO RIFARE BUONA PARTE DEL LAVORO PERCHÉ AVEVO CAPITO MALE
        AffineTransform previousTransform = setTransform(g, arena, configuration);
        drawArena(g, configuration);
        int[][] visitCounter = new int[(int) Math.ceil(arena.xExtent() / configuration.descriptorTick.x() - 0.0001)]
                [(int) Math.ceil(arena.xExtent() / configuration.descriptorTick.x() - 0.0001)];
        double[][] averageFitness = new double[visitCounter.length][visitCounter[0].length];
        double[] fitnessBounds = new double[]{Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY};
        Arrays.stream(visitCounter).forEach(a -> Arrays.fill(a, 0));
        Arrays.stream(averageFitness).forEach(a -> Arrays.fill(a, 0));
        for (MEIndividual[] run : individuals) {
            for (MEIndividual individual : run) {
                ++visitCounter[individual.bin1()][individual.bin2()];
                averageFitness[individual.bin1()][individual.bin2()] += individual.fitness();
                if (individual.fitness() < fitnessBounds[0]) {
                    fitnessBounds[0] = individual.fitness();
                }
                if (individual.fitness() > fitnessBounds[1]) {
                    fitnessBounds[1] = individual.fitness();
                }
            }
        }
        for (int i = 0; i < averageFitness.length; ++i) {
            for (int j = 0; j < averageFitness[0].length; ++j) {
                averageFitness[i][j] /= visitCounter[i][j];
            }
        }
        for (int i = 0; i < visitCounter.length; ++i) {
            for (int j = 0; j < visitCounter[0].length; ++j) {
                if (visitCounter[i][j] != 0) {
                    System.out.printf("%d %d; %d\n", i, j, visitCounter[i][j]);
                }
            }
        }
        final int[] color1 = new int[]{255, 0, 0};
        final int[] color2 = new int[]{0, 255, 0};
        final double minimumRadius = configuration.circleRadius / Arrays.stream(visitCounter).map(a -> Arrays.stream(a).max().orElse(0))
                .max(Comparator.comparingInt(i -> i)).orElse(0);
        for (int i = 0; i < visitCounter.length; ++i) {
            for (int j = 0; j < visitCounter[i].length; ++j) {
                if (visitCounter[i][j] != 0) {
                    double circleRadius = visitCounter[i][j] * minimumRadius;
                    double relativeFitness = (averageFitness[i][j] - fitnessBounds[0]) / (fitnessBounds[1] - fitnessBounds[0]);
                    Color color = new Color(
                            (int) (color1[0] * relativeFitness + color2[0] * (1 - relativeFitness)),
                            (int) (color1[1] * relativeFitness + color2[1] * (1 - relativeFitness)),
                            (int) (color1[2] * relativeFitness + color2[2] * (1 - relativeFitness))
                    );
                    Ellipse2D circle = new Ellipse2D.Double(
                            configuration.descriptorTick.x() * i - circleRadius,
                            configuration.descriptorTick.y() * j - circleRadius,
                            2 * circleRadius,
                            2 * circleRadius
                    );
                    g.setColor(GraphicsUtils.alphaed(color, configuration.pointInternAlpha));
                    g.fill(circle);
                    g.setColor(color);
                    g.draw(circle);
                }
            }
        }
        g.setTransform(previousTransform);
    }
}