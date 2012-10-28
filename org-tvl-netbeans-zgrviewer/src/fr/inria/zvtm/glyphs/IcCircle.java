package fr.inria.zvtm.glyphs;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

class IcCircle extends GlyphIcon implements Icon {

    VCircle glyph;
    int trS;

    IcCircle(VCircle g, int w, int h) {
        this.glyph = g;
        this.width = w;
        this.height = h;
    }

    /**
     * set the glyph that the icon should be representing
     *
     * @param g glyph to be represented (should be a VCircle (or subclass))
     */
    @Override
    public void setGlyph(Glyph g) {
        glyph = (VCircle)g;
    }

    /**
     * get the glyph that the icon is representing
     */
    @Override
    public Glyph getGlyph() {
        return glyph;
    }

    /**
     * get the icon's width (Icon interface)
     */
    @Override
    public int getIconHeight() {
        return height;
    }

    /**
     * get the icon's height (Icon interface)
     */
    @Override
    public int getIconWidth() {
        return width;
    }

    /**
     * Icon interface
     */
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
        cWidth = c.getWidth() / 2;
        cHeight = c.getHeight() / 2;
        trS = Math.min(width, height) / 2 - 2;
        if (glyph.isFilled()) {
            g.setColor(glyph.getColor());
            g.fillOval(cWidth - trS, cHeight - trS, 2 * trS, 2 * trS);
        }
        g.setColor(glyph.getBorderColor());
        g.drawOval(cWidth - trS, cHeight - trS, 2 * trS, 2 * trS);
    }

}
