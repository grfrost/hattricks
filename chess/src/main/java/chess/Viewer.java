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
package chess;


import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Viewer extends JFrame {
    public final BoardViewer boardViewer;

    public void view(ChessData.Board initBoard) {
        boardViewer.board = initBoard;
        boardViewer.repaint();
    }

    public static class BoardViewer extends JComponent {
        public ChessData.Board board;
        private final Object doorBell = new Object();
        public Point to = null;
        Map<Integer, BufferedImage> pieces = new HashMap<>();
        String[] pieceFileNames = new String[]{
                null, //empty
                "/pngs/bp.png",
                "/pngs/bn.png",
                "/pngs/bb.png",
                "/pngs/br.png",
                "/pngs/bq.png",
                "/pngs/bk.png",
                null,
                null,
                "/pngs/wp.png",
                "/pngs/wn.png",
                "/pngs/wb.png",
                "/pngs/wr.png",
                "/pngs/wq.png",
                "/pngs/wk.png"
        };

        BoardViewer() {
            super();
            for (int i = 0; i < pieceFileNames.length; i++) {
                String pieceFileName = pieceFileNames[i];
                if (pieceFileName != null) {
                    try {
                        pieces.put(i, ImageIO.read(Viewer.class.getResourceAsStream(pieceFileName)));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    to = e.getPoint();
                    synchronized (doorBell) {
                        doorBell.notify();
                    }
                }
            });
            to = new Point(getWidth() / 2, getHeight() / 2);
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(640, 640);
        }
        Long startTime=null;
        Double PLAY_TIME =5.0;
        Timer timer = new Timer(40, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                if (startTime == null) {
                    startTime = System.currentTimeMillis();
                }
                long playTime = System.currentTimeMillis() - startTime;
                double progress = playTime / PLAY_TIME;
                if (progress >= 1.0) {
                    progress = 1d;
                    ((Timer) e.getSource()).stop();
                }

              //  int index = Math.min(Math.max(0, (int) (points.size() * progress)), points.size() - 1);

              //  pos = points.get(index);
              //  if (index < points.size() - 1) {
              //      angle = angleTo(pos, points.get(index + 1));
              //  }
              //  repaint();
            }
        });




        @Override
        public void paintComponent(Graphics g1d) {
            super.paintComponent(g1d);
            Graphics2D g = (Graphics2D) g1d;
            int currentPieceWidth = getWidth() / 8;
            int currentPieceHeight = getHeight() / 8;
            for (int i = 0; i < 64; i++) {
                int x = i % 8;
                int y = i / 8;
                g.setColor((((x + y) % 2) == 0) ? Color.LIGHT_GRAY : Color.WHITE);
                g.fillRect(x * currentPieceWidth, y * currentPieceHeight, currentPieceWidth, currentPieceHeight);
                if (board != null) {
                    byte squareBits = board.squareBits(i);
                    if (!Compute.isEmpty(squareBits)) {
                        BufferedImage piece = (Compute.isWhite(squareBits))
                                ? pieces.get((squareBits & ChessConstants.PIECE_MASK) + 8)
                                : pieces.get((squareBits & ChessConstants.PIECE_MASK));
                        g.drawImage(piece, x * currentPieceWidth, y * currentPieceHeight, currentPieceWidth, currentPieceHeight,
                                null);
                    }
                }
            }
        }

        public void waitForDoorbell() {
            to = null;
            while (to == null) {
                synchronized (doorBell) {
                    try {
                        doorBell.wait();
                       // timer.start();
                    } catch (final InterruptedException ie) {
                        ie.getStackTrace();
                    }
                }
            }
        }

    }

    public Viewer() {
        super("chess");
        this.boardViewer = new BoardViewer();
        this.getContentPane().add(this.boardViewer);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }
}
