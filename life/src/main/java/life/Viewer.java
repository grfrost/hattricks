package life;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

public class Viewer extends JFrame {

    final public class MainPanel extends JComponent {
        final double IN = 1.1;
        final double OUT = 1/IN;
        private final BufferedImage image;
        private final byte[] rasterData;
        private final double initialZoomFactor;
        private double zoomFactor;
        private double prevZoomFactor;
        private boolean zooming;
        private boolean released;
        private double xOffset = 0;
        private double yOffset = 0;
        private Point startPoint;
        private Life.CellGrid cellGrid;
        private Life.Control control;

        class Drag{
            public int xDiff;
            public int yDiff;
            Drag(int xDiff, int yDiff) {
                this.xDiff = xDiff;
                this.yDiff = yDiff;
            }
        }
        Drag drag = null;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension((int)(image.getWidth()*zoomFactor), (int)(image.getHeight()*zoomFactor));
        }
        public MainPanel(BufferedImage image, Life.Control control, Life.CellGrid cellGrid) {
            this.image = image;
            Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
            this.initialZoomFactor = Math.min((bounds.width-20)/(float)image.getWidth(),
                    (bounds.height-20)/(float)image.getHeight());
            this.control = control;
            this.cellGrid = cellGrid;
            this.rasterData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
            this.prevZoomFactor =initialZoomFactor;
            this.zoomFactor = initialZoomFactor;
            addMouseWheelListener(e -> {
                zooming = true;
                zoomFactor = zoomFactor * ((e.getWheelRotation() < 0)?IN:OUT);
                if (zoomFactor < initialZoomFactor ){
                    zoomFactor = initialZoomFactor;
                    prevZoomFactor = zoomFactor;
                }
                repaint();
            });
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    Point curPoint = e.getLocationOnScreen();
                    drag = new Drag(curPoint.x - startPoint.x, curPoint.y - startPoint.y);
                    repaint();
                }
            });
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    released = false;
                    startPoint = MouseInfo.getPointerInfo().getLocation();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    released = true;
                    repaint();
                }
            });
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            Graphics2D g2 = (Graphics2D) g;
            AffineTransform at = new AffineTransform();
            if (zooming) {
                double xRel = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX();
                double yRel = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();
                double zoomDiv = zoomFactor / prevZoomFactor;
                xOffset = (zoomDiv) * (xOffset) + (1 - zoomDiv) * xRel;
                yOffset = (zoomDiv) * (yOffset) + (1 - zoomDiv) * yRel;
                at.translate(xOffset, yOffset);
                prevZoomFactor = zoomFactor;
                zooming = false;
            } else if (drag!= null) {
                at.translate(xOffset +drag.xDiff, yOffset + drag.yDiff);
                if (released) {
                    xOffset += drag.xDiff;
                    yOffset += drag.yDiff;
                    drag = null;
                }
            } else{
                at.translate(xOffset, yOffset);
            }
            at.scale(zoomFactor, zoomFactor);
            g2.transform(at);
            cellGrid.copySliceTo(rasterData, control.to());
            g2.setColor(Color.BLACK);
            g2.fillRect(0-5000, 0-5000, cellGrid.width()+10000, cellGrid.height()+10000);
            g2.drawImage(image, 0,0, cellGrid.width(), cellGrid.height(), 0, 0, cellGrid.width(), cellGrid.height(), this);
        }
    }


    private final Object doorBell = new Object();
    private final MainPanel mainPanel;
    private final JTextField generation;
    private final JTextField generationsPerSecond;
    volatile private boolean started=false;

    void setGeneration(int generation, float generationsPerSecond){
        this.generation.setText(String.format("%8d",generation));
        this.generationsPerSecond.setText(String.format("%5.2f",generationsPerSecond));
        mainPanel.repaint();
    }

    Viewer(String title, Life.Control control, Life.CellGrid cellGrid) {
        super(title);
        this.mainPanel = new MainPanel(new BufferedImage(cellGrid.width(), cellGrid.height(), BufferedImage.TYPE_BYTE_GRAY),control,cellGrid);
        var menuBar = new JMenuBar();
        this.setJMenuBar(menuBar);
        ((JButton) menuBar.add(new JButton("Exit"))).addActionListener(_ -> System.exit(0));
        ((JButton) menuBar.add(new JButton("Start"))).addActionListener(_ -> {started=true;synchronized (doorBell) {doorBell.notify();}});
        menuBar.add(Box.createHorizontalStrut(400));
        menuBar.add(new JLabel("Gen"));
        (this.generation = (JTextField) menuBar.add(new JTextField("",8))).setEditable(false);
        menuBar.add(new JLabel("Gen/Sec"));
        (this.generationsPerSecond = (JTextField) menuBar.add(new JTextField("",6))).setEditable(false);
        this.setGeneration(0,0);

        this.getContentPane().add(this.mainPanel);

        this.setLocationRelativeTo(null);
        this.pack();
        this.setVisible(true);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
    }

    public void waitForStart() {
        while (!started) {
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
        mainPanel.repaint();
    }
}
