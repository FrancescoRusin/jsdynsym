package io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers;

import java.awt.*;

public interface Configuration {
    Color segmentColor();
    Color DEFAULT_SEGMENT_COLOR = Color.DARK_GRAY;
    float trajectoryThickness();
    float DEFAULT_TRAJECTORY_THICKNESS = 1;
    double circleRadius();
    double DEFAULT_CIRCLE_RADIUS = .01;
    float segmentThickness();
    float DEFAULT_SEGMENT_THICKNESS = 3;
    double marginRate();
    double DEFAULT_MARGIN_RATE = .02;
}
