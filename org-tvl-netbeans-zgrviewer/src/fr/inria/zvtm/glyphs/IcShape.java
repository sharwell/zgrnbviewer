package fr.inria.zvtm.glyphs;

import java.awt.Component;
import java.awt.Graphics;
import java.awt.Polygon;
import javax.swing.Icon;

class IcShape extends GlyphIcon implements Icon {

    VShape glyph;
    int trS;
    Polygon p;
    int[] xcoords;
    int[] ycoords;
    double vertexAngle;
    float[] vertices;

    IcShape(VShape g, int w, int h) {
        this.glyph = g;
        this.width = w;
        this.height = h;
    }

    /**
     * set the glyph that the icon should be representing
     *
     * @param g glyph to be represented (should be a VShape (or subclass))
     */
    @Override
    public void setGlyph(Glyph g) {
        glyph = (VShape)g;
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
        computePolygon();
        if (glyph.isFilled()) {
            g.setColor(glyph.getColor());
            g.fillPolygon(p);
        }
        g.setColor(glyph.getBorderColor());
        g.drawPolygon(p);
    }

    protected void computePolygon() {
        trS = Math.min(width, height) / 2 - 2;
        vertexAngle = glyph.getOrient();
        vertices = glyph.getVertices();
        xcoords = new int[vertices.length];
        ycoords = new int[vertices.length];
        for (int j = 0; j < vertices.length - 1; j++) {
            xcoords[j] = (int)Math.round(cWidth + trS * Math.cos(vertexAngle) * vertices[j]);
            ycoords[j] = (int)Math.round(cHeight - trS * Math.sin(vertexAngle) * vertices[j]);
            vertexAngle += 2 * Math.PI / vertices.length;
        }//last iteration outside to loop to avoid one vertexAngle computation too many
        xcoords[vertices.length - 1] = (int)Math.round(cWidth + trS * Math.cos(vertexAngle) * vertices[vertices.length - 1]);
        ycoords[vertices.length - 1] = (int)Math.round(cHeight - trS * Math.sin(vertexAngle) * vertices[vertices.length - 1]);
        p = new Polygon(xcoords, ycoords, vertices.length);
    }

}
