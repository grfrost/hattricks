package vga;

import javax.swing.JComponent;
import javax.swing.JFrame;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

public class VGAFrame extends JFrame {
    final private int width;
    final private int height;
    final private byte[] vga;
    final private BufferedImage image;
    final private Object doorBell;
    final private JComponent viewer;
    final long startMillis;
    long frames;
    Point point;

    VGAFrame(String name, int width, int height) {
        super(name);
        this.width = width;
        this.height = height;
        image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        vga = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        Arrays.fill(vga, (byte) 1);
        startMillis = System.currentTimeMillis();

        this.doorBell = new Object();
        this.viewer = new JComponent() {
            @Override
            public void paintComponent(Graphics g) {
                var g2 = (Graphics2D) g;
                //   vga[25*width+25]= (byte) 0xff;
                g2.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), 0, 0, image.getWidth(), image.getHeight(), this);
            }
        };
        this.viewer.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        this.viewer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                ringDoorBell(e.getPoint());
            }
        });
        getContentPane().add(viewer);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent _windowEvent) {
                System.exit(0);
            }
        });
    }

    Point waitForPoint(long timeout) {
        while (point == null) {
            synchronized (doorBell) {
                try {
                    if (timeout > 0) {
                        doorBell.wait(timeout);
                    }
                    update();
                } catch (final InterruptedException ie) {
                    ie.getStackTrace();
                }
            }
        }
        Point returnPoint = point;
        point = null;
        return returnPoint;
    }

    void ringDoorBell(Point point) {
        this.point = point;
        synchronized (doorBell) {
            doorBell.notify();
        }
    }

    void update() {
        for (int i = 0; i < vga.length; i++) {
            shader.accept(vga, i % width, i / width, width, height, frames);
        }
        viewer.repaint();
        final long elapsedMillis = System.currentTimeMillis() - startMillis;
        if ((frames++ % 50) == 0) {
            System.out.println("Frames " + frames + " FPS = " + ((frames * 1000) / elapsedMillis));
        }
    }

    @FunctionalInterface
    public interface Shader {
        void accept(byte[] vga, int x, int y, int width, int height, long frame);
    }

    Shader shader;

    public void setShader(Shader shader) {
        this.shader = shader;
    }
}
