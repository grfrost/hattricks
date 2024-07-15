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

import hat.ComputeContext;
import hat.KernelContext;
import hat.buffer.F32Array;

import java.awt.Point;
import java.lang.runtime.CodeReflection;
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
    @CodeReflection
    static int red(int rgb) {
        return (rgb >> 16) & 0xff;
    }

    @CodeReflection
    static int green(int rgb) {
        return (rgb >> 8) & 0xff;
    }

    @CodeReflection
    static int blue(int rgb) {
        return rgb & 0xff;
    }

    @CodeReflection
    static int rgb(int r, int g, int b) {
        return ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
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

    @CodeReflection
    public static float getSum(ImageData imageData, Box selectionBox, XYList selectionXYList, RGBList selectionRgbList, int x, int y) {
        int offset = (y - selectionBox.y1()) * imageData.width + (x - selectionBox.x1());
        float sum = 0;
        for (int i = 0; i < selectionXYList.length(); i++) {
            var xy = selectionXYList.xy(i);
            var rgb = selectionRgbList.rgb(i);
            int rgbFromImage = imageData.array(offset + xy.y() * imageData.width + xy.x());
            int dr = red(rgbFromImage) - rgb.r();
            int dg = green(rgbFromImage) - rgb.g();
            int db = blue(rgbFromImage) - rgb.b();
            sum += dr * dr + dg * dg + db * db;
        }
        return sum;
    }

    public static boolean isInSelection(Box selectionBox, int x, int y) {
        int selectionBoxWidth = selectionBox.x2() - selectionBox.x1();
        int selectionBoxHeight = selectionBox.y2() - selectionBox.y1();
        return (!(x > selectionBox.x2() || x + selectionBoxWidth < selectionBox.x1() || y > selectionBox.y2() || y + selectionBoxHeight < selectionBox.y1()));
    }

    public static Point original(ImageData imageData, RGBList rgbList, Box searchBox, Box selectionBox, XYList selectionXYList) {
        float minSoFar = Float.MAX_VALUE;
        Point bestSoFar = new Point(0, 0);
        for (int y = searchBox.y1(); y < searchBox.y2(); y++) {
            for (int x = searchBox.x1(); x < searchBox.x2(); x++) {
                if (!isInSelection(selectionBox, x, y)) {// don't search inside the area we are healing
                    float sum = getSum(imageData, selectionBox, selectionXYList, rgbList, x, y);
                    if (sum < minSoFar) {
                        minSoFar = sum;
                        bestSoFar.setLocation(x - selectionBox.x1(), y - selectionBox.y1());
                    }
                }
            }
        }
        return bestSoFar;
    }


    public static Point sequential(ImageData imageData, RGBList rgbList, Box searchBox, Box selectionBox, XYList selectionXYList) {
        int searchBoxWidth = searchBox.x2() - searchBox.x1();
        int searchBoxHeight = searchBox.y2() - searchBox.y1();
        int range = searchBoxWidth * searchBoxHeight;
        float minSoFar = Float.MAX_VALUE;
        int bestId = range+1;
        for (int id = 0; id < range; id++) {
            int x = searchBox.x1() + id % searchBoxWidth;
            int y = searchBox.y1() + id / searchBoxWidth;
            if (!isInSelection(selectionBox, x, y)) {// don't search inside the area we are healing
                float sum = getSum(imageData, selectionBox, selectionXYList, rgbList, x, y);
                if (sum < minSoFar) {
                    minSoFar = sum;
                    bestId = id;

                }
            }
        }
        int x = searchBox.x1() + (bestId % searchBoxWidth);
        int y = searchBox.y1() + (bestId / searchBoxWidth);
        return new Point(x - selectionBox.x1(), y - selectionBox.y1());
    }


    public static Point parallel(ImageData imageData, RGBList rgbList, Box searchBox, Box selectionBox, XYList selectionXYList) {
        int searchBoxWidth = searchBox.x2() - searchBox.x1();
        int searchBoxHeight = searchBox.y2() - searchBox.y1();
        int range = searchBoxWidth * searchBoxHeight;
        float[] sumArray = new float[range];
        IntStream.range(0, range).parallel().forEach(id -> {
            int x = searchBox.x1() + id % searchBoxWidth;
            int y = searchBox.y1() + id / searchBoxWidth;
            if (isInSelection(selectionBox, x, y)) {// don't search inside the area we are healing
                sumArray[id] = Float.MAX_VALUE;
            } else {
                sumArray[id] = getSum(imageData, selectionBox, selectionXYList, rgbList, x, y);
            }
        });
        float minSoFar = Float.MAX_VALUE;
        int id = sumArray.length + 1;
        for (int i = 0; i < sumArray.length; i++) {
            float value = sumArray[i];
            if (value < minSoFar) {
                id = i;
                minSoFar = value;
            }
        }
        int x = searchBox.x1() + (id % searchBoxWidth);
        int y = searchBox.y1() + (id / searchBoxWidth);
        return new Point(x - selectionBox.x1(), y - selectionBox.y1());
    }

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
    @CodeReflection
    public static void bestKernel(KernelContext kc,
                                  ImageData imageData,
                                  RGBList rgbList,
                                  Box searchBox,
                                  Box selectionBox,
                                  XYList selectionXYList,
                                  F32Array sumArray) {
        int id = kc.x;
        int searchBoxWidth = searchBox.x2() - searchBox.x1();
        int x = searchBox.x1() + id % searchBoxWidth;
        int y = searchBox.y1() + id / searchBoxWidth;
        if (isInSelection(selectionBox, x, y)) {// don't search inside the area we are healing
            sumArray.array(id, Float.MAX_VALUE);
        } else {
            sumArray.array(id,  getSum(imageData, selectionBox, selectionXYList, rgbList, x, y));
        }
    }

    @CodeReflection
    public static Point bestCompute(ComputeContext cc, ImageData imageData, RGBList rgbList, Box searchBox, Path selectionPath) {
        int searchBoxWidth = searchBox.x2() - searchBox.x1();
        int searchBoxHeight = searchBox.y2() - searchBox.y1();
        int range = searchBoxWidth * searchBoxHeight;

        F32Array sumArray = F32Array.create(cc.accelerator, range);
        Box selectionBox = Box.create(cc.accelerator, selectionPath.x1(), selectionPath.y1(), selectionPath.x2(), selectionPath.y2());
        XYList selectionXYList = selectionPath.xyList;
        cc.dispatchKernel(range, kc -> bestKernel(kc, imageData, rgbList, searchBox, selectionBox, selectionXYList, sumArray));

        float minSoFar = Float.MAX_VALUE;
        int id = range + 1;
        for (int i = 0; i < range; i++) {
            float value = sumArray.array(i);
            if (value < minSoFar) {
                id = i;
                minSoFar = value;
            }
        }
        int x = searchBox.x1() + (id % searchBoxWidth);
        int y = searchBox.y1() + (id / searchBoxWidth);
        return new Point(x - selectionPath.x1(), y - selectionPath.y1());
    }

    public static Point getOffsetOfBestMatch(ImageData imageData, Path selectionPath) {
        Point offset = null;
        if (selectionPath.xyList.length() != 0) {
            /*
            Walk the list of xy coordinates in the path and extract a list of RGB values
            for those coordinates.
             */
            RGBListImpl rgbList = new RGBListImpl();
            for (int i = 0; i < selectionPath.xyList.length(); i++) {
                XYList.XY xy = selectionPath.xyList.xy(i);
                rgbList.addRGB(imageData.array(xy.y() * imageData.width + xy.x()));
            }

            /*
              Create a search box of pad * selection (w & h), but constrain the box to bounds of the image
             */
            int pad = 4;
            int padx = selectionPath.width() * pad;
            int pady = selectionPath.height() * pad;
            int x1 = Math.max(0, selectionPath.x1() - padx);
            int y1 = Math.max(0, selectionPath.y1() - pady);
            int x2 = Math.min(imageData.width, selectionPath.x2() + padx) - selectionPath.width();
            int y2 = Math.min(imageData.height, selectionPath.y2() + pady) - selectionPath.height();
            BoxImpl searchBox = new BoxImpl(x1, y1, x2, y2);
            long searchStart = System.currentTimeMillis();

            boolean useOriginal = true;
            boolean useSequential = true;
            // if (useOriginal) {
            long originalStart = System.currentTimeMillis();
            Box selectionBox = new BoxImpl(selectionPath.x1(), selectionPath.y1(), selectionPath.x2(), selectionPath.y2());
            XYList selectionXYList = selectionPath.xyList;
            offset = original(imageData, rgbList, searchBox, selectionBox, selectionXYList);
            System.out.println("original search " + (System.currentTimeMillis() - originalStart) + "ms");
            //  } else {
            //   if (useSequential) {
            long sequentialStart = System.currentTimeMillis();
            offset = sequential(imageData, rgbList, searchBox, selectionBox, selectionXYList);
            System.out.println("sequential search " + (System.currentTimeMillis() - sequentialStart) + "ms");
            //    } else {
            long parallelStart = System.currentTimeMillis();
            offset = parallel(imageData, rgbList, searchBox, selectionBox, selectionXYList);
            System.out.println("parallel search " + (System.currentTimeMillis() - parallelStart) + "ms");
            // }


            System.out.println("total search " + (System.currentTimeMillis() - searchStart) + "ms");
        }
        return offset;
    }
}
