package io.github.ericmedvet.jsdynsym.control.navigation;

import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jviz.core.drawer.Drawer;
import io.github.ericmedvet.jviz.core.util.GraphicsUtils;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.util.stream.IntStream;

public class TrajectoryDrawer implements Drawer<Point[][]> {
    private final Configuration configuration;
    private final Arena arena;

    public record Configuration(
            Color trajectoryColor,
       Color segmentColor,
       float trajectoryThickness,
       double finalCircleRadius,
       float segmentThickness,
       double trajectoryAlpha,
       double marginRate
    ) {
        public static final Configuration DEFAULT = new Configuration(Color.BLUE, Color.DARK_GRAY,1, .01, 3, 1, .02);
    }

    public TrajectoryDrawer(Arena arena, Configuration configuration) {
        this.arena = arena;
        this.configuration = configuration;
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
        g.setColor(GraphicsUtils.alphaed(configuration.trajectoryColor, configuration.trajectoryAlpha));
        IntStream.range(0, trajectories.length).forEach(i ->
                IntStream.range(1, trajectories[i].length).forEach(j -> drawArrow(g, trajectories[i][j - 1], trajectories[i][j]))
        );
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
