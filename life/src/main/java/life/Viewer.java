package life;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.Color;
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

public class Viewer extends JFrame {
    public final LifeSupport lifeSupport;
    public final LifeData lifeData;
    private final BufferedImage image;
    private final JScrollPane scrollPane;
    final JComponent viewer;
    final byte [] rasterData;
    private final Object doorBell = new Object();
    private double scale;

    private Point to = null;

    Viewer(String title, LifeSupport lifeSupport, LifeData lifeData) {
        super(title);
        this.lifeSupport = lifeSupport;
        this.lifeData = lifeData;
        this.image =new BufferedImage(lifeData.width(), lifeData.height(), BufferedImage.TYPE_BYTE_GRAY);
        this.rasterData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        this.scale = 1;
        this.viewer = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension((int)(image.getWidth()*scale), (int)(image.getHeight()*scale));
            }
            @Override
            public void paintComponent(Graphics g1d) {
                super.paintComponent(g1d);
                Graphics2D g2d = (Graphics2D) g1d;
                g2d.scale(scale, scale);
                lifeData.copySliceTo(rasterData, lifeSupport.to());
                g2d.setBackground(Color.BLACK);
                g2d.clearRect(0,0,this.getWidth(),this.getHeight());
                g2d.drawImage(image, 0, 0, lifeData.width(), lifeData.height(), 0, 0, lifeData.width(), lifeData.height(), this);
                g2d.dispose();
            }
        };
        this.scrollPane = new JScrollPane(this.viewer);
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
            this.scale = (e.getWheelRotation() < 0)?this.scale * 1.1:scale * 1 / 1.1;
            Viewer.this.viewer.setSize(new Dimension((int) (image.getWidth()*scale), (int) (image.getHeight()*scale)));
            scrollPane.setViewportView(Viewer.this.viewer);
        });
        this.getContentPane().add(this.scrollPane);
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

    public void update() {
        viewer.repaint();
    }
}
