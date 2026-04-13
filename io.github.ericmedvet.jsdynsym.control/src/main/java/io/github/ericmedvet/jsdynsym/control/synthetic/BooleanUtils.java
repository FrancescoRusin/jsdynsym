/*-
 * ========================LICENSE_START=================================
 * jsdynsym-control
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
package io.github.ericmedvet.jsdynsym.control.synthetic;

public class BooleanUtils {

  private BooleanUtils() {
  }

  public static double[] bitStringToDoubleString(boolean[] bitString) {
    double[] doubleString = new double[bitString.length];
    for (int i = 0; i < bitString.length; i = i + 1) {
      doubleString[i] = bitString[i] ? 1 : -1;
    }
    return doubleString;
  }

  public static String bitStringToString(boolean[] bitString) {
    StringBuilder sb = new StringBuilder();
    for (boolean bit : bitString) {
      sb.append(bit ? "1" : "0");
    }
    return sb.toString();
  }

  public static boolean[] doubleStringToBitString(double[] doubleString) {
    boolean[] bitString = new boolean[doubleString.length];
    for (int i = 0; i < doubleString.length; i = i + 1) {
      bitString[i] = doubleString[i] > 0;
    }
    return bitString;
  }

  public static boolean[] stringToBitString(String string) {
    boolean[] bitString = new boolean[string.length()];
    for (int i = 0; i < string.length(); i = i + 1) {
      bitString[i] = switch (string.charAt(i)) {
        case '0' -> false;
        case '1' -> true;
        default -> throw new IllegalArgumentException(
            "Illegal char '%s' in bit string at %d".formatted(string.charAt(i), i)
        );
      };
    }
    return bitString;
  }

  public static double computeScore(double output, double gtOutput, ScoreType scoreType) {
    return switch (scoreType) {
      case BOOLEAN -> (output * gtOutput > 0) ? 1d : -1d;
      case UNLIMITED -> gtOutput * output;
      case LIMITED -> (gtOutput > 0) ? Math.min(1, output) : Math.min(1, -output);
    };
  }

  public static double computeScore(double[] output, double[] gtOutput, ScoreType scoreType) {
    double sum = 0;
    for (int i = 0; i < gtOutput.length; i = i + 1) {
      sum = sum + computeScore(output[i], gtOutput[i], scoreType);
    }
    return sum / gtOutput.length;
  }

  public enum ScoreType {
    BOOLEAN, LIMITED, UNLIMITED
  }

}
