/*
 * FILE: Context.java
 * DATE OF CREATION: Thu Jan 16 17:24:56 2003
 * AUTHOR : Emmanuel Pietriga (emmanuel@w3.org)
 * MODIF: Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 * Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 * Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 * Copyright (c) INRIA, 2004-2011. All Rights Reserved
 * Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Context.java 4271 2011-02-25 07:44:36Z epietrig $
 */
package fr.inria.zvtm.svg;

import java.awt.Color;
import java.awt.Font;
import java.util.StringTokenizer;

/**
 * Context information that is propagated while walking the SVG DOM tree.
 */
public class Context {

    String font_family;
    String font_size;
    String font_weight;
    String font_style;
    Color fill;
    Color stroke;
    boolean fillColorDefined = false;   // fill == null could have two meanings : it is not defined, or it is none
    boolean strokeColorDefined = false; // the boolean helps disambiguate these situations ; same thing for stroke
//     int stroke_width;
    Float fill_opacity;
    // title of the closest <g> ancestor (group)
    String title;
    // xlink:href of the closest <a> ancestor (link)
    String url;
    // xlink:title of the closest <a> ancestor (link)
    String url_title;
    String css_class;
    String closestAncestorGroupID;
    String closestAncestorGroupClass;

    /*
     * give it the value of a style attribute
     */
    Context() {
    }

    /*
     * give it the value of a style attribute
     */
    Context(String s) {
        if (s != null) {
            processStyleInfo(s);
        }
    }

    /*
     * give it the value of a style attribute - any previously set value will be overwritten
     */
    void add(String s) {
        processStyleInfo(s);
    }

    final void processStyleInfo(String styleInfo) {
        String[] ar = null;
        if (styleInfo != null) {
            StringTokenizer st = new StringTokenizer(styleInfo, ";");
            ar = new String[st.countTokens()];
            int i = 0;
            while (st.hasMoreTokens()) {
                ar[i++] = st.nextToken();
            }
        }
        if (ar != null) {
            for (int i = 0; i < ar.length; i++) {
                if (ar[i].startsWith(SVGReader._fill)) {
                    fill = SVGReader.getColor(ar[i].substring(SVGReader._fill.length()));
                    fillColorDefined = true;
                } else if (ar[i].startsWith(SVGReader._stroke)) {
                    SVGReader.getColor(ar[i].substring(SVGReader._stroke.length()));
                    strokeColorDefined = true;
                } else if (ar[i].startsWith(SVGReader._fillopacity)) {
                    fill_opacity = new Float(ar[i].substring(SVGReader._fillopacity.length()));
                } else if (ar[i].startsWith(SVGReader._fontfamilyCSS)) {
                    font_family = ar[i].substring(SVGReader._fontfamily.length());
                } else if (ar[i].startsWith(SVGReader._fontsize)) {
                    font_size = ar[i].substring(SVGReader._fontsizeCSS.length());
                    if (font_size.endsWith(SVGReader._pt)) {
                        font_size = font_size.substring(0, font_size.length() - 2);
                    }
                } else if (ar[i].startsWith(SVGReader._fontweight)) {
                    font_weight = ar[i].substring(SVGReader._fontweightCSS.length());
                } else if (ar[i].startsWith(SVGReader._fontstyle)) {
                    font_style = ar[i].substring(SVGReader._fontstyleCSS.length());
                }
            }
        }
    }

    /**
     * Has transparency information been declared. The value does not matter.
     */
    public boolean hasTransparencyInformation() {
        if (fill_opacity == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * Get fill (interior) color.
     */
    public Color getFillColor() {
        return fill;
    }

    /**
     * Has fill information been declared.
     */
    public boolean hasFillColorInformation() {
        return fillColorDefined;
    }

    /**
     * Get stroke (border) color.
     */
    public Color getStrokeColor() {
        return stroke;
    }

    /**
     * Has stroke color information been declared.
     */
    public boolean hasStrokeColorInformation() {
        return strokeColorDefined;
    }

    /**
     * Get alpha transparency value.
     *
     * @return alpha value in [0,1f]. 1.0 if opaque, 0 is fully transparent.
     */
    public float getAlphaTransparencyValue() {
        if (fill_opacity != null) {
            return fill_opacity.floatValue();
        } else {
            return 1.0f;
        }
    }

    /**
     * Get a Font object if there is enough information to create one, null if not.
     */
    Font getDefinedFont() {
        if (font_family != null || font_size != null || font_style != null || font_weight != null) {
            String fam = (font_family != null) ? font_family : "Default";
            int size;
            try {
                size = (font_size != null) ? Math.round((new Float(font_size)).floatValue()) : 10;
            } catch (NumberFormatException ex) {
                System.err.println("Warning: Font size value not supported (using default): " + font_size);
                size = 10;
            }
            int style;
            if (font_style != null && font_style.equals("italic")) {
                if (font_weight != null && font_weight.equals("bold")) {
                    style = Font.BOLD + Font.ITALIC;
                } else {
                    style = Font.ITALIC;
                }
            } else {
                if (font_weight != null && font_weight.equals("bold")) {
                    style = Font.BOLD;
                } else {
                    style = Font.PLAIN;
                }
            }
            return SVGReader.getFont(fam, style, size);
        } else {
            return null;
        }
    }

    /**
     * Set a URL for this part of the SVG document tree.
     */
    public void setURL(String s) {
        url = s;
    }

    /**
     * Get URL associated with this part of the SVG document tree.
     *
     * @return null if none specified.
     */
    public String getURL() {
        return url;
    }

    /**
     * Set a URL's title for this part of the SVG document tree.
     */
    public void setURLTitle(String s) {
        url_title = s;
    }

    /**
     * Get the URL's title associated with this part of the SVG document tree.
     *
     * @return null if none specified.
     */
    public String getURLTitle() {
        return url_title;
    }

    /**
     * Set a Title for this part of the SVG document tree.
     */
    public void setTitle(String s) {
        title = s;
    }

    /**
     * Get Title associated with this part of the SVG document tree.
     *
     * @return null if none specified.
     */
    public String getTitle() {
        return title;
    }

    public void setClosestAncestorGroupID(String s) {
        closestAncestorGroupID = s;
    }

    /**
     * @return null if none
     */
    public String getClosestAncestorGroupID() {
        return closestAncestorGroupID;
    }

    public void setClosestAncestorGroupClass(String s) {
        closestAncestorGroupClass = s;
    }

    /**
     * @return null if none
     */
    public String getClosestAncestorGroupClass() {
        return closestAncestorGroupClass;
    }

    public Context duplicate() {
        Context copy = new Context(null);
        copy.font_family = this.font_family;
        copy.font_size = this.font_size;
        copy.font_weight = this.font_weight;
        copy.font_style = this.font_style;
        copy.fill = this.fill;
        copy.stroke = this.stroke;
        copy.fillColorDefined = this.fillColorDefined;
        copy.strokeColorDefined = this.strokeColorDefined;
        copy.fill_opacity = this.fill_opacity;
        copy.url = this.url;
        copy.title = this.title;
        copy.url_title = this.url_title;
        copy.closestAncestorGroupID = this.closestAncestorGroupID;
        copy.closestAncestorGroupClass = this.closestAncestorGroupClass;
        return copy;
    }

}
