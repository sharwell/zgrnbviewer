package fr.inria.zvtm.glyphs;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import javax.swing.JPanel;

class GlyphPanel extends JPanel {

    GlyphFactory gf;
    int pw, ph; //panel width and height
    int cx, cy, cs; //glyph X Y and size in panel
    double vertexAngle;
    double[] vertices;
    int[] xcoords;
    int[] ycoords;
    Color fColor, bColor;
    double alpha = 1.0;
    int selectedVertex = -1;  //-1 if none, [0..n] if vertex, -2 if orientation handle (always -1 or -2 for all glyphs except VShape and subclasses)
    int hdx, hdy;  //temporary variable used for vertex selection
    int xorientHandle, yorientHandle;
    Object alias = RenderingHints.VALUE_ANTIALIAS_OFF;
    boolean displayGrid = false;
    boolean displayIndicators = true;
    Stroke continuous;
    static float dash1[] = {5.0f};
    @SuppressWarnings("StaticNonFinalUsedInInitialization")
    static Stroke dashed = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
    static AlphaComposite acO = AlphaComposite.getInstance(AlphaComposite.SRC_OVER);  //opaque
    AlphaComposite acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)alpha);
    static Color gridGray = new Color(190, 190, 190);
    Graphics2D g2d;

    GlyphPanel(GlyphFactory gfact) {
        super(true);
        gf = gfact;
        fColor = gf.fillColor;
        bColor = gf.borderColor;
        alpha = gf.alpha;
        acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)alpha);
    }

    void setColor(Color c1, Color c2) {
        fColor = c1;
        bColor = c2;
        repaint();
    }

    void setAngle(double a) {
        vertexAngle = a;
        repaint();
    }

    void setTransparency(double t) {
        alpha = t;
        acST = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)alpha);
        repaint();
    }

    void setVertices(double[] array) {
        vertices = new double[array.length];
        xcoords = new int[vertices.length];
        ycoords = new int[vertices.length];
        System.arraycopy(array, 0, vertices, 0, array.length);
        repaint();
    }

    void setAntialiasing(boolean b) {
        if (b) {
            alias = RenderingHints.VALUE_ANTIALIAS_ON;
        } else {
            alias = RenderingHints.VALUE_ANTIALIAS_OFF;
        }
        repaint();
    }

    void setGrid(boolean b) {
        displayGrid = b;
        repaint();
    }

    void setGeom(boolean b) {
        displayIndicators = b;
        repaint();
    }

    @Override
    public void paint(Graphics g) {
        pw = this.getSize().width;
        ph = this.getSize().height;
        g2d = (Graphics2D)g;
        g2d.setColor(GlyphFactory.PANEL_BKG);
        g2d.fillRect(0, 0, pw, ph);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, alias);
        continuous = g2d.getStroke();
        vertexAngle = gf.angle;
        cs = Math.round(Math.min(pw, ph) / 2 - 20);
        cx = pw / 2;
        cy = ph / 2;
        if (displayGrid) {
            g2d.setColor(gridGray);
            for (int i = 0; i < pw; i += 30) {
                g2d.drawLine(0, i, pw, i);
                g2d.drawLine(i, 0, i, ph);
            }
        }
        if (gf.orientable && displayIndicators) {
            g2d.setColor(Color.black);
            xorientHandle = (int)Math.round(-Math.sin(gf.angle) * cs * 1.1) + cx;
            yorientHandle = -(int)Math.round(Math.cos(gf.angle) * cs * 1.1) + cy;
            g2d.drawLine(cx, cy, xorientHandle, yorientHandle);
            if (selectedVertex == -2) {
                g2d.setColor(Color.red);
            }
            g2d.fillOval(xorientHandle - 4, yorientHandle - 4, 8, 8);
        }
    }

    void orientDrag(MouseEvent e) {
        if (selectedVertex == -2) {
            hdx = e.getX() - cx;
            hdy = e.getY() - cy;
            if (hdy != 0) {
                if (hdy < 0) {
                    if (hdx < 0) {
                        gf.updateAngleSpin(Math.atan(((double)hdx) / ((double)hdy)));
                    } else {
                        gf.updateAngleSpin(2 * Math.PI + Math.atan(((double)hdx) / ((double)hdy)));
                    }
                } else {
                    if (hdx < 0) {
                        gf.updateAngleSpin(Math.PI + Math.atan(((double)hdx) / ((double)hdy)));
                    } else {
                        gf.updateAngleSpin(Math.PI + Math.atan(((double)hdx) / ((double)hdy)));
                    }
                }
            } else {
                if (hdx > 0) {
                    gf.updateAngleSpin(3 * Math.PI / 2.0);
                } else {
                    gf.updateAngleSpin(Math.PI / 2.0);
                }
            }
            //do not repaint as a repaint will be triggered as a consequence of updateAngleSpin
            //when doing JSpinner.setValue() which fires a ChangeEvent
        }
    }

}
