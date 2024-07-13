package life;

import hat.buffer.Buffer;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public class Viewer extends JFrame {
    public final Life.S08Array s08Array;
    public final MemorySegment memorySegment;
    public final MemoryLayout memoryLayout;
    public final int width;
    public final int height;

    private final byte[] rasterData;

    private final BufferedImage image;

    private final JScrollPane pane;

    private final JComponent viewer;

    private final Object doorBell = new Object();
    private double scale;

    private Point to = null;

    Viewer(String title, Life.S08Array s08Array, int width, int height, double scale) {
        super(title);
        this.image = new BufferedImage(width, height*2, BufferedImage.TYPE_BYTE_GRAY);
        this.s08Array = s08Array;
        this.memorySegment = Buffer.getMemorySegment(s08Array);
        this.memoryLayout = Buffer.getLayout(s08Array);
        this.width = image.getWidth();
        this.height = image.getHeight()/2;
        this.scale = scale;
        this.rasterData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();

        this.viewer = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(width, height);
            }
            @Override
            public void paintComponent(Graphics g1d) {
                super.paintComponent(g1d);
                Graphics2D g = (Graphics2D) g1d;
                g.scale(Viewer.this.scale, Viewer.this.scale);
                RenderingHints rh = new RenderingHints(
                        RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHints(rh);


                if (s08Array.from() == 0) {
                    g.drawImage(image, 0, 0, width, height, 0, 0, width, height, this);
                } else {
                    g.drawImage(image, 0, 0, width, height, 0, height, width, 2 * height, this);
                }

            }
        };
        this.pane = new JScrollPane(this.viewer) {
            public void paintComponent(Graphics g) {
                super.paintComponent(g);
            }
        };

        viewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                to = e.getPoint();
                synchronized (doorBell) {
                    doorBell.notify();
                }
            }
        });
        viewer.addMouseWheelListener( e -> {
            //  String message;
            int notches = e.getWheelRotation();
            if (notches < 0) {
                //  message = "Mouse wheel moved UP "
                //  + -notches + " notch(es)\n";
                this.scale *= 1.1;
                viewer.repaint();
            } else {
                // message = "Mouse wheel moved DOWN "
                //      + notches + " notch(es)\n";
                this.scale *= 1/1.1;
                viewer.repaint();
            }
        });

        pane.setPreferredSize(new Dimension((int) (width * scale), (int) (height * scale)));
        this.getContentPane().add(this.pane);
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent _windowEvent) {
                System.exit(0);
            }
        });
    }

    public void syncWithRGB() {
        s08Array.copyTo(rasterData);
        this.viewer.repaint();
    }

    public void waitForDoorbell() {
        while (to == null) {
            synchronized (doorBell) {
                try {
                    doorBell.wait();
                } catch (final InterruptedException ie) {
                    ie.getStackTrace();
                }
            }
        }
    }
}
