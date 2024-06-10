package io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers;

import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.util.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

public class RankBasedTrajectoryDrawer extends AbstractArenaBasedTrajectoryDrawer implements Drawer<MEIndividual[][]> {
    private final RBTConfiguration configuration;

    public record RBTConfiguration(
            int populationSize,
            Color segmentColor,
            float trajectoryThickness,
            double circleRadius,
            float segmentThickness,
            double pointThickness,
            double pointInternAlpha,
            double marginRate
    ) implements Configuration {
        public RBTConfiguration(int populationSize) {
            this(populationSize,
                    Configuration.DEFAULT_SEGMENT_COLOR,
                    Configuration.DEFAULT_TRAJECTORY_THICKNESS,
                    Configuration.DEFAULT_CIRCLE_RADIUS,
                    Configuration.DEFAULT_SEGMENT_THICKNESS,
                    Configuration.DEFAULT_TRAJECTORY_THICKNESS * 1.3,
                    .3,
                    Configuration.DEFAULT_MARGIN_RATE
            );
        }
    }

    public RankBasedTrajectoryDrawer(Arena arena, RBTConfiguration configuration) {
        super(arena);
        this.configuration = configuration;
    }

    public RankBasedTrajectoryDrawer(Arena arena, int populationSize) {
        this(arena, new RBTConfiguration(populationSize));
    }

    @Override
    public void draw(Graphics2D g, MEIndividual[][] individuals) {
        AffineTransform previousTransform = setTransform(g, arena, configuration);
        drawArena(g, configuration);
        final int[] color1 = new int[]{255, 0, 0};
        final int[] color2 = new int[]{0, 255, 0};
        g.setStroke(new BasicStroke((float) (configuration.trajectoryThickness / g.getTransform().getScaleX())));
        for (MEIndividual[] run : individuals) {
            Point basePoint = run[0].point();
            for (int i = 0; i < run.length; ++i) {
                do {
                    ++i;
                } while (i < run.length && basePoint.equals(run[i].point()));
                double relativePosition = run[i - 1].rank() / (double) configuration.populationSize;
                Color color = new Color(
                        (int) (color1[0] * relativePosition + color2[0] * (1 - relativePosition)),
                        (int) (color1[1] * relativePosition + color2[1] * (1 - relativePosition)),
                        (int) (color1[2] * relativePosition + color2[2] * (1 - relativePosition))
                );
                Ellipse2D circle = new Ellipse2D.Double(
                        basePoint.x() - configuration.circleRadius,
                        basePoint.y() - configuration.circleRadius,
                        2 * configuration.circleRadius,
                        2 * configuration.circleRadius
                );
                g.setColor(GraphicsUtils.alphaed(color, configuration.pointInternAlpha));
                g.fill(circle);
                g.setColor(color);
                g.draw(circle);
                if (i < run.length) {
                    g.setColor(Color.BLUE);
                    drawArrow(g, basePoint, run[i].point());
                    basePoint = run[i].point();
                }
            }
        }
        g.setTransform(previousTransform);
    }
}