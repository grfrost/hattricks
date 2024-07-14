package life;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Viewer extends JFrame {
    public final Life.LifeData lifeData;
    private final BufferedImage image;
    private final JScrollPane scrollPane;
    final JComponent viewer;

    private final Object doorBell = new Object();
    private double scalex;
    private double scaley;
    private Point to = null;

    Viewer(String title, Life.LifeData lifeData) {
        super(title);
        this.image = new BufferedImage(lifeData.width(), lifeData.height(), BufferedImage.TYPE_INT_RGB);
        this.lifeData = lifeData;
        this.scalex = .1;
        this.scaley = .1;
        this.viewer = new JComponent() {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension((int)(image.getWidth()*scalex), (int)(image.getHeight()*scalex));
            }
            static private double getScale(int panelWidth, int panelHeight, int imageWidth, int imageHeight) {
                double scale = 1;
                double xScale;
                double yScale;
                if (imageWidth > panelWidth || imageHeight > panelHeight) {
                    xScale = (double)imageWidth  / panelWidth;
                    yScale = (double)imageHeight / panelHeight;
                    scale = Math.min(xScale, yScale);
                }
                else if (imageWidth < panelWidth && imageHeight < panelHeight) {
                    xScale = (double)panelWidth / imageWidth;
                    yScale = (double)panelHeight / imageHeight;
                    scale = Math.min(xScale, yScale);
                }
                return scale;
            }
            @Override
            public void paintComponent(Graphics g1d) {
                super.paintComponent(g1d);
                Graphics2D g2d = (Graphics2D) g1d;
                g2d.scale(scalex, scaley);
                lifeData.copyTo(
                        ((DataBufferInt) image.getRaster().getDataBuffer()).getData(),
                        ((long) lifeData.generation() % 2) *lifeData.width()*lifeData.height()*4);
                int dx = 0;
                if (image.getWidth()<this.getWidth()){
                    dx =(this.getWidth()-image.getWidth())/2;
                }
                g2d.setBackground(Color.BLACK);
                g2d.clearRect(0,0,this.getWidth(),this.getHeight());
                g2d.drawImage(image, dx, 0, lifeData.width(), lifeData.height(), 0, 0, lifeData.width(), lifeData.height(), this);
                g2d.dispose();
            }
        };
        this.scrollPane = new JScrollPane(this.viewer) {
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
                    this.scalex *= 1.1;
                    this.scaley *= 1.1;

                } else {
                    // message = "Mouse wheel moved DOWN "
                    //      + notches + " notch(es)\n";
                    this.scalex *= 1 / 1.1;
                    this.scaley *= 1 / 1.1;
                }
            Viewer.this.viewer.setSize(new Dimension((int) (image.getWidth()*scalex), (int) (image.getHeight()*scaley)));
            scrollPane.setViewportView(Viewer.this.viewer);
            //scrollPane.revalidate();
           // scrollPane.repaint();
          //  Viewer.this.viewer.setPreferredSize(new Dimension((int) (image.getWidth()*scalex), (int) (image.getHeight()*scaley)));
           // Viewer.this.viewer.setPreferredSize(new Dimension((int) (image.getWidth()*scalex), (int) (image.getHeight()*scaley)));
        });

       // pane.setPreferredSize(new Dimension((int) (lifeData.width() * scale), (int) (lifeData.height() * scale)));
        this.getContentPane().add(this.scrollPane);
      // this.addComponentListener(new ComponentAdapter() {
        //    public void componentResized(ComponentEvent componentEvent) {
          //     Viewer.this.viewer.setPreferredSize(new Dimension(componentEvent.getComponent().getWidth(), componentEvent.getComponent().getHeight()));
          //  }
        //});
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
}
