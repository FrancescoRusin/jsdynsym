/*-
 * ========================LICENSE_START=================================
 * jsdynsym-control
 * %%
 * Copyright (C) 2023 - 2025 Eric Medvet
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
package io.github.ericmedvet.jsdynsym.control.geometry;

import java.util.ArrayList;
import java.util.List;

public record Rectangle(Point topLeft, Point bottomRight) {

  public Rectangle {
    if (topLeft.x() >= bottomRight.x() || bottomRight.y() >= topLeft.y()) {
      throw new IllegalArgumentException(
          "Invalid rectangle: Ensure bottomLeft is below and to the left of topRight."
      );
    }
  }

  public Rectangle(Point center, double w, double h) {
    this(center.sum(new Point(-w / 2d, h / 2d)), center.sum(new Point(w / 2d, -h / 2d)));
  }

  public static Rectangle of(Point min, Point max) {
    return new Rectangle(new Point(min.x(), max.y()), new Point(max.x(), min.y()));
  }

  public Segment bottomEdge() {
    return new Segment(bottomLeft(), bottomRight);
  }

  public Point bottomLeft() {
    return new Point(topLeft.x(), bottomRight.y());
  }

  public Point center() {
    return new Point(
        topLeft.x() / 2d + bottomRight.x() / 2d,
        topLeft.y() / 2d + bottomRight.y() / 2d
    );
  }

  public double height() {
    return topLeft.y() - bottomRight.y();
  }

  public List<Point> horizontalEdgesIntersections(Segment segment, double precision) {
    List<Point> horizontalEdgesIntersections = new ArrayList<>();
    Point topEdgeIntersection = segment.intersection(topEdge(), precision);
    Point bottomEdgeIntersection = segment.intersection(bottomEdge(), precision);
    if (topEdgeIntersection != null) {
      horizontalEdgesIntersections.add(topEdgeIntersection);
    }
    if (bottomEdgeIntersection != null) {
      horizontalEdgesIntersections.add(bottomEdgeIntersection);
    }
    horizontalEdgesIntersections.sort((intersection1, intersection2) -> {
      double distance1 = segment.p1().distance(intersection1);
      double distance2 = segment.p1().distance(intersection2);
      return Double.compare(distance1, distance2);
    });
    return horizontalEdgesIntersections;
  }

  // the list of intersection points returned sorted in ascending order based on the distance from segment.p1()
  public List<Point> intersection(Segment segment, double precision) {
    List<Point> intersections = new ArrayList<>();
    // check intersection with each edge
    for (Segment edge : List.of(topEdge(), bottomEdge(), leftEdge(), rightEdge())) {
      Point intersection = segment.intersection(edge, precision);
      if (intersection != null) {
        intersections.add(intersection);
      }
    }
    intersections.sort((intersection1, intersection2) -> {
      double distance1 = segment.p1().distance(intersection1);
      double distance2 = segment.p1().distance(intersection2);
      return Double.compare(distance1, distance2);
    });
    return intersections;
  }

  public Segment leftEdge() {
    return new Segment(topLeft, bottomLeft());
  }

  public Point max() {
    return new Point(bottomRight.x(), topLeft.y());
  }

  public Point min() {
    return new Point(topLeft.x(), bottomRight.y());
  }

  public Segment rightEdge() {
    return new Segment(topRight(), bottomRight);
  }

  public Segment topEdge() {
    return new Segment(topLeft, topRight());
  }

  public Point topRight() {
    return new Point(bottomRight.x(), topLeft.y());
  }

  public List<Point> verticalEdgesIntersections(Segment segment, double precision) {
    List<Point> verticalEdgesIntersections = new ArrayList<>();
    Point leftEdgeIntersection = segment.intersection(leftEdge(), precision);
    Point rightEdgeIntersection = segment.intersection(rightEdge(), precision);
    if (leftEdgeIntersection != null) {
      verticalEdgesIntersections.add(leftEdgeIntersection);
    }
    if (rightEdgeIntersection != null) {
      verticalEdgesIntersections.add(rightEdgeIntersection);
    }
    verticalEdgesIntersections.sort((intersection1, intersection2) -> {
      double distance1 = segment.p1().distance(intersection1);
      double distance2 = segment.p1().distance(intersection2);
      return Double.compare(distance1, distance2);
    });
    return verticalEdgesIntersections;
  }

  public double width() {
    return bottomRight.x() - topLeft.x();
  }

  @Override
  public String toString() {
    return "r(%s->%s)".formatted(min(), max());
  }

}