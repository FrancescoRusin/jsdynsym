package io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers;

import io.github.ericmedvet.jnb.datastructure.Pair;
import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.util.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

public class RankBasedTrajectoryDrawer extends AbstractArenaBasedTrajectoryDrawer implements Drawer<Pair<MEIndividual, Integer>[][]> {
    private final RBTConfiguration configuration;

    public record RBTConfiguration(
            Color segmentColor,
            Color arrowColor,
            float trajectoryThickness,
            double circleRadius,
            float segmentThickness,
            double pointThickness,
            double pointInternAlpha,
            double marginRate
    ) implements Configuration {
        public RBTConfiguration() {
            this(
                    Configuration.DEFAULT_SEGMENT_COLOR,
                    Configuration.DEFAULT_ARROW_COLOR,
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

    public RankBasedTrajectoryDrawer(Arena arena) {
        this(arena, new RBTConfiguration());
    }

    @Override
    public void draw(Graphics2D g, Pair<MEIndividual, Integer>[][] individualsAndSizes) {
        AffineTransform previousTransform = setTransform(g, arena, configuration);
        drawArena(g, configuration);
        final int[] firstColor = new int[]{255, 0, 0};
        final int[] secondColor = new int[]{255, 255, 0};
        final int[] thirdColor = new int[]{0, 255, 0};
        g.setStroke(new BasicStroke((float) (configuration.trajectoryThickness / g.getTransform().getScaleX())));
        for (Pair<MEIndividual, Integer>[] run : individualsAndSizes) {
            Point basePoint = run[0].first().point();
            for (int i = 0; i < run.length; ++i) {
                do {
                    ++i;
                } while (i < run.length && basePoint.equals(run[i].first().point()));
                double relativePosition = 2 * run[i - 1].first().rank() / (double) run[i - 1].second();
                Color color = getColor(Color.GREEN, Color.YELLOW, Color.RED, relativePosition);
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
                    g.setColor(configuration.arrowColor);
                    drawArrow(g, basePoint, run[i].first().point());
                    basePoint = run[i].first().point();
                }
            }
        }
        g.setTransform(previousTransform);
    }
}