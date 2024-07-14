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
import java.awt.image.DataBufferInt;
import java.lang.foreign.MemoryLayout;
import java.lang.foreign.MemorySegment;

public class Viewer extends JFrame {
    public final Life.LifeData lifeData;
    public final MemorySegment memorySegment;
    public final MemoryLayout memoryLayout;

    private final int[] rasterData;

    private final BufferedImage image;

    private final JScrollPane pane;

    private final JComponent viewer;

    private final Object doorBell = new Object();
    private double scale;

    private Point to = null;

    Viewer(String title, Life.LifeData lifeData) {
        super(title);
        this.image = new BufferedImage(lifeData.width(), lifeData.height()*2, BufferedImage.TYPE_INT_RGB);
        this.lifeData = lifeData;
        this.memorySegment = Buffer.getMemorySegment(lifeData);
        this.memoryLayout = Buffer.getLayout(lifeData);
        this.scale = .1;
        this.rasterData = ((DataBufferInt) image.getRaster().getDataBuffer()).getData();

        this.viewer = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(lifeData.width(), lifeData.height());
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

                lifeData.copyTo(rasterData);
                if (lifeData.from() == 0) {
                    g.drawImage(image, 0, 0, lifeData.width(), lifeData.height(), 0, 0, lifeData.width(), lifeData.height(), this);
                } else {
                    g.drawImage(image, 0, 0, lifeData.width(), lifeData.height(), 0, 0, lifeData.width(), lifeData.height(), this);
                   // g.drawImage(image, 0, 0, s08Array.width(), s08Array.height(), 0, s08Array.height(), 0, 2 * s08Array.height(), this);
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

        pane.setPreferredSize(new Dimension((int) (lifeData.width() * scale), (int) (lifeData.height() * scale)));
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
