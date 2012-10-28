package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

class VOctPanel extends GlyphPanel implements MouseMotionListener, MouseListener {

    VOctPanel(GlyphFactory gfact) {
        super(gfact);
        xcoords = new int[8];
        ycoords = new int[8];
    }

    Polygon p;
    int hs;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        hs = cs / 2;
        xcoords[0] = (int)Math.round((cs * Math.cos(vertexAngle) - hs * Math.sin(vertexAngle)) + cx);
        xcoords[1] = (int)Math.round((hs * Math.cos(vertexAngle) - cs * Math.sin(vertexAngle)) + cx);
        xcoords[2] = (int)Math.round((-hs * Math.cos(vertexAngle) - cs * Math.sin(vertexAngle)) + cx);
        xcoords[3] = (int)Math.round((-cs * Math.cos(vertexAngle) - hs * Math.sin(vertexAngle)) + cx);
        xcoords[4] = (int)Math.round((-cs * Math.cos(vertexAngle) + hs * Math.sin(vertexAngle)) + cx);
        xcoords[5] = (int)Math.round((-hs * Math.cos(vertexAngle) + cs * Math.sin(vertexAngle)) + cx);
        xcoords[6] = (int)Math.round((hs * Math.cos(vertexAngle) + cs * Math.sin(vertexAngle)) + cx);
        xcoords[7] = (int)Math.round((cs * Math.cos(vertexAngle) + hs * Math.sin(vertexAngle)) + cx);
        ycoords[0] = (int)Math.round((-hs * Math.cos(vertexAngle) - cs * Math.sin(vertexAngle)) + cy);
        ycoords[1] = (int)Math.round((-cs * Math.cos(vertexAngle) - hs * Math.sin(vertexAngle)) + cy);
        ycoords[2] = (int)Math.round((-cs * Math.cos(vertexAngle) + hs * Math.sin(vertexAngle)) + cy);
        ycoords[3] = (int)Math.round((-hs * Math.cos(vertexAngle) + cs * Math.sin(vertexAngle)) + cy);
        ycoords[4] = (int)Math.round((hs * Math.cos(vertexAngle) + cs * Math.sin(vertexAngle)) + cy);
        ycoords[5] = (int)Math.round((cs * Math.cos(vertexAngle) + hs * Math.sin(vertexAngle)) + cy);
        ycoords[6] = (int)Math.round((cs * Math.cos(vertexAngle) - hs * Math.sin(vertexAngle)) + cy);
        ycoords[7] = (int)Math.round((hs * Math.cos(vertexAngle) - cs * Math.sin(vertexAngle)) + cy);
        p = new Polygon(xcoords, ycoords, 8);
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
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        orientDrag(e);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        if ((gf.orientable) && (GlyphFactory.hasEditableAngle()) && (xorientHandle - 3 <= mx) && (mx <= xorientHandle + 3) && (yorientHandle - 3 <= my) && (my <= yorientHandle + 3)) {
            selectedVertex = -2;
        } else {
            selectedVertex = -1;
        }
        repaint();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        selectedVertex = -1;
        gf.setVertexVal(-1.0);
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
    }

}
