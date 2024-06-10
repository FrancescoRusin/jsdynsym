package io.github.ericmedvet.jsdynsym.control.navigation;

import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.util.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.stream.IntStream;

public class TrajectoryDrawer implements Drawer<Point[][]> {
    private final Configuration configuration;
    private final Arena arena;

    public enum Mode {
        GRADIENT, FIXED_ARROW, FIXED_LINE
    }

    public record Configuration(
            Mode mode,
            Color segmentColor,
            float trajectoryThickness,
            double finalCircleRadius,
            float segmentThickness,
            double trajectoryAlpha,
            double marginRate
    ) {}

    public TrajectoryDrawer(Arena arena, Configuration configuration) {
        this.arena = arena;
        this.configuration = configuration;
    }

    public TrajectoryDrawer(Arena arena, Mode mode) {
        this(arena, new Configuration(mode, Color.DARK_GRAY, 1, .01, 3, 1, .02));
    }


    @Override
    public void draw(Graphics2D g, Point[][] trajectories) {
        AffineTransform previousTransform = setTransform(g, arena);
        // draw arena
        g.setStroke(new BasicStroke(
                (float) (configuration.segmentThickness / g.getTransform().getScaleX())));
        g.setColor(configuration.segmentColor);
        arena.segments().forEach(s -> g.draw(new Line2D.Double(s.p1().x(), s.p1().y(), s.p2().x(), s.p2().y())));
        g.setStroke(new BasicStroke((float) (configuration.trajectoryThickness / g.getTransform().getScaleX())));
        switch (configuration.mode) {
            case FIXED_ARROW:
                g.setColor(GraphicsUtils.alphaed(Color.BLUE, configuration.trajectoryAlpha));
                IntStream.range(0, trajectories.length).forEach(i ->
                        IntStream.range(1, trajectories[i].length).forEach(j -> drawArrow(g, trajectories[i][j - 1], trajectories[i][j]))
                );

                break;
            case FIXED_LINE:
                g.setColor(GraphicsUtils.alphaed(Color.BLUE, configuration.trajectoryAlpha));
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
                        g.setColor(GraphicsUtils.alphaed(new Color(current[0], current[1], current[2]), configuration.trajectoryAlpha));
                        g.draw(new Line2D.Double(
                                trajectory[i].x(), trajectory[i].y(), trajectory[i + 1].x(), trajectory[i + 1].y()
                        ));
                    }
                }
        }
        g.setTransform(previousTransform);
    }

    private AffineTransform setTransform(Graphics2D g, Arena arena) {
        double cX = g.getClipBounds().x;
        double cY = g.getClipBounds().y;
        double cW = g.getClipBounds().width;
        double cH = g.getClipBounds().height;
        // compute transformation
        double scale = Math.min(
                cW / (1 + 2 * configuration.marginRate) / arena.xExtent(),
                cH / (1 + 2 * configuration.marginRate) / arena.yExtent());
        AffineTransform previousTransform = g.getTransform();
        AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
        transform.translate(
                (cX / scale + cW / scale - arena.xExtent()) / 2d, (cY / scale + cH / scale - arena.yExtent()) / 2d);
        g.setTransform(transform);
        return previousTransform;
    }

    private void drawArrow(Graphics2D g, Point startingPoint, Point endingPoint) {
        if (startingPoint.equals(endingPoint)) {
            return;
        }
        Point extension = endingPoint.diff(startingPoint);
        double headLength = Math.min(.02, endingPoint.diff(startingPoint).magnitude());
        Point headBase = endingPoint.sum(extension.scale(-headLength / extension.magnitude()));
        g.draw(new Line2D.Double(startingPoint.x(), startingPoint.y(), endingPoint.x(), endingPoint.y()));
        Point headHeight = endingPoint.diff(headBase).scale(.2);
        Point vertex1 = headBase.sum(new Point(headHeight.y(), -headHeight.x()));
        Point vertex2 = headBase.sum(new Point(-headHeight.y(), headHeight.x()));
        g.draw(new Line2D.Double(vertex1.x(), vertex1.y(), endingPoint.x(), endingPoint.y()));
        g.draw(new Line2D.Double(vertex2.x(), vertex2.y(), endingPoint.x(), endingPoint.y()));
    }
}
