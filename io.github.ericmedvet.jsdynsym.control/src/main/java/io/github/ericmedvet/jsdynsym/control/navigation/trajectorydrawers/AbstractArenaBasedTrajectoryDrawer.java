/*-
 * ========================LICENSE_START=================================
 * jsdynsym-control
 * %%
 * Copyright (C) 2023 - 2024 Eric Medvet
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * =========================LICENSE_END==================================
 */
package io.github.ericmedvet.jsdynsym.control.navigation.trajectorydrawers;

import io.github.ericmedvet.jsdynsym.control.geometry.Point;
import io.github.ericmedvet.jsdynsym.control.navigation.Arena;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

public abstract class AbstractArenaBasedTrajectoryDrawer {
  protected final Arena arena;

  protected AbstractArenaBasedTrajectoryDrawer(Arena arena) {
    this.arena = arena;
  }

  protected AffineTransform setTransform(Graphics2D g, Arena arena, Configuration configuration) {
    double cX = g.getClipBounds().x;
    double cY = g.getClipBounds().y;
    double cW = g.getClipBounds().width;
    double cH = g.getClipBounds().height;
    // compute transformation
    double scale = Math.min(
        cW / (1 + 2 * configuration.marginRate()) / arena.xExtent(),
        cH / (1 + 2 * configuration.marginRate()) / arena.yExtent());
    AffineTransform previousTransform = g.getTransform();
    AffineTransform transform = AffineTransform.getScaleInstance(scale, scale);
    transform.translate(
        (cX / scale + cW / scale - arena.xExtent()) / 2d, (cY / scale + cH / scale - arena.yExtent()) / 2d);
    g.setTransform(transform);
    return previousTransform;
  }

  protected Color getColor(Color colorZero, Color colorMid, Color colorOne, double t) {
    double doubleT = 2 * t;
    if (doubleT > 1) {
      doubleT -= 1;
      return new Color(
          (int) (colorOne.getRed() * doubleT + colorMid.getRed() * (1 - doubleT)),
          (int) (colorOne.getGreen() * doubleT + colorMid.getGreen() * (1 - doubleT)),
          (int) (colorOne.getBlue() * doubleT + colorMid.getBlue() * (1 - doubleT)));
    } else {
      return new Color(
          (int) (colorMid.getRed() * doubleT + colorZero.getRed() * (1 - doubleT)),
          (int) (colorMid.getGreen() * doubleT + colorZero.getGreen() * (1 - doubleT)),
          (int) (colorMid.getBlue() * doubleT + colorZero.getBlue() * (1 - doubleT)));
    }
  }

  protected void drawArrow(Graphics2D g, Point startingPoint, Point endingPoint) {
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

  protected void drawArena(Graphics2D g, Configuration configuration) {
    // draw arena
    g.setStroke(new BasicStroke(
        (float) (configuration.segmentThickness() / g.getTransform().getScaleX())));
    g.setColor(configuration.segmentColor());
    arena.segments().forEach(s -> g.draw(new Line2D.Double(s.p1().x(), s.p1().y(), s.p2().x(), s.p2().y())));
  }
}
