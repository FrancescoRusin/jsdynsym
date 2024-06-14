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

import java.awt.*;

public interface Configuration {
  Color segmentColor();

  Color DEFAULT_SEGMENT_COLOR = Color.DARK_GRAY;

  Color arrowColor();

  Color DEFAULT_ARROW_COLOR = Color.BLUE;

  float trajectoryThickness();

  float DEFAULT_TRAJECTORY_THICKNESS = 1;

  double circleRadius();

  double DEFAULT_CIRCLE_RADIUS = .02;

  float segmentThickness();

  float DEFAULT_SEGMENT_THICKNESS = 3;

  double marginRate();

  double DEFAULT_MARGIN_RATE = .02;
}
