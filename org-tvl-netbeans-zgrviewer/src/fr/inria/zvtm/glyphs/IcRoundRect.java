package fr.inria.zvtm.glyphs;

import java.awt.Component;
import java.awt.Graphics;
import javax.swing.Icon;

class IcRoundRect extends GlyphIcon implements Icon {

    VRoundRect glyph;
    double rW, rH;  //rectangle half width and height
    int trW, trH;
    double factor;  //projection factor

    IcRoundRect(VRoundRect g, int w, int h) {
        this.glyph = g;
        this.width = w;
        this.height = h;
    }

    /**
     * set the glyph that the icon should be representing
     *
     * @param g glyph to be represented (should be a VRectangle (or subclass))
     */
    @Override
    public void setGlyph(Glyph g) {
        glyph = (VRoundRect)g;
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
        rW = glyph.getWidth();
        rH = glyph.getHeight();
        factor = Math.max(rW / (double)width, rH / (double)height);
        trW = (int)Math.round(rW / (factor * 2)) - 2;  //-2 so that it leaves a 1 pixel border blank
        trH = (int)Math.round(rH / (factor * 2)) - 2;  //around it and the component's border
        if (glyph.isFilled()) {
            g.setColor(glyph.getColor());
            g.fillRoundRect(cWidth - trW, cHeight - trH, 2 * trW, 2 * trH, trW, trH);
        }
        g.setColor(glyph.getBorderColor());
        g.drawRoundRect(cWidth - trW, cHeight - trH, 2 * trW, 2 * trH, trW, trH);
    }

}
