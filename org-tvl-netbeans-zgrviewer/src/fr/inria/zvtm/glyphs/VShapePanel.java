package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

class VShapePanel extends GlyphPanel implements MouseMotionListener, MouseListener {

    Polygon p;
    int x2, y2;  //temporary variables used for vertex computation
    double tmpD; //temporary variable used for vertex selection

    VShapePanel(GlyphFactory gfact) {
        super(gfact);
        vertices = gf.vertices;
        xcoords = new int[vertices.length];
        ycoords = new int[vertices.length];
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (int j = 0; j < vertices.length - 1; j++) {
            xcoords[j] = (int)Math.round(cx + cs * Math.cos(vertexAngle) * vertices[j]);
            ycoords[j] = (int)Math.round(cy - cs * Math.sin(vertexAngle) * vertices[j]);
            vertexAngle += 2 * Math.PI / vertices.length;
        }//last iteration outside to loop to avoid one vertxAngle computation too many
        xcoords[vertices.length - 1] = (int)Math.round(cx + cs * Math.cos(vertexAngle) * vertices[vertices.length - 1]);
        ycoords[vertices.length - 1] = (int)Math.round(cy - cs * Math.sin(vertexAngle) * vertices[vertices.length - 1]);
        p = new Polygon(xcoords, ycoords, xcoords.length);
        if (alpha > 0.0) {
            if (alpha == 1.0) {
                g2d.setColor(fColor);
                g2d.fillPolygon(p);
            } else {
                g2d.setColor(fColor);
                g2d.setComposite(acST);
                g2d.fillPolygon(p);
                g2d.setComposite(acO);
            }
        }
        g2d.setColor(bColor);
        g2d.drawPolygon(p);
        g2d.setColor(Color.black);
        //bounding circle and vertex segments
        if (displayIndicators) {
            g2d.setStroke(dashed);
            g2d.drawOval(cx - cs, cy - cs, 2 * cs, 2 * cs);
            vertexAngle = gf.angle;
            for (int i = 0; i < xcoords.length; i++) {
                x2 = (int)Math.round(cx + cs * Math.cos(vertexAngle));
                y2 = (int)Math.round(cy - cs * Math.sin(vertexAngle));
                g2d.drawLine(cx, cy, x2, y2);
                if (i == selectedVertex) {
                    g2d.setColor(Color.red);
                    g2d.fillRect(xcoords[i] - 3, ycoords[i] - 3, 6, 6);
                    g2d.setColor(Color.black);
                } else {
                    g2d.fillRect(xcoords[i] - 3, ycoords[i] - 3, 6, 6);
                }
                vertexAngle += 2 * Math.PI / vertices.length;
            }
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedVertex >= 0) {
            tmpD = Math.sqrt(Math.pow(e.getX() - cx, 2) + Math.pow(e.getY() - cy, 2)) / ((double)cs);
            if (tmpD < 0) {
                tmpD = 0.0;
            } else if (tmpD > 1.0) {
                tmpD = 1.0;
            }
            vertices[selectedVertex] = tmpD;
            gf.setVertexVal(tmpD);
            repaint();
        } else {
            orientDrag(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        boolean selectedSomething = false;
        if (GlyphFactory.hasEditableVertexValues()) {
            for (int i = 0; i < xcoords.length; i++) {
                if ((xcoords[i] - 3 <= mx) && (mx <= xcoords[i] + 3) && (ycoords[i] - 3 <= my) && (my <= ycoords[i] + 3)) {
                    selectedVertex = i;
                    selectedSomething = true;
                    break;
                }
            }
        }
        if (!selectedSomething) {
            if ((gf.orientable) && (GlyphFactory.hasEditableAngle()) && (xorientHandle - 3 <= mx) && (mx <= xorientHandle + 3) && (yorientHandle - 3 <= my) && (my <= yorientHandle + 3)) {
                selectedVertex = -2;
            } else {
                selectedVertex = -1;
            }
        }
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        boolean selectedSomething = false;
        if (GlyphFactory.hasEditableVertexValues()) {
            for (int i = 0; i < xcoords.length; i++) {
                if ((xcoords[i] - 3 <= mx) && (mx <= xcoords[i] + 3) && (ycoords[i] - 3 <= my) && (my <= ycoords[i] + 3)) {
                    selectedSomething = true;
                    selectedVertex = i;
                    gf.setVertexVal(vertices[i]);
                    break;
                }
            }
        }
        if (!selectedSomething) {
            selectedVertex = -1;
            gf.setVertexVal(-1.0);
        }
        repaint();
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        gf.vertices = this.vertices;
        //selectedVertex=-1;
    }

}
