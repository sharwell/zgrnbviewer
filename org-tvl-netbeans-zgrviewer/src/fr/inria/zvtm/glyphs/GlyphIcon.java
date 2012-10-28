/*
 * FILE: GlyphIcon.java DATE OF CREATION: Thu Oct 17 16:17:13 2002 AUTHOR :
 * Emmanuel Pietriga (emmanuel@w3.org) MODIF: Fri Aug 01 13:02:13 2003 by
 * Emmanuel Pietriga (emmanuel@w3.org, emmanuel@claribole.net) Copyright (c)
 * Emmanuel Pietriga, 2002. All Rights Reserved Copyright (c) INRIA, 2004-2010.
 * All Rights Reserved Licensed under the GNU LGPL. For full terms see the file
 * COPYING.
 *
 * $Id: GlyphIcon.java 4264 2011-02-23 05:14:18Z epietrig $
 */
package fr.inria.zvtm.glyphs;

/**
 * Icon representing a Glyph (can be used to represent a Glyph in any Java/Swing
 * component including non-ZVTM components)
 *
 * @author Emmanuel Pietriga
 */
public abstract class GlyphIcon {

    int width;
    int height;
    int cWidth;   //half component width
    int cHeight;  //half component height

    /**
     * Use this method to get a GlyphIcon (you shoud not use a Ic* constructor
     * directly nor GlyphIcon()) ; all objects returned by this method implement
     * javax.swing.Icon
     *
     * @param g glyph to be represented
     * @param w icon width (should be greater than 0)
     * @param h icon height (should be greater than 0)
     */
    public static javax.swing.Icon getGlyphIcon(Glyph g, int w, int h) {
        if (g instanceof VShape) {
            return new IcShape((VShape)g, w, h);
        } else if (g instanceof VRectangle) {
            return new IcRectangle((VRectangle)g, w, h);
        } else if (g instanceof VCircle) {
            return new IcCircle((VCircle)g, w, h);
        } else if (g instanceof VEllipse) {
            return new IcEllipse((VEllipse)g, w, h);
        } else if (g instanceof VRoundRect) {
            return new IcRoundRect((VRoundRect)g, w, h);
        } else {
            return null;
        }
    }

    /**
     * set the glyph that the icon should be representing
     *
     * @param g glyph to be represented (warning: the glyph type cannot be
     * changed, only its attributes ; this means that if the Icon was
     * instantiated as a VShape, only a VShape can be provided as parameter,
     * etc...)
     */
    public abstract void setGlyph(Glyph g);

    /**
     * get the glyph that the icon is representing
     */
    public abstract Glyph getGlyph();

    /**
     * set the icon's width and height
     *
     * @param w width (must be greater than 0)
     * @param h height (must be greater than 0)
     */
    public void setIconWidthHeight(int w, int h) {
        if (w > 0) {
            width = w;
        }
        if (h > 0) {
            height = h;
        }
    }

}
