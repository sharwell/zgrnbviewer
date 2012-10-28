package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Polygon;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

class VDiamPanel extends GlyphPanel implements MouseMotionListener, MouseListener {

    VDiamPanel(GlyphFactory gfact) {
        super(gfact);
        xcoords = new int[4];
        ycoords = new int[4];
    }

    Polygon p;

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        xcoords[0] = (int)Math.round(cx + cs * Math.cos(vertexAngle));
        xcoords[1] = (int)Math.round(cx - cs * Math.sin(vertexAngle));
        xcoords[2] = (int)Math.round(cx - cs * Math.cos(vertexAngle));
        xcoords[3] = (int)Math.round(cx + cs * Math.sin(vertexAngle));
        ycoords[0] = (int)Math.round(cy - cs * Math.sin(vertexAngle));
        ycoords[1] = (int)Math.round(cy - cs * Math.cos(vertexAngle));
        ycoords[2] = (int)Math.round(cy + cs * Math.sin(vertexAngle));
        ycoords[3] = (int)Math.round(cy + cs * Math.cos(vertexAngle));
        p = new Polygon(xcoords, ycoords, 4);
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
