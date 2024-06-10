package io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers;

import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.util.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.stream.IntStream;

public class BaseTrajectoryDrawer extends AbstractArenaBasedTrajectoryDrawer implements Drawer<Point[][]> {
    private final BTDConfiguration configuration;

    public enum Mode {
        FIXED_ARROW, FIXED_LINE, GRADIENT
    }

    public record BTDConfiguration(
            Mode mode,
            Color segmentColor,
            float trajectoryThickness,
            double circleRadius,
            float segmentThickness,
            double marginRate
    ) implements Configuration {
        public BTDConfiguration(Mode mode) {
            this(mode,
                    Configuration.DEFAULT_SEGMENT_COLOR,
                    Configuration.DEFAULT_TRAJECTORY_THICKNESS,
                    Configuration.DEFAULT_CIRCLE_RADIUS,
                    Configuration.DEFAULT_SEGMENT_THICKNESS,
                    Configuration.DEFAULT_MARGIN_RATE);
        }
    }

    public BaseTrajectoryDrawer(Arena arena, BTDConfiguration configuration) {
        super(arena);
        this.configuration = configuration;
    }

    public BaseTrajectoryDrawer(Arena arena, Mode mode) {
        this(arena, new BTDConfiguration(mode));
    }


    @Override
    public void draw(Graphics2D g, Point[][] trajectories) {
        AffineTransform previousTransform = setTransform(g, arena, configuration);
        drawArena(g, configuration);
        g.setStroke(new BasicStroke((float) (configuration.trajectoryThickness / g.getTransform().getScaleX())));
        switch (configuration.mode) {
            case FIXED_ARROW:
                g.setColor(Color.BLUE);
                IntStream.range(0, trajectories.length).forEach(i ->
                        IntStream.range(1, trajectories[i].length).forEach(j -> drawArrow(g, trajectories[i][j - 1], trajectories[i][j]))
                );

                break;
            case FIXED_LINE:
                g.setColor(Color.BLUE);
                for (Point[] trajectory : trajectories) {
                    Path2D path = new Path2D.Double();
                    path.moveTo(trajectory[0].x(), trajectory[0].y());
                    Arrays.stream(Arrays.copyOfRange(trajectory, 1, trajectory.length)).forEach(p -> path.lineTo(p.x(), p.y()));
                    g.draw(path);
                }
                break;
            case GRADIENT:
                int[] color2 = new int[]{255, 0, 0};
                int[] color1 = new int[]{0, 255, 0};
                int[] current = new int[3];
                for (Point[] trajectory : trajectories) {
                    for (int i = 0; i < trajectory.length - 1; ++i) {
                        double tick = i / (double) (trajectory.length - 1);
                        for (int j = 0; j < 3; ++j) {
                            current[j] = (int) (color1[j] * tick + color2[j] * (1 - tick));
                        }
                        g.setColor(new Color(current[0], current[1], current[2]));
                        g.draw(new Line2D.Double(
                                trajectory[i].x(), trajectory[i].y(), trajectory[i + 1].x(), trajectory[i + 1].y()
                        ));
                    }
                }
        }
        g.setTransform(previousTransform);
    }
}
