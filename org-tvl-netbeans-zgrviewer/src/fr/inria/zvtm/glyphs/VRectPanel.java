package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

class VRectPanel extends GlyphPanel implements MouseMotionListener, MouseListener {

    int cw, ch;
    int arhx = -1;
    int arhy = -1;
    int hdx2, hdy2;
    double ratioAngle;

    VRectPanel(GlyphFactory gfact) {
        super(gfact);
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        if (arhx == -1 && arhy == -1) {
            hdx2 = Math.round(cx * 0.707f);
            hdy2 = Math.round(cy * 0.707f);
            hdx = Math.abs(hdx2);
            hdy = Math.abs(hdy2);
            updateRatioHandle();
        }
        if (hdx == 0 && hdy == 0) {
            gf.aspectRatio = 1.0;
        } else {
            gf.aspectRatio = Math.abs((double)hdx / (double)hdy);
        }
        if (gf.aspectRatio == 1.0) {
            cw = cs;
            ch = cs;
        } else if (gf.aspectRatio > 1.0) {
            cw = cs;
            ch = (int)Math.round(cs / gf.aspectRatio);
        } else {
            cw = (int)Math.round(cs * gf.aspectRatio);
            ch = cs;
        }
        if (alpha > 0.0) {
            if (alpha == 1.0) {
                g2d.setColor(fColor);
                g2d.fillRect(cx - cw, cy - ch, 2 * cw, 2 * ch);
            } else {
                g2d.setColor(fColor);
                g2d.setComposite(acST);
                g2d.fillRect(cx - cw, cy - ch, 2 * cw, 2 * ch);
                g2d.setComposite(acO);
            }
        }
        g2d.setColor(bColor);
        g2d.drawRect(cx - cw, cy - ch, 2 * cw, 2 * ch);
        if (displayIndicators) {
            if (selectedVertex == 0) {
                g2d.setColor(Color.red);
                g2d.fillRect(arhx - 3, arhy - 3, 6, 6);
                g2d.setColor(Color.black);
            } else {
                g2d.setColor(Color.black);
                g2d.fillRect(arhx - 3, arhy - 3, 6, 6);
            }
            g2d.setStroke(dashed);
            g2d.drawOval(0, 0, pw - 1, ph - 1);
            g2d.drawLine(cx, cy, arhx, arhy);
        }
    }

    void updateRatioHandle() {
        if (hdy2 != 0) {
            if (hdy2 < 0) {
                if (hdx2 < 0) {
                    ratioAngle = Math.atan(((double)hdx2) / ((double)hdy2));
                } else {
                    ratioAngle = 2 * Math.PI + Math.atan(((double)hdx2) / ((double)hdy2));
                }
            } else {
                if (hdx2 < 0) {
                    ratioAngle = Math.PI + Math.atan(((double)hdx2) / ((double)hdy2));
                } else {
                    ratioAngle = Math.PI + Math.atan(((double)hdx2) / ((double)hdy2));
                }
            }
        } else {
            if (hdx2 > 0) {
                ratioAngle = 3 * Math.PI / 2.0;
            } else {
                ratioAngle = Math.PI / 2.0;
            }
        }
        arhx = cx - (int)Math.round(Math.sin(ratioAngle) * cx);
        arhy = cy - (int)Math.round(Math.cos(ratioAngle) * cy);
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (selectedVertex == 0) {
            int mx = e.getX();
            int my = e.getY();
            hdx2 = mx - cx;
            hdy2 = my - cy;
            hdx = Math.abs(hdx2);
            hdy = Math.abs(hdy2);
            updateRatioHandle();
            repaint();
        } else if (selectedVertex == -2) {
            orientDrag(e);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        int mx = e.getX();
        int my = e.getY();
        boolean selectedSomething = false;
        if (GlyphFactory.hasEditableVertexValues()) {
            if ((arhx - 3 <= mx) && (mx <= arhx + 3) && (arhy - 3 <= my) && (my <= arhy + 3)) {
                selectedVertex = 0;  //east
                selectedSomething = true;
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
