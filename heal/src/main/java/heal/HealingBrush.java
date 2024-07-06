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

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;
import java.util.stream.IntStream;

public class HealingBrush {
    public static int[] getMask(Path path, int width, int height) {
        Polygon polygon = new Polygon();
        for (int i = 0; i < path.xyList.length(); i++) {
            XYList.XY xy = path.xyList.xy(i);
            polygon.addPoint(xy.x() - path.x1() + 1, xy.y() - path.y1() + 1);
        }
        BufferedImage maskImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        int[] mask = ((DataBufferInt) (maskImg.getRaster().getDataBuffer())).getData();
        Arrays.fill(mask, 0);
        Graphics2D g = maskImg.createGraphics();
        g.setColor(Color.WHITE);
        g.fillPolygon(polygon);
        return mask;
    }

    public static void heal(ImageData imageData,Path selectionPath, int fromDeltaX, int fromDeltaY) {
        int reg_width = 2 + selectionPath.width();
        int reg_height = 2 + selectionPath.height();
        int[] mask = getMask(selectionPath, reg_width, reg_height);
        int[] dest = new int[mask.length];
        int[] src = new int[mask.length];
        long start = System.currentTimeMillis();
        for (int i = 0; i < mask.length; i++) { //parallel
            int x = i % reg_width;
            int y = i / reg_width;
            src[i] = imageData.get(selectionPath.x1() + x + fromDeltaX,selectionPath.y1() + y - 1 + fromDeltaY) ;
            dest[i] =(mask[i] != 0) ?src[i] :imageData.get(+ selectionPath.x1() + x,(selectionPath.y1() + y - 1));
        }
        System.out.println("heal " + (System.currentTimeMillis() - start) + "ms");

        RGBGrowableList srclap = laplacian(src, reg_width, reg_height);

       // displayLapacian(srclap, dest, mask);

        solve(dest, mask, srclap, reg_width, reg_height);


        start = System.currentTimeMillis();
        for (int i = 0; i < mask.length; i++) { //parallel
            int x = i % reg_width;
            int y = i / reg_width;
            imageData.set( selectionPath.x1() + x,selectionPath.y1() + y - 1,dest[i] );

        }
        System.out.println("heal2 " + (System.currentTimeMillis() - start) + "ms");
    }

    static void solve(int[] dest, int[] mask, RGBGrowableList lap_rgb, int width, int height) {
        int r, g, b, v;
        int[] tmp = Arrays.copyOf(dest, dest.length);
        long start = System.currentTimeMillis();
        for (int i = 0; i < 500; i++) {
            for (int p = 0; p < width * height; p++) { // parallel
                int x = p % width;
                int y = p / width;
                if (x > 0 && x < width - 1 && y > 0 && y < height - 1 && mask[p] != 0) {
                    v = dest[p - 1];
                    r = ((v >> 16) & 0xff);
                    g = ((v >> 8) & 0xff);
                    b = ((v >> 0) & 0xff);

                    v = dest[p + 1];
                    r += ((v >> 16) & 0xff);
                    g += ((v >> 8) & 0xff);
                    b += ((v >> 0) & 0xff);

                    v = dest[p - width];
                    r += ((v >> 16) & 0xff);
                    g += ((v >> 8) & 0xff);
                    b += ((v >> 0) & 0xff);

                    v = dest[p + width];
                    r += ((v >> 16) & 0xff);
                    g += ((v >> 8) & 0xff);
                    b += ((v >> 0) & 0xff);

                    r += (lap_rgb.rgb[p * RGBGrowableList.STRIDE + RGBGrowableList.Ridx]);
                    g += (lap_rgb.rgb[p * RGBGrowableList.STRIDE + RGBGrowableList.Gidx]);
                    b += (lap_rgb.rgb[p * RGBGrowableList.STRIDE + RGBGrowableList.Bidx]);

                    r = (r + 2) / 4;
                    g = (g + 2) / 4;
                    b = (b + 2) / 4;
                    tmp[p] = (((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF));
                }
            }
            int[] swap = tmp;
            tmp = dest;
            dest = swap;
        }
        System.out.println("solve " + (System.currentTimeMillis() - start) + "ms");
    }


    static void displayLapacian(RGBGrowableList lap_rgb, int[] dst, int[] mask) {
        for (int i = 0; i < lap_rgb.length(); i++) {
            var rgb = lap_rgb.rgb(i);
            if (mask[rgb.idx] != 0) {
                dst[rgb.idx] =
                        (((Math.abs(rgb.r()) & 0xFF) << 16)
                                | ((Math.abs(rgb.g()) & 0xFF) << 8)
                                | (Math.abs(rgb.b()) & 0xFF));
            }
        }
    }

    static RGBGrowableList laplacian(int[] src, int width, int height) {
        RGBGrowableList rgbList = new RGBGrowableList();
        long start = System.currentTimeMillis();
        for (int p = 0; p < width * height; p++) { //parallel
            int x = p % width;
            int y = p / width;
            if (x > 0 && x < width - 1 && y > 0 && y < height - 1) {
                int v = src[p];
                int r = ((v >> 16) & 0xff) << 2;
                int g = ((v >> 8) & 0xff) << 2;
                int b = ((v >> 0) & 0xff) << 2;

                v = src[p - 1];
                r -= ((v >> 16) & 0xff);
                g -= ((v >> 8) & 0xff);
                b -= ((v >> 0) & 0xff);

                v = src[p + 1];
                r -= ((v >> 16) & 0xff);
                g -= ((v >> 8) & 0xff);
                b -= ((v >> 0) & 0xff);

                v = src[p - width];
                r -= ((v >> 16) & 0xff);
                g -= ((v >> 8) & 0xff);
                b -= ((v >> 0) & 0xff);

                v = src[p + width];
                r -= ((v >> 16) & 0xff);
                g -= ((v >> 8) & 0xff);
                b -= ((v >> 0) & 0xff);
                rgbList.add(r, g, b);
            } else {
                rgbList.add(0, 0, 0);
            }
        }
        System.out.println("laplacian " + (System.currentTimeMillis() - start) + "ms");
        return rgbList;
    }


    public static Point getBestMatch(ImageData imageData,Path selectionPath) {
        Point offset = null;
        if (selectionPath.xyList.length() != 0) {
            int xmin = Math.max(0, selectionPath.x1() - selectionPath.width() * 3);
            int ymin = Math.max(0, selectionPath.y1() - selectionPath.height() * 3);
            int xmax = Math.min(imageData.width, selectionPath.x2()+ selectionPath.width() * 3);
            int ymax = Math.min(imageData.height, selectionPath.y2() + selectionPath.height() * 3);

            RGBGrowableList rgbList = new RGBGrowableList();
            for (int i = 0; i < selectionPath.xyList.length(); i++) {
                XYList.XY xy = selectionPath.xyList.xy(i);
                rgbList.addRGB(imageData.data[xy.y() * imageData.width + xy.x()]);
            }

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

            boolean useOriginal = false;
            float minSoFar = Float.MAX_VALUE;
            Point bestSoFar = new Point(0, 0);
            if (useOriginal) {
                for (int y = ymin; y < searchBoxMaxY; y++) {
                    for (int x = xmin; x < searchBoxMaxX; x++) {
                        boolean inSelection = (!(x > pathx2 || x + selectionWidth < pathx1 || y > pathy2 || y + selectionHeight < pathy1));
                        if (!inSelection) { // don't search inside the area we are healing
                            int sdx = x - pathx1;
                            int sdy = y - pathy1;
                            int sdxyoffset = sdy * imageData.width + sdx;
                            float sum = 0;
                            for (int i = 0; i < xyList.length(); i++) {
                                var xy = xyList.xy(i);
                                var rgb = rgbList.rgb(i);
                                int rgbFromImage = imageData.data[sdxyoffset + xy.y() * imageData.width + xy.x()];
                                int r = (rgbFromImage >> 16) & 0xff;
                                int g = (rgbFromImage >> 8) & 0xff;
                                int b = (rgbFromImage >> 0) & 0xff;
                                int dr = r - rgb.r();
                                int dg = g - rgb.g();
                                int db = b - rgb.b();
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
                        int sdx = x - pathx1;
                        int sdy = y - pathy1;
                        int sdxyoffset = sdy * imageData.width + sdx;
                        float sum = 0;
                        for (int i = 0; i < xyList.length(); i++) {
                            var xy = xyList.xy(i);
                            var rgb = rgbList.rgb(i);
                            int rgbFromImage = imageData.data[sdxyoffset + xy.y() * imageData.width + xy.x()];
                            int r = (rgbFromImage >> 16) & 0xff;
                            int g = (rgbFromImage >> 8) & 0xff;
                            int b = (rgbFromImage >> 0) & 0xff;
                            int dr = r - rgb.r();
                            int dg = g - rgb.g();
                            int db = b - rgb.b();
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
