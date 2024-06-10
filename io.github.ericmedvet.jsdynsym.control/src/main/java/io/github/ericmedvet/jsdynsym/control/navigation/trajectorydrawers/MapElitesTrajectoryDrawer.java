package io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers;

import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import io.github.ericmedvet.jviz.core.drawer.Drawer;

import java.awt.*;
import java.awt.geom.AffineTransform;

public class MapElitesTrajectoryDrawer extends AbstractArenaBasedTrajectoryDrawer implements Drawer<Point[][]> {
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
    public void draw(Graphics2D g, Point[][] points) {
        AffineTransform previousTransform = setTransform(g, arena, configuration);
        drawArena(g, configuration);
        //TODO
        g.setTransform(previousTransform);
    }
}