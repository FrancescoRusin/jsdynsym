/*-
 * ========================LICENSE_START=================================
 * jsdynsym-core
 * %%
 * Copyright (C) 2023 - 2026 Eric Medvet
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
package io.github.ericmedvet.jsdynsym.core.rl_2.cartpole;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;

public class CartPoleVisualizer implements Consumer<CartPoleProblem.CartPoleState> {
  private final List<CartPoleProblem.CartPoleState> cache;

  public CartPoleVisualizer() {
    this.cache = new ArrayList<>();
  }

  @Override
  public void accept(CartPoleProblem.CartPoleState cartPoleState) {
    this.cache.add(cartPoleState);
  }

  private void drawRect(double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4) {
    glBegin(GL_TRIANGLES);
    glVertex3d(x1, y1, 0f);
    glVertex3d(x2, y2, 0f);
    glVertex3d(x3, y3, 0f);
    glEnd();
    glBegin(GL_TRIANGLES);
    glVertex3d(x1, y1, 0f);
    glVertex3d(x3, y3, 0f);
    glVertex3d(x4, y4, 0f);
    glEnd();
  }

  public void renderCache() {
    GLFWErrorCallback.createPrint(System.err).set();
    if (!glfwInit())
      throw new IllegalStateException("Unable to initialize GLFW");

    // Configure GLFW
    glfwDefaultWindowHints();
    glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
    glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
    final long window = glfwCreateWindow(800, 600, "Hello World!", NULL, NULL);
    if (window == NULL)
      throw new RuntimeException("Failed to create the GLFW window");
    glfwMakeContextCurrent(window);
    glfwSwapInterval(1);
    glfwShowWindow(window);
    GL.createCapabilities();

    glClearColor(1f, 1f, 1f, 1.0f);

    for (int i = 0; i < 60; ++i) {
      if (glfwWindowShouldClose(window))
        break;

      double cos = Math.cos(cache.getFirst().poleAngle());
      double sin = Math.sin(cache.getFirst().poleAngle());
      double stateX = cache.getFirst().x() * .1;
      glClear(GL_COLOR_BUFFER_BIT);
      glColor3f(0f, 0f, 0f);
      glLineWidth(10f);
      glBegin(GL_LINES);
      glVertex3d(-1d, -.5d, 0d);
      glVertex3d(1d, -.5d, 0d);
      glEnd();

      glColor3f(0f, 0f, 1f);
      drawRect(stateX - .2, -.35, stateX - .2, -.65, stateX + .2, -.65, stateX + .2, -.35);

      glColor3f(1f, 0f, 0f);
      drawRect(
          stateX - .1 * cos,
          -.5 + .1 * sin,
          stateX - .1 * cos + .8 * sin,
          -.5 + .1 * sin + .8 * cos,
          stateX + .1 * cos + .8 * sin,
          -.5 - .1 * sin + .8 * cos,
          stateX + .1 * cos,
          -.5 - .1 * sin
      );

      glfwSwapBuffers(window);
      glfwPollEvents();
    }

    for (CartPoleProblem.CartPoleState state : cache) {
      if (glfwWindowShouldClose(window))
        break;

      double cos = Math.cos(state.poleAngle());
      double sin = Math.sin(state.poleAngle());
      double stateX = state.x() * .1;
      glClear(GL_COLOR_BUFFER_BIT);
      glColor3f(0f, 0f, 0f);
      glLineWidth(10f);
      glBegin(GL_LINES);
      glVertex3d(-1d, -.5d, 0d);
      glVertex3d(1d, -.5d, 0d);
      glEnd();

      glColor3f(0f, 0f, 1f);
      drawRect(stateX - .2, -.35, stateX - .2, -.65, stateX + .2, -.65, stateX + .2, -.35);

      glColor3f(1f, 0f, 0f);
      drawRect(
          stateX - .1 * cos,
          -.5 + .1 * sin,
          stateX - .1 * cos + .8 * sin,
          -.5 + .1 * sin + .8 * cos,
          stateX + .1 * cos + .8 * sin,
          -.5 - .1 * sin + .8 * cos,
          stateX + .1 * cos,
          -.5 - .1 * sin
      );

      glfwSwapBuffers(window);
      glfwPollEvents();
    }

    glfwDestroyWindow(window);
    glfwTerminate();
    glfwSetErrorCallback(null).free();
  }
}
