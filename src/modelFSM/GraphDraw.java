package modelFSM;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.font.LineMetrics;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.SwingUtilities;

public class GraphDraw extends JPanel implements MouseListener, MouseMotionListener, MouseWheelListener {

    private static final long serialVersionUID = 740469716794771348L;
    private final static int  PADX = 20;
    private final static int  PADY = 200;
    private final static int MAX_ZOOM = 30;
    private final static double  DELTAY = 1080*(0.25+MAX_ZOOM*0.25)+500;
    private final static int  HBAR_EXT = 20;
    private final static int  COLOR_SIZE = 15;
    
  
    
    private Point mousePressedState;
    private Point mouseCurrentState;
    
    Color[] colorArr = new Color[COLOR_SIZE];/*= {Color.black, Color.red, Color.green, Color.yellow, Color.blue, Color.magenta,  
                        Color.pink, Color.orange, Color.gray,  Color.white, Color.cyan, 
                        Color.black,Color.black,Color.black,Color.black,Color.black,Color.black,Color.black,Color.black,Color.black,
                        Color.black,Color.black,Color.black,Color.black,Color.black,Color.black,Color.black,Color.black,Color.black};*/
    
    ArrayList<Double> timeArray;
    ArrayList<Double> dataArray;
    ArrayList<Double> dataArrayGroup;
    JScrollBar hbar;
    JScrollBar vbar;
    JLabel label;
    private int pointsToPrint;
    private int zoomLevel = 1;
    private int dataSize;
    
    class MyAdjustmentListener implements AdjustmentListener {
        public void adjustmentValueChanged(AdjustmentEvent e) {
          repaint();
        }
      }
    
    
    public GraphDraw(ArrayList<Double> timeArray, ArrayList<Double> dataArray, ArrayList<Double> dataArrayGroup) {
        this.timeArray = timeArray;
        this.dataArray = dataArray;
        this.dataArrayGroup = dataArrayGroup;
        
        int colorSpace = 255;
        for (int i = 0; i < COLOR_SIZE; i++) {
            
            int step_size = (COLOR_SIZE-1)/6;
            int r_p = Math.max(Math.min(colorSpace, colorSpace/step_size*i), 0);
            int r_n = Math.max(Math.min(colorSpace, colorSpace/step_size*(i-step_size)), 0);
            int r = (r_p - r_n)*(colorSpace - 50)/colorSpace + 49;
            
            int g_p = Math.max(Math.min(colorSpace, colorSpace/step_size*(i - step_size)), 0);
            int g_n = Math.max(Math.min(colorSpace, colorSpace/step_size*(i-2*step_size)), 0);
            int g = (g_p - g_n)*(colorSpace - 50)/colorSpace + 49;
            
            int b_p = Math.max(Math.min(colorSpace, colorSpace/step_size*(i - 3*step_size)), 0);
            int b_n = Math.max(Math.min(colorSpace, colorSpace/step_size*(i-4*step_size)), 0);
            int b = (b_p - b_n)*(colorSpace - 50)/colorSpace + 49;
            
            //System.out.println(r + " " + g + " " + b);
            colorArr[i] = new Color(b, g, r);
            
        }
        
        addMouseListener(this);
        addMouseMotionListener(this);
        this.setBorder(BorderFactory.createLineBorder(Color.black));
//        this.setBounds(20, 20, 500, 500);
        //this.setSize(200,500);
        setLayout(new BorderLayout());
        pointsToPrint = timeArray.size();
        dataSize = timeArray.size();
        hbar = new JScrollBar(JScrollBar.HORIZONTAL, 0, pointsToPrint, 0, dataSize);
        vbar = new JScrollBar(JScrollBar.VERTICAL, (int)(DELTAY/2), 20, 0, (int)DELTAY);
        label = new JLabel();
        
        hbar.setUnitIncrement(1);
        hbar.setBlockIncrement(1);
        
        vbar.setUnitIncrement(1);
        vbar.setBlockIncrement(1);

        hbar.addAdjustmentListener(new MyAdjustmentListener());
        vbar.addAdjustmentListener(new MyAdjustmentListener());

        label.setText("Zoomlevel: "+Integer.toString(zoomLevel));
        
        add(hbar, BorderLayout.SOUTH);
        add(vbar, BorderLayout.EAST);
        add(label, BorderLayout.NORTH);
        setPreferredSize(new Dimension(1520, 1000));
    } 
 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D)g;
        
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        int w = getWidth();
        int h = getHeight();
        
        int deltaY = (int)-(vbar.getValue() - DELTAY/2); 
        // Draw ordinate.
        g2.draw(new Line2D.Double(PADX, PADX, PADX, h-PADX));
        // Draw abcissa.
        g2.draw(new Line2D.Double(PADX, h-PADY + deltaY, w-PADX, h-PADY + deltaY));
        // Draw labels.
        Font font = g2.getFont();
        FontRenderContext frc = g2.getFontRenderContext();
        LineMetrics lm = font.getLineMetrics("0", frc);
        float sh = lm.getAscent() + lm.getDescent();
        // Ordinate label.
        String s = "Data";
        float sy = PADX + ((h - 2*PADX) - s.length()*sh)/2 + lm.getAscent();
        for(int i = 0; i < s.length(); i++) {
            String letter = String.valueOf(s.charAt(i));
            float sw = (float)font.getStringBounds(letter, frc).getWidth();
            float sx = (PADX - sw)/2;
            g2.drawString(letter, sx, sy);
            sy += sh;
        }
        // Abcissa label.
        s = "Time";
        sy = h - PADX + (PADX - sh)/2 + lm.getAscent();
        float sw = (float)font.getStringBounds(s, frc).getWidth();
        float sx = (w - sw)/2;
        g2.drawString(s, sx, sy);
        
        int start = hbar.getValue();
        int end = hbar.getValue() + pointsToPrint;
        
        //System.out.println("start: " + start + " end/dataSize: " + end + "/" + dataSize + " hbar: " + hbar.getValue());
        
        
        // Draw lines.
        double scaleX = (double)(w - 2*PADX)/(getMax(timeArray, start, end) - timeArray.get(start));
        double scaleY = (double)(h - 2*PADY)/getMax(dataArray, 0, dataSize)*(0.25 + zoomLevel*0.25);
        
        
        
        for(int i = start; i < end - 1; i++) {
            g2.setPaint(colorArr[dataArrayGroup.get(i).intValue()]);
            double x1 = PADX + timeArray.get(i)*scaleX - timeArray.get(start)*scaleX;
            double y1 = h - PADY - scaleY*dataArray.get(i) + deltaY;
            double x2 = PADX + timeArray.get(i+1)*scaleX - timeArray.get(start)*scaleX;
            double y2 = h - PADY - scaleY*dataArray.get(i+1) + deltaY;
            
            g2.draw(new Line2D.Double(x1, y1, x2, y2));
        }
//        // Mark data points.
        int curGroup = dataArrayGroup.get(0).intValue();
        float x_start = 0, y_start = 0;
        for(int i = start; i < end; i++) {
            if (curGroup != dataArrayGroup.get(i).intValue() || i == start) {
                if (curGroup != 0) {
                    g2.setPaint(colorArr[curGroup]);
                    double x = PADX + timeArray.get(i)*scaleX - timeArray.get(start)*scaleX;
                    double y = h - PADY - scaleY*dataArray.get(i) + deltaY;
                    g2.fill(new Ellipse2D.Double(x-2, y-2, 4, 4));
                    
                    if (i != start && (x - x_start) >= 10.0)
                        g2.drawString("E"+curGroup,  (x_start + ((float) x - x_start)/2),  (y_start + ((float) y - y_start)/2));
                    
                    x_start = (float) x;
                    y_start = (float) y;
                }
                
                
                curGroup = dataArrayGroup.get(i).intValue();
            }
        }
    }
 
    private double getMax(ArrayList<Double> data, int start, int end) {
        double max = -Double.MAX_VALUE;
        for(int i = start; i < end; i++) {
            if(data.get(i) > max)
                max = data.get(i) ;
        }
        return max;
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        // TODO Auto-generated method stub
        if (move) {
            dx = (e.getX() - initX)*(8*(zoomLevel+1) + 10);
            dy = (e.getY() - initY)*(1 + zoomLevel/10);
            
            initX = e.getX();
            initY = e.getY();
            
            //System.out.println("dx: " +dx + " dy " +dy + " hbar: " + hbar.getValue() + " vbar: " + vbar.getValue());
            
            hbar.setValue(hbar.getValue() + dx);
            vbar.setValue(vbar.getValue() + dy);
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub

    }

    @Override
    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub

    }
    
    private int dx, dy;
    private int initX, initY;
    private boolean move = false;
    
    @Override
    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        mousePressedState = e.getPoint();
        if (SwingUtilities.isMiddleMouseButton(e)) {
            if (zoomLevel < MAX_ZOOM)
                zoomLevel++;
        }
        
        int hbarValue = hbar.getValue();
        if (SwingUtilities.isRightMouseButton(e)) {
            if (zoomLevel > 1)
                zoomLevel--;
            hbarValue = hbarValue * (zoomLevel-1)/zoomLevel;
            
        }
        
        
        if (SwingUtilities.isLeftMouseButton(e)) {
            move = true;
            initX = e.getX();
            initY = e.getY();
        }
        

        
        
        pointsToPrint = (int) dataSize / zoomLevel;
        
        label.setText("Zoomlevel: " + Integer.toString(zoomLevel));
        
        BoundedRangeModel model = hbar.getModel();
        hbar.setValue(hbarValue);
        model.setExtent(pointsToPrint);
        hbar.setModel(model);
        repaint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        if (SwingUtilities.isMiddleMouseButton(e)) {
            move = false;
        }
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent arg0) {
        // TODO Auto-generated method stub
        
    }
}
