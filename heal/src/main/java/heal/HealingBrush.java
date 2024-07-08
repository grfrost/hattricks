/*
 * Copyright (c) 2024, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
/*
 * Based on code from HealingBrush renderscript example
 *
 * https://github.com/yongjhih/HealingBrush/tree/master
 *
 * Copyright (C) 2015 The Android Open Source Project
 *
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
 */


package heal;

import java.awt.Point;
import java.util.Arrays;
import java.util.stream.IntStream;
/*
 From the original renderscript

 float3 __attribute__((kernel)) solve1(uchar in, uint32_t x, uint32_t y) {
  if (in > 0) {
     float3 k = getF32_3(dest1, x - 1, y);
     k += getF32_3(dest1, x + 1, y);
     k += getF32_3(dest1, x, y - 1);
     k += getF32_3(dest1, x, y + 1);
     k += getF32_3(laplace, x, y);
     k /= 4;
     return k;
  }
  return rsGetElementAt_float3(dest1, x, y);;
}


float3 __attribute__((kernel)) solve2(uchar in, uint32_t x, uint32_t y) {
  if (in > 0) {
    float3 k = getF32_3(dest2, x - 1, y);
    k += getF32_3(dest2, x + 1, y);
    k += getF32_3(dest2, x, y - 1);
    k += getF32_3(dest2, x, y + 1);
       k += getF32_3(laplace, x, y);
       k /= 4;
       return k;
  }
  return getF32_3(dest2, x, y);;
}

float3 __attribute__((kernel))extractBorder(int2 in) {
  return convert_float3(rsGetElementAt_uchar4(image, in.x, in.y).xyz);
}

float __attribute__((kernel)) bordercorrelation(uint32_t x, uint32_t y) {
  float sum = 0;
  for(int i = 0 ; i < borderLength; i++) {
    int2  coord = rsGetElementAt_int2(border_coords,i);
    float3 orig = convert_float3(rsGetElementAt_uchar4(image, coord.x + x, coord.y + y).xyz);
    float3 candidate = rsGetElementAt_float3(border, i).xyz;
    sum += distance(orig, candidate);
  }
  return sum;
}
 */
public class HealingBrush {
    static int red(int rgb){
        return  (rgb >> 16) & 0xff;
    }
    static int green(int rgb){
        return  (rgb >> 8) & 0xff;
    }
    static int blue(int rgb){
        return  rgb & 0xff;
    }

    static int rgb(int r, int g, int b){
        return  ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
    }


    public static void heal(ImageData imageData, Path selectionPath, Point healPositionOffset) {

        long start = System.currentTimeMillis();
        Mask mask = new Mask(selectionPath);
        var src = new int[mask.data.length];
        var dest = new int[mask.data.length];

        for (int i = 0; i < mask.data.length; i++) { //parallel
            int x = i % mask.width;
            int y = i / mask.width;
            src[i] = imageData.getXY(selectionPath.x1() + x + healPositionOffset.x, selectionPath.y1() + y - 1 + healPositionOffset.y);
            dest[i] = (mask.data[i] != 0)
                    ? src[i]
                    : imageData.getXY(+selectionPath.x1() + x, selectionPath.y1() + y - 1);
        }




        System.out.println("mask " + (System.currentTimeMillis() - start) + "ms");
/*
        int[] stencil = new int[]{-1, 1, -mask.width, mask.width};

        int[] laplaced= new int[dest.length];

        boolean laplacian = true;
        if (laplacian) {
            start = System.currentTimeMillis();

            for (int p = 0; p < src.length; p++) { //parallel
                int x = p % mask.width;
                int y = p / mask.width;

                    int r = 0, g = 0, b = 0;
                if (x > 0 && x < mask.width - 1 && y > 0 && y < mask.height - 1) {
                    for (int offset : stencil) {
                        var v = src[p + offset];
                        r += red(v);
                        g += green(v);
                        b += blue(v);
                    }
                }
                    laplaced[p]=rgb(r, g, b);

                }
            }

        System.out.println("laplacian " + (System.currentTimeMillis() - start) + "ms");
        boolean solve = false;
        if (solve) {

            var tmp = new int[dest.length];
            start = System.currentTimeMillis();
            for (int i = 0; i < 500; i++) {
                for (int p = 0; p < mask.width * mask.height; p++) { // parallel
                    int x = p % mask.width;
                    int y = p / mask.width;
                    if (x > 0 && x < mask.width - 1 && y > 0 && y < mask.height - 1 && mask.data[p] != 0) {
                     //   var rgb = rgbList.rgb(p);

                        var r = red(laplaced[i]);//rgb.r();
                        var g = green(laplaced[i]);//rgb.g();
                        var b = blue(laplaced[i]);//rgb.b();
                        for (int offset : stencil) {
                            var v = dest[p + offset];
                            r += red(v);
                            g += green(v);
                            b += blue(v);
                        }
                        tmp[p] = rgb((r + 2) / 4, (g + 2) / 4, (b + 2) / 4);
                    }
                }
                var swap = tmp;
                tmp = dest;
                dest = swap;
            }
            System.out.println("solve " + (System.currentTimeMillis() - start) + "ms");
        }
 */
        start = System.currentTimeMillis();
        for (int i = 0; i < mask.data.length; i++) { //parallel
            int x = i % mask.width;
            int y = i / mask.width;
            imageData.setXY(selectionPath.x1() + x, selectionPath.y1() + y - 1, dest[i]);
        }
        System.out.println("heal2 " + (System.currentTimeMillis() - start) + "ms");
    }



    public static Point getOffsetOfBestMatch(ImageData imageData, Path selectionPath) {
        Point offset = null;
        if (selectionPath.xyList.length() != 0) {
            /*
            Walk the list of xy coordinates in the path and extract a list of RGB values
            for those coordinates. 
             */
            RGBGrowableList rgbList = new RGBGrowableList();
            for (int i = 0; i < selectionPath.xyList.length(); i++) {
                XYList.XY xy = selectionPath.xyList.xy(i);
                rgbList.addRGB(imageData.data[xy.y() * imageData.width + xy.x()]);
            }
            int xmin = Math.max(0, selectionPath.x1() - selectionPath.width() * 10);
            int ymin = Math.max(0, selectionPath.y1() - selectionPath.height() * 10);
            int xmax = Math.min(imageData.width, selectionPath.x2() + selectionPath.width() * 10);
            int ymax = Math.min(imageData.height, selectionPath.y2() + selectionPath.height() * 10);

            int searchBoxMaxX = xmax - selectionPath.width();
            int searchBoxMaxY = ymax - selectionPath.height();
            XYList xyList = selectionPath.xyList;
            int pathx1 = selectionPath.x1();
            int pathy1 = selectionPath.y1();
            int pathx2 = selectionPath.x2();
            int pathy2 = selectionPath.y2();
            int selectionWidth = selectionPath.width();
            int selectionHeight = selectionPath.height();


            long searchStart = System.currentTimeMillis();


            float minSoFar = Float.MAX_VALUE;
            Point bestSoFar = new Point(0, 0);

              /*
                  float __attribute__((kernel)) bordercorrelation(uint32_t x, uint32_t y) {
                    float sum = 0;
                    for(int i = 0 ; i < borderLength; i++) {
                       int2  coord = rsGetElementAt_int2(border_coords,i);
                       float3 orig = convert_float3(rsGetElementAt_uchar4(image, coord.x + x, coord.y + y).xyz);
                       float3 candidate = rsGetElementAt_float3(border, i).xyz;
                       sum += distance(orig, candidate);
                    }
                    return sum;
                  }
               */

            boolean useOriginal = false;
            if (useOriginal) {
                for (int y = ymin; y < searchBoxMaxY; y++) {
                    for (int x = xmin; x < searchBoxMaxX; x++) {
                        boolean inSelection = (!(x > pathx2 || x + selectionWidth < pathx1 || y > pathy2 || y + selectionHeight < pathy1));
                        if (!inSelection) { // don't search inside the area we are healing
                            float sum = 0;
                            for (int i = 0; i < xyList.length(); i++) {
                                var xy = xyList.xy(i);
                                var rgb = rgbList.rgb(i);
                                int rgbFromImage = imageData.data[
                                        (y - pathy1) * imageData.width + (x - pathx1) + xy.y() * imageData.width + xy.x()
                                        ];
                                int dr = red(rgbFromImage) - rgb.r();
                                int dg = green(rgbFromImage) - rgb.g();
                                int db = blue(rgbFromImage) - rgb.b();
                                sum += dr * dr + dg * dg + db * db;
                            }

                            if (sum < minSoFar) {
                                minSoFar = sum;
                                bestSoFar.setLocation(x - pathx1, y - pathy1);
                            }
                        }
                    }
                }
            } else {
                int searchBoxWidth = (searchBoxMaxX - xmin);
                int searchBoxHeight = (searchBoxMaxY - ymin);
                int range = searchBoxWidth * searchBoxHeight;

                float[] sumArray = new float[range];
                IntStream.range(0, range).parallel().forEach(id -> {
                    int x = xmin + id % searchBoxWidth;
                    int y = ymin + id / searchBoxWidth;
                    boolean inSelection = (!(x > pathx2 || x + selectionWidth < pathx1 || y > pathy2 || y + selectionHeight < pathy1));
                    if (inSelection) {// don't search inside the area we are healing
                        sumArray[id] = Float.MAX_VALUE;
                    } else {
                        float sum = 0;
                        for (int i = 0; i < xyList.length(); i++) {
                            var xy = xyList.xy(i);
                            var rgb = rgbList.rgb(i);

                            int rgbFromImage = imageData.data[
                                    (y - pathy1) * imageData.width + (x - pathx1) + xy.y() * imageData.width + xy.x()
                                    ];
                            int dr = red(rgbFromImage) - rgb.r();
                            int dg = green(rgbFromImage) - rgb.g();
                            int db = blue(rgbFromImage) - rgb.b();
                            sum += dr * dr + dg * dg + db * db;
                        }
                        sumArray[id] = sum;
                    }
                });
                int id = sumArray.length + 1;
                for (int i = 0; i < sumArray.length; i++) {
                    float value = sumArray[i];
                    if (value < minSoFar) {
                        id = i;
                        minSoFar = value;
                    }
                }
                int x = xmin + (id % searchBoxWidth);
                int y = ymin + (id / searchBoxWidth);
                bestSoFar = new Point(x - pathx1, y - pathy1);
            }
            System.out.println("search " + (System.currentTimeMillis() - searchStart) + "ms");
            offset = bestSoFar;
        }
        return offset;
    }
}
