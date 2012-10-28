package fr.inria.zvtm.glyphs;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

class VCirPanel extends GlyphPanel implements MouseMotionListener, MouseListener {

    VCirPanel(GlyphFactory gfact) {
        super(gfact);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (alpha > 0.0) {
            if (alpha == 1.0) {
                g2d.setColor(fColor);
                g2d.fillOval(cx - cs, cy - cs, 2 * cs, 2 * cs);
            } else {
                g2d.setColor(fColor);
                g2d.setComposite(acST);
                g2d.fillOval(cx - cs, cy - cs, 2 * cs, 2 * cs);
                g2d.setComposite(acO);
            }
        }
        g2d.setColor(bColor);
        g2d.drawOval(cx - cs, cy - cs, 2 * cs, 2 * cs);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {
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
