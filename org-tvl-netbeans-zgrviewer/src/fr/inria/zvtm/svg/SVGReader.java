/*
 * FILE: SVGReader.java
 * DATE OF CREATION: Oct 16 2001
 * AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 * MODIF: Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 * Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2000-2002. All Rights Reserved
 * Copyright (c) 2003 World Wide Web Consortium. All Rights Reserved
 * Copyright (c) INRIA, 2004-2011. All Rights Reserved
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For full terms see the file COPYING.
 *
 * $Id: SVGReader.java 4271 2011-02-25 07:44:36Z epietrig $
 */
package fr.inria.zvtm.svg;

import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VEllipse;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangleOr;
import fr.inria.zvtm.glyphs.VRoundRect;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VText;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import javax.swing.ImageIcon;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * An <a href="http://www.w3.org/Graphics/SVG/">SVG</a> interpreter for ZVTM.
 * For now SVGReader covers a <i><b>very</b></i> limited subset of the specification.
 * Enough to interpret GraphViz layout programs' SVG output (Ellipse, Text, Path, Rectangle, Circle, limited support for Polygon and Image).
 * The goal is to have it support at least <a href="http://www.w3.org/TR/SVGTiny12/">SVG Tiny</a>.
 *
 * @author Emmanuel Pietriga
 */
public class SVGReader {

    private SVGReader() {
    }

    public static final String _polyline = "polyline";
    public static final String _line = "line";
    public static final String _rect = "rect";
    public static final String _image = "image";
    public static final String _ellipse = "ellipse";
    public static final String _circle = "circle";
    public static final String _path = "path";
    public static final String _text = "text";
    public static final String _polygon = "polygon";
    public static final String _g = "g";
    public static final String _a = "a";
    public static final String _title = "title";
    public static final String _fill = "fill";
    public static final String _stroke = "stroke";
    public static final String _strokewidth = "stroke-width";
    public static final String _strokedasharray = "stroke-dasharray";
    public static final String _fillopacity = "fill-opacity";
    public static final String _fontfamily = "font-family";
    public static final String _fontsize = "font-size";
    public static final String _fontweight = "font-weight";
    public static final String _fontstyle = "font-style";
    public static final String _fillCSS = _fill + ":";
    public static final String _strokeCSS = _stroke + ":";
    public static final String _strokewidthCSS = _strokewidth + ":";
    public static final String _strokedasharrayCSS = _strokedasharray + ":";
    public static final String _fillopacityCSS = _fillopacity + ":";
    public static final String _fontfamilyCSS = _fontfamily + ":";
    public static final String _fontsizeCSS = _fontsize + ":";
    public static final String _fontweightCSS = _fontweight + ":";
    public static final String _fontstyleCSS = _fontstyle + ":";
    public static final String _style = "style";
    public static final String _class = "class";
    public static final String _cx = "cx";
    public static final String _cy = "cy";
    public static final String _rx = "rx";
    public static final String _ry = "ry";
    public static final String _r = "r";
    public static final String _d = "d";
    public static final String _x = "x";
    public static final String _y = "y";
    public static final String _x1 = "x1";
    public static final String _y1 = "y1";
    public static final String _x2 = "x2";
    public static final String _y2 = "y2";
    public static final String _width = "width";
    public static final String _height = "height";
    public static final String _points = "points";
    public static final String _textanchor = "text-anchor";
    public static final String _start = "start";
    public static final String _middle = "middle";
    public static final String _end = "end";
    public static final String _inherit = "inherit";
    public static final String _pt = "pt";
    public static final String _id = "id";
    public static final String _transform = "transform";
    public static final String _translate = "translate";
    public static final String _viewBox = "viewBox";
    public static final String xlinkURI = "http://www.w3.org/1999/xlink";
    public static final String _href = "href";
    public static final String HTTP_SCHEME = "http://";
    public static final String FILE_SCHEME = "file:/";
    public static final String FILE_PROTOCOL = "file";
    public static final String EMPTY_STRING = "";
    public static final String COMMA_SEP = ",";
    public static final String SPACE_SEP = " ";
    public static final String LINE_SEP = "\n";
    static final String STYLING_INSTRUCTION_SEP = ";";
    static Map<String, List<List<Object>>> fontCache = new HashMap<String, List<List<Object>>>();
    static double xoffset = 0;
    static double yoffset = 0;
    static double scale = 1.0;
    public static float RRARCR = 0.4f;

    /*
     * maps stroke attributes to a corresponding BasicStroke instance
     * key = Float defining stroke width
     * value = Hashtable for which:
     * key = dasharray as a string of comma-separated float values
     * (or string "solid" for solid stroke with no specific dashing pattern)
     * value = BasicStroke instance
     */
    protected static Map<Float, Map<String, Stroke>> strokes = new HashMap<Float, Map<String, Stroke>>();
    protected static String SOLID_DASH_PATTERN = "solid";

    /**
     * Set position offset.
     * When this is set to something different than (0,0), all SVG objects will be translated by (dx,dy) in the ZVTM virtual space coordinates system.
     * This can be useful if you do not want all objects of the SVG file to all be in the south-east quadrant of the virtual space.
     * SVG files often use positive coordinates only, and their coordinate system is inversed (vertically) w.r.t ZVTM's coordinate system.
     */
    public static void setPositionOffset(double dx, double dy) {
        xoffset = dx;
        yoffset = dy;
    }

    /**
     * Get the position offset. (0,0) means no offset.
     */
    public static Point2D.Double getPositionOffset() {
        return new Point2D.Double(xoffset, yoffset);
    }

    /**
     * Check that an SVG path expression is supported by ZVTM.
     * Verifies that all SVG commands are actually supported by ZVTM (M,m,L,l,H,h,V,v,C,c,Q,q)
     */
    public static boolean checkSVGPath(String svg) {
        boolean res = true;
        byte[] chrs = svg.getBytes();
        for (int i = 0; i < chrs.length; i++) {
            if (!((chrs[i] == 32) || // space
                (chrs[i] == 45) || // minus
                ((chrs[i] >= 48) && (chrs[i] <= 57)) ||// 0-9
                (chrs[i] == 67) || // C
                (chrs[i] == 72) || // H
                (chrs[i] == 76) || // L
                (chrs[i] == 77) || // M
                (chrs[i] == 81) || // Q
                (chrs[i] == 86) || // V
                (chrs[i] == 99) || // c
                (chrs[i] == 104) || // h
                (chrs[i] == 108) || // l
                (chrs[i] == 109) || // m
                (chrs[i] == 113) || // q
                (chrs[i] == 118) || // v
                (chrs[i] == 101) || // e (exponent in floating-point-constant)
                (chrs[i] == 69) || // E (exponent in floating-point-constant)
                (chrs[i] == 44) || // comma
                (chrs[i] == 46))) {                     // point
                res = false;
                System.err.println("SVG Path: char '" + svg.substring(i, i + 1) + "' not supported");
                break;
            }
        }
        return res;
    }

    private static void processNextSVGPathCommand(StringBuffer svg, DPath ph, StringBuffer lastCommand) {
        if (svg.length() > 0) {
            switch (svg.charAt(0)) {
            case 'M': {
                svg.deleteCharAt(0);
                double x = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y = getNextNumber(svg) + yoffset;
                ph.jump(x, -y, true);
                lastCommand.setCharAt(0, 'M');
                break;
            }
            case 'm': {
                svg.deleteCharAt(0);
                double x = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y = getNextNumber(svg) + yoffset;
                ph.jump(x, -y, false);
                lastCommand.setCharAt(0, 'm');
                break;
            }
            case 'L': {
                svg.deleteCharAt(0);
                double x = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y = getNextNumber(svg) + yoffset;
                ph.addSegment(x, -y, true);
                lastCommand.setCharAt(0, 'L');
                break;
            }
            case 'l': {
                svg.deleteCharAt(0);
                double x = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y = getNextNumber(svg) + yoffset;
                ph.addSegment(x, -y, false);
                lastCommand.setCharAt(0, 'l');
                break;
            }
            case 'H': {
                svg.deleteCharAt(0);
                double x = getNextNumber(svg) + xoffset;
                ph.addSegment(x, 0, true);
                lastCommand.setCharAt(0, 'H');
                break;
            }
            case 'h': {
                svg.deleteCharAt(0);
                double x = getNextNumber(svg) + xoffset;
                ph.addSegment(x, 0, false);
                lastCommand.setCharAt(0, 'h');
                break;
            }
            case 'V': {
                svg.deleteCharAt(0);
                double y = getNextNumber(svg) + yoffset;
                ph.addSegment(0, -y, true);
                lastCommand.setCharAt(0, 'V');
                break;
            }
            case 'v': {
                svg.deleteCharAt(0);
                double y = getNextNumber(svg) + yoffset;
                ph.addSegment(0, -y, false);
                lastCommand.setCharAt(0, 'v');
                break;
            }
            case 'C': {
                svg.deleteCharAt(0);
                double x1 = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y1 = getNextNumber(svg) + yoffset;
                double x2 = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y2 = getNextNumber(svg) + yoffset;
                double x = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y = getNextNumber(svg) + yoffset;
                ph.addCbCurve(x, -y, x1, -y1, x2, -y2, true);
                lastCommand.setCharAt(0, 'C');
                break;
            }
            case 'c': {
                svg.deleteCharAt(0);
                double x1 = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y1 = getNextNumber(svg) + yoffset;
                double x2 = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y2 = getNextNumber(svg) + yoffset;
                double x = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y = getNextNumber(svg) + yoffset;
                ph.addCbCurve(x, -y, x1, -y1, x2, -y2, false);
                lastCommand.setCharAt(0, 'c');
                break;
            }
            case 'Q': {
                svg.deleteCharAt(0);
                double x1 = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y1 = getNextNumber(svg) + yoffset;
                double x = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y = getNextNumber(svg) + yoffset;
                ph.addQdCurve(x, -y, x1, -y1, true);
                lastCommand.setCharAt(0, 'Q');
                break;
            }
            case 'q': {
                svg.deleteCharAt(0);
                double x1 = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y1 = getNextNumber(svg) + yoffset;
                double x = getNextNumber(svg) + xoffset;
                //seekSecondCoord(svg);
                double y = getNextNumber(svg) + yoffset;
                ph.addQdCurve(x, -y, x1, -y1, false);
                lastCommand.setCharAt(0, 'q');
                break;
            }
            default: {
                //same command as previous point
                //the string has been checked by checkSVGPath, therefore only possible chars are digits 1234567890 and -
                switch (lastCommand.charAt(0)) {
                case 'M': {
                    double x = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y = getNextNumber(svg) + yoffset;
                    ph.jump(x, -y, true);
                    break;
                }
                case 'm': {
                    double x = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y = getNextNumber(svg) + yoffset;
                    ph.jump(x, -y, false);
                    break;
                }
                case 'L': {
                    double x = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y = getNextNumber(svg) + yoffset;
                    ph.addSegment(x, -y, true);
                    break;
                }
                case 'l': {
                    double x = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y = getNextNumber(svg) + yoffset;
                    ph.addSegment(x, -y, false);
                    break;
                }
                case 'H': {
                    double x = getNextNumber(svg) + xoffset;
                    ph.addSegment(x, 0, true);
                    break;
                }
                case 'h': {
                    double x = getNextNumber(svg) + xoffset;
                    ph.addSegment(x, 0, false);
                    break;
                }
                case 'V': {
                    double y = getNextNumber(svg) + yoffset;
                    ph.addSegment(0, -y, true);
                    break;
                }
                case 'v': {
                    double y = getNextNumber(svg) + yoffset;
                    ph.addSegment(0, -y, false);
                    break;
                }
                case 'C': {
                    double x1 = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y1 = getNextNumber(svg) + yoffset;
                    double x2 = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y2 = getNextNumber(svg) + yoffset;
                    double x = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y = getNextNumber(svg) + yoffset;
                    ph.addCbCurve(x, -y, x1, -y1, x2, -y2, true);
                    break;
                }
                case 'c': {
                    double x1 = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y1 = getNextNumber(svg) + yoffset;
                    double x2 = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y2 = getNextNumber(svg) + yoffset;
                    double x = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y = getNextNumber(svg) + yoffset;
                    ph.addCbCurve(x, -y, x1, -y1, x2, -y2, false);
                    break;
                }
                case 'Q': {
                    double x1 = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y1 = getNextNumber(svg) + yoffset;
                    double x = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y = getNextNumber(svg) + yoffset;
                    ph.addQdCurve(x, -y, x1, -y1, true);
                    break;
                }
                case 'q': {
                    double x1 = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y1 = getNextNumber(svg) + yoffset;
                    double x = getNextNumber(svg) + xoffset;
                    //seekSecondCoord(svg);
                    double y = getNextNumber(svg) + yoffset;
                    ph.addQdCurve(x, -y, x1, -y1, false);
                    break;
                }
                }
            }
            }
        }
    }

    /**
     * Get the Java Color instance corresponding to an SVG string representation of that color.
     * The SVG string representation of the color can be any of the values defined in <a href="http://www.w3.org/TR/SVG11/types.html#DataTypeColor">Scalable Vector Graphics (SVG) 1.1 Specification, section 4.1: Basic data types</a>.
     *
     * @param s string representation of a color (as an SVG style attribute)
     *
     * @return null if s is null or not a syntactically well-formed color
     */
    public static Color getColor(String s) {
        if (s == null) {
            return null;
        }
        try {
            if (s.startsWith("rgb(")) {
                //color expressed as rgb(R,G,B)
                //ar should be of length 3
                StringTokenizer st = new StringTokenizer(s.substring(4, s.length() - 1), ",");
                String[] ar = new String[st.countTokens()];
                int i = 0;
                while (st.hasMoreTokens()) {
                    ar[i++] = st.nextToken();
                }
                if (ar[0].endsWith("%")) {
                    //format is (x%,y%,z%)  with x,y and z between 0 and 100
                    float r = new Float(ar[0].substring(0, ar[0].length() - 1)) / 100.0f;
                    float g = new Float(ar[1].substring(0, ar[1].length() - 1)) / 100.0f;
                    float b = new Float(ar[2].substring(0, ar[2].length() - 1)) / 100.0f;
                    return new Color(r, g, b);
                } else {
                    //format is (x,y,z)  with x,y and z between 0 and 255
                    int r = (new Float(ar[0])).intValue();
                    int g = (new Float(ar[1])).intValue();
                    int b = (new Float(ar[2])).intValue();
                    return new Color(r, g, b);
                }
            } else if (s.startsWith("#")) {
                String s2;
                if (s.length() == 4) {
                    //#FB0 is transformed into #FFBB00 according to http://www.w3.org/TR/SVG/types.html#DataTypeColor
                    s2 = s.substring(1, 2) + s.substring(1, 2) + s.substring(2, 3) + s.substring(2, 3) + s.substring(3, 4) + s.substring(3, 4);
                } else {
                    s2 = s.substring(1, s.length());
                }
                //hexadecimal base (radix 16)
                int r = Integer.parseInt(s2.substring(0, 2), 16);
                int g = Integer.parseInt(s2.substring(2, 4), 16);
                int b = Integer.parseInt(s2.substring(4, 6), 16);
                return new Color(r, g, b);
            } else if (s.startsWith("none")) {
                return null;
            } else {
                Color c;
                if ((c = Utils.getColorByKeyword(s)) != null) {
                    return c;
                } else {
                    return null;
                }
            }
        } catch (Exception ex) {
            System.err.println("Error: SVGReader.getColor(): " + ex);
            return Color.WHITE;
        }
    }

    static final short fill_INDEX = 0;
    static final short stroke_INDEX = 1;
    static final short strokewidth_INDEX = 2;
    static final short strokedasharray_INDEX = 3;
    static final short fillopacity_INDEX = 4;
    static final short fontfamily_INDEX = 5;
    static final short fontsize_INDEX = 6;
    static final short fontweight_INDEX = 7;
    static final short fontstyle_INDEX = 8;

    /**
     * Parse style information.
     *
     * @param s the value of an SVG style attribute. Supported declarations are: fill, fill-opacity, font-family, font-size, font-weight, font-style, stroke, stroke-dasharray, stroke-width
     *
     * @return styling attributes which can be interpreted by ZVTM
     */
    public static SVGStyle getStyle(String s, Element e) {
        /*
         * According to SVG 1.0 6.4 Specifying properties using the presentation attributes
         * http://www.w3.org/TR/SVG10/styling.html#UsingPresentationAttributes
         * CSS instructions in a style attribute override presentation attributes. The followig
         * code respects that by ignoring attributes of e if equivalent instructions exist in
         * the style attribute.
         */
        SVGStyle ss = new SVGStyle();
        boolean[] attributesSet = new boolean[9];
        if (s != null) {
            s = s.replaceAll(LINE_SEP, EMPTY_STRING);
            s = s.replaceAll(SPACE_SEP, EMPTY_STRING);
            StringTokenizer st = new StringTokenizer(s, STYLING_INSTRUCTION_SEP);
            // parse all CSS instructions in style attribute
            while (st.hasMoreTokens()) {
                String instr = st.nextToken();
                // FIXME (see Bug #2809312 Patch #2809313): getColor can return null, but calling
                // SVGStyle#setColor( null) causes problems
                if (instr.startsWith(_fillCSS)) {
                    ss.setFillColor(getColor(instr.substring(5, instr.length())));
                    attributesSet[fill_INDEX] = true;
                } else if (instr.startsWith(_strokeCSS)) {
                    ss.setStrokeColor(getColor(instr.substring(7, instr.length())));
                    attributesSet[stroke_INDEX] = true;
                } else if (instr.startsWith(_strokewidthCSS)) {
                    ss.setStrokeWidth(instr.substring(13, instr.length()));
                    attributesSet[strokewidth_INDEX] = true;
                } else if (instr.startsWith(_strokedasharrayCSS)) {
                    ss.setStrokeDashArray(instr.substring(17, instr.length()));
                    attributesSet[strokedasharray_INDEX] = true;
                } else if (instr.startsWith(_fillopacityCSS)) {
                    ss.setAlphaTransparencyValue(new Float(instr.substring(13, instr.length())));
                    attributesSet[fillopacity_INDEX] = true;
                } else if (instr.startsWith(_fontfamilyCSS)) {
                    ss.setFontFamily(instr.substring(12, instr.length()));
                    attributesSet[fontfamily_INDEX] = true;
                } else if (instr.startsWith(_fontsizeCSS)) {
                    ss.setFontSize(instr.substring(10, instr.length()));
                    attributesSet[fontsize_INDEX] = true;
                } else if (instr.startsWith(_fontweightCSS)) {
                    ss.setFontWeight(instr.substring(12, instr.length()));
                    attributesSet[fontweight_INDEX] = true;
                } else if (instr.startsWith(_fontstyleCSS)) {
                    ss.setFontStyle(instr.substring(11, instr.length()));
                    attributesSet[fontstyle_INDEX] = true;
                }
            }
        }
        if (e != null) {
            // look for presentation attributes for instructions not in CSS style attribute
            if (!attributesSet[fill_INDEX] && e.hasAttribute(_fill)) {
                ss.setFillColor(getColor(e.getAttribute(_fill)));
            }
            if (!attributesSet[stroke_INDEX] && e.hasAttribute(_stroke)) {
                ss.setStrokeColor(getColor(e.getAttribute(_stroke)));
            }
            if (!attributesSet[strokewidth_INDEX] && e.hasAttribute(_strokewidth)) {
                ss.setStrokeWidth(e.getAttribute(_strokewidth));
            }
            if (!attributesSet[strokedasharray_INDEX] && e.hasAttribute(_strokedasharray)) {
                ss.setStrokeDashArray(e.getAttribute(_strokedasharray));
            }
            if (!attributesSet[fillopacity_INDEX] && e.hasAttribute(_fillopacity)) {
                ss.setAlphaTransparencyValue(new Float(e.getAttribute(_fillopacity)));
            }
            if (!attributesSet[fontfamily_INDEX] && e.hasAttribute(_fontfamily)) {
                ss.setFontFamily(e.getAttribute(_fontfamily));
            }
            if (!attributesSet[fontsize_INDEX] && e.hasAttribute(_fontsize)) {
                ss.setFontSize(e.getAttribute(_fontsize));
            }
            if (!attributesSet[fontweight_INDEX] && e.hasAttribute(_fontweight)) {
                ss.setFontWeight(e.getAttribute(_fontweight));
            }
            if (!attributesSet[fontstyle_INDEX] && e.hasAttribute(_fontstyle)) {
                ss.setFontStyle(e.getAttribute(_fontstyle));
            }
        }
        return (ss.hasStylingInformation()) ? ss : null;
    }

    /**
     * Translate an SVG polygon coordinates from the SVG space to the ZVTM space, taking position offset into account.
     *
     * @param s   the SVG list of coordinates (value of attribute <i>points</i> in <i>polygon</i> elements)
     * @param res a Vector in which the result will be stored (this will be a vector of Point2D.Double)
     */
    public static void translateSVGPolygon(String s, List<Point2D.Double> res) {
        StringBuffer svg = new StringBuffer(s);
        while (svg.length() > 0) {
            Utils.delLeadingSpaces(svg);
            processNextSVGCoords(svg, res);
        }
    }

    //seek and consume next pair of numerical coordinates in a string
    private static void processNextSVGCoords(StringBuffer svg, List<Point2D.Double> res) {
        if (svg.length() > 0) {
            double x = getNextNumber(svg);

            //seekSecondCoord(svg);
            double y = getNextNumber(svg);

            if (scale != 1.0) {
                x = x * scale;
                y = y * scale;
            }

            x += xoffset;
            y += yoffset;

            res.add(new Point2D.Double(x, y));
        }
    }

    //utility method used by processNextSVGCoords()
    private static void seekSecondCoord(StringBuffer sb) {
        Utils.delLeadingSpaces(sb);
        while ((sb.length() > 0) && ((Character.isWhitespace(sb.charAt(0))) || (sb.charAt(0) == ','))) {
            sb.deleteCharAt(0);
        }
    }

    //utility method used by processNextSVGCoords()
    static double getNextNumber(StringBuffer sb) {
        double res = 0;
        seekSecondCoord(sb);
        StringBuilder dgb = new StringBuilder();
        while ((sb.length() > 0) && ((Character.isDigit(sb.charAt(0))) || (sb.charAt(0) == '-') || (sb.charAt(0) == '.') || (sb.charAt(0) == 'e') || (sb.charAt(0) == 'E') || (sb.charAt(0) == '+'))) {
            dgb.append(sb.charAt(0));
            sb.deleteCharAt(0);
        }
        if (dgb.length() > 0) {
            res = Double.parseDouble(dgb.toString());
        }
        return res;
    }

    public static void assignStroke(Glyph g, SVGStyle ss) {
        Float sw = ss.getStrokeWidth();
        if (sw == null) {
            sw = 1.0f;
        }
        float[] sda = ss.getStrokeDashArray();
        BasicStroke bs;
        if (sda != null) {
            String dashPattern = Utils.arrayOffloatAsCSStrings(sda);
            if (strokes.containsKey(sw)) {
                Map<String, Stroke> h1 = strokes.get(sw);
                if (h1.containsKey(dashPattern)) {
                    bs = (BasicStroke)h1.get(dashPattern);
                } else {
                    bs = new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                         SVGWriter.DEFAULT_MITER_LIMIT, sda,
                                         SVGWriter.DEFAULT_DASH_OFFSET);
                    h1.put(dashPattern, bs);
                }
            } else {
                bs = new BasicStroke(sw, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
                                     SVGWriter.DEFAULT_MITER_LIMIT, sda,
                                     SVGWriter.DEFAULT_DASH_OFFSET);
                Map<String, Stroke> h1 = new HashMap<String, Stroke>();
                h1.put(dashPattern, bs);
                strokes.put(sw, h1);
            }
        } else {
            if (strokes.containsKey(sw)) {
                Map<String, Stroke> h1 = strokes.get(sw);
                if (h1.containsKey(SOLID_DASH_PATTERN)) {
                    bs = (BasicStroke)h1.get(SOLID_DASH_PATTERN);
                } else {
                    bs = new BasicStroke(sw);
                    h1.put(SOLID_DASH_PATTERN, bs);
                }
            } else {
                bs = new BasicStroke(sw);
                Map<String, Stroke> h1 = new HashMap<String, Stroke>();
                h1.put(SOLID_DASH_PATTERN, bs);
                strokes.put(sw, h1);
            }
        }
        g.setStroke(bs);
    }

    /**
     * Tells whether the 4 points contained in a Vector form a rectangle or not.
     *
     * @param v a vector of 4 Point2D.Double For this to return true, points 2 &amp; 3 and points 1 &amp; 4 have to be horizontally aligned. Moreother, points 1 &amp; 2 and points 3 &amp; 4 have to be vertically aligned.
     */
    public static boolean isRectangle(List<Point2D.Double> v) {
        if (v.size() == 4 || v.size() == 5) {
            //should be 5, since the polygon has to be closed (first_point==last_point)
            Point2D.Double p1 = v.get(0);
            Point2D.Double p2 = v.get(1);
            Point2D.Double p3 = v.get(2);
            Point2D.Double p4 = v.get(3);
            if (((p2.x == p3.x) && (p1.y == p2.y) && (p3.y == p4.y) && (p1.x == p4.x)) || ((p2.y == p3.y) && (p1.x == p2.x) && (p3.x == p4.x) && (p1.y == p4.y))) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Create a VEllipse from an SVG ellipse element.
     *
     * @param e an SVG ellipse as a DOM element (org.w3c.dom.Element)
     */
    public static VEllipse createEllipse(Element e) {
        return createEllipse(e, null, false);
    }

    /**
     * Create a VEllipse from an SVG ellipse element.
     *
     * @param e   an SVG ellipse as a DOM element (org.w3c.dom.Element)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static VEllipse createEllipse(Element e, Context ctx) {
        return createEllipse(e, ctx, false);
    }

    /**
     * Create a VEllipse from an SVG ellipse element.
     *
     * @param e    an SVG ellipse as a DOM element (org.w3c.dom.Element)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static VEllipse createEllipse(Element e, Context ctx, boolean meta) {
        double x = Double.parseDouble(e.getAttribute(_cx));
        double y = Double.parseDouble(e.getAttribute(_cy));
        double w = 2 * Double.parseDouble(e.getAttribute(_rx));
        double h = 2 * Double.parseDouble(e.getAttribute(_ry));
        if (scale != 1.0) {
            x *= scale;
            y *= scale;
            w *= scale;
            h *= scale;
        }
        x += xoffset;
        y += yoffset;
        VEllipse res;
        SVGStyle ss = getStyle(e.getAttribute(_style), e);
        if (ss != null) {
            if (ss.hasTransparencyInformation()) {
                if (ss.getFillColor() == null) {
                    res = new VEllipse(x, -y, 0, w, h, Color.WHITE, Color.BLACK, ss.getAlphaTransparencyValue());
                    res.setFilled(false);
                } else {
                    res = new VEllipse(x, -y, 0, w, h, ss.getFillColor(), Color.BLACK, ss.getAlphaTransparencyValue());
                }
            } else {
                if (ss.getFillColor() == null) {
                    res = new VEllipse(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 1.0f);
                    res.setFilled(false);
                } else {
                    res = new VEllipse(x, -y, 0, w, h, ss.getFillColor(), Color.BLACK, 1.0f);
                }
            }
            Color border = ss.getStrokeColor();
            if (border != null) {
                float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
            } else if (ss.hasStrokeColorInformation()) {
                res.setDrawBorder(false);
            }
            if (ss.requiresSpecialStroke()) {
                assignStroke(res, ss);
            }
        } else if (ctx != null) {
            if (ctx.hasTransparencyInformation()) {
                if (ctx.getFillColor() == null) {
                    res = new VEllipse(x, -y, 0, w, h, Color.WHITE, Color.BLACK, ctx.getAlphaTransparencyValue());
                    res.setFilled(false);
                } else {
                    res = new VEllipse(x, -y, 0, w, h, ctx.getFillColor(), Color.BLACK, ctx.getAlphaTransparencyValue());
                }
            } else {
                if (ctx.getFillColor() == null) {
                    res = new VEllipse(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 1.0f);
                    res.setFilled(false);
                } else {
                    res = new VEllipse(x, -y, 0, w, h, ctx.getFillColor(), Color.BLACK, 1.0f);
                }
            }
            Color border = ctx.getStrokeColor();
            if (border != null) {
                float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
            }
        } else {
            res = new VEllipse(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 1.0f);
        }
        if (meta) {
            setMetadata(res, ctx);
        }
        if (e.hasAttribute(_class)) {
            res.setType(e.getAttribute(_class));
        }
        return res;
    }

    /**
     * Create a VCircle from an SVG circle element.
     *
     * @param e an SVG circle as a DOM element (org.w3c.dom.Element)
     */
    public static VCircle createCircle(Element e) {
        return createCircle(e, null, false);
    }

    /**
     * Create a VCircle from an SVG circle element.
     *
     * @param e   an SVG circle as a DOM element (org.w3c.dom.Element)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static VCircle createCircle(Element e, Context ctx) {
        return createCircle(e, ctx, false);
    }

    /**
     * Create a VCircle from an SVG circle element.
     *
     * @param e    an SVG circle as a DOM element (org.w3c.dom.Element)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static VCircle createCircle(Element e, Context ctx, boolean meta) {
        double x = Double.parseDouble(e.getAttribute(_cx));
        double y = Double.parseDouble(e.getAttribute(_cy));
        double d = 2 * Double.parseDouble(e.getAttribute(_r));
        if (scale != 1.0) {
            x *= scale;
            y *= scale;
            d *= scale;
        }
        x += xoffset;
        y += yoffset;
        VCircle res;
        SVGStyle ss = getStyle(e.getAttribute(_style), e);
        if (ss != null) {
            if (ss.hasTransparencyInformation()) {
                if (ss.getFillColor() == null) {
                    res = new VCircle(x, -y, 0, d, Color.WHITE, Color.BLACK, 1.0f);
                    res.setFilled(false);
                } else {
                    res = new VCircle(x, -y, 0, d, ss.getFillColor(), Color.BLACK, ss.getAlphaTransparencyValue());
                }
            } else {
                if (ss.getFillColor() == null) {
                    res = new VCircle(x, -y, 0, d, Color.WHITE, Color.BLACK, 1.0f);
                    res.setFilled(false);
                } else {
                    res = new VCircle(x, -y, 0, d, ss.getFillColor(), Color.BLACK, 1.0f);
                }
            }
            Color border = ss.getStrokeColor();
            if (border != null) {
                float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
            } else if (ss.hasStrokeColorInformation()) {
                res.setDrawBorder(false);
            }
            if (ss.requiresSpecialStroke()) {
                assignStroke(res, ss);
            }
        } else {
            res = new VCircle(x, -y, 0, d, Color.WHITE, Color.BLACK, 1.0f);
        }
        if (meta) {
            setMetadata(res, ctx);
        }
        if (e.hasAttribute(_class)) {
            res.setType(e.getAttribute(_class));
        }
        return res;
    }

    /**
     * Create a VText from an SVG text element.
     * Warning if text uses attribute text-anchor and has a value different from start, it will not be taken into account (it is up to you to place the text correctly, as it requires information about the View's graphicscontext to compute the string's width/height).
     *
     * @param e an SVG text as a DOM element (org.w3c.dom.Element)
     */
    public static VText createText(Element e) {
        return createText(e, null, false);
    }

    /**
     * Create a VText from an SVG text element.
     * Warning if text uses attribute text-anchor and has a value different from start, it will not be taken into account (it is up to you to place the text correctly, as it requires information about the View's graphicscontext to compute the string's width/height).
     *
     * @param e   an SVG text as a DOM element (org.w3c.dom.Element)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static VText createText(Element e, Context ctx) {
        return createText(e, ctx, false);
    }

    /**
     * Create a VText from an SVG text element.
     * Warning if text uses attribute text-anchor and has a value different from start, it will not be taken into account (it is up to you to place the text correctly, as it requires information about the View's graphicscontext to compute the string's width/height).
     *
     * @param e    an SVG text as a DOM element (org.w3c.dom.Element)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static VText createText(Element e, Context ctx, boolean meta) {
        String tx = (e.getFirstChild() == null) ? "" : e.getFirstChild().getNodeValue();
        double x = Double.parseDouble(e.getAttribute(_x));
        double y = Double.parseDouble(e.getAttribute(_y));
        if (scale != 1.0) {
            x *= scale;
            y *= scale;
        }
        x += xoffset;
        y += yoffset;
        VText res;
        short ta = VText.TEXT_ANCHOR_START;
        if (e.hasAttribute(_textanchor)) {
            String tas = e.getAttribute(_textanchor);
            if (tas.equals(_middle)) {
                ta = VText.TEXT_ANCHOR_MIDDLE;
            } else if (tas.equals(_end)) {
                ta = VText.TEXT_ANCHOR_END;
            } else if (tas.equals(_inherit)) {
                System.err.println("SVGReader::'inherit' value for text-anchor attribute not supported yet");
            }
        }
        Color tc = Color.BLACK;
        SVGStyle ss = ss = getStyle(e.getAttribute(_style), e);
        if (ss != null) {
            if (ss.getStrokeColor() == null) {
                if (ss.getFillColor() != null) {
                    tc = ss.getFillColor();
                }
            } else {
                tc = ss.getStrokeColor();
            }
            if (ss.hasTransparencyInformation()) {
                res = new VText(x, -y, 0, tc, tx, ta, 1f, ss.getAlphaTransparencyValue());
            } else {
                res = new VText(x, -y, 0, tc, tx, ta, 1f, 1f);
            }
            Font f;
            if (specialFont(f = ss.getDefinedFont(ctx), VText.getMainFont())) {
                res.setFont(f);
            }
        } else if (ctx != null) {
            if (ctx.hasTransparencyInformation()) {
                res = new VText(x, -y, 0, tc, tx, ta, 1f, ctx.getAlphaTransparencyValue());
            } else {
                res = new VText(x, -y, 0, tc, tx, ta, 1f, 1f);
            }
            Font f;
            if (specialFont(f = ctx.getDefinedFont(), VText.getMainFont())) {
                res.setFont(f);
            }
        } else {
            res = new VText(x, -y, 0, tc, tx, ta, 1f, 1f);
        }

        if (meta) {
            setMetadata(res, ctx);
        }
        if (e.hasAttribute(_class)) {
            res.setType(e.getAttribute(_class));
        }
        return res;
    }

    /**
     * Create a VRectangle from an SVG polygon element.
     * After checking this is actually a rectangle - returns null if not.
     *
     * @param e an SVG polygon as a DOM element (org.w3c.dom.Element)
     */
    public static VRectangleOr createRectangleFromPolygon(Element e) {
        return createRectangleFromPolygon(e, null, false);
    }

    /**
     * Create a VRectangle from an SVG polygon element.
     * After checking this is actually a rectangle - returns null if not.
     *
     * @param e   an SVG polygon as a DOM element (org.w3c.dom.Element)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static VRectangleOr createRectangleFromPolygon(Element e, Context ctx) {
        return createRectangleFromPolygon(e, ctx, false);
    }

    /**
     * Create a VRectangle from an SVG polygon element.
     * After checking this is actually a rectangle - returns null if not.
     *
     * @param e    an SVG polygon as a DOM element (org.w3c.dom.Element)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static VRectangleOr createRectangleFromPolygon(Element e, Context ctx, boolean meta) {
        List<Point2D.Double> coords = new ArrayList<Point2D.Double>();
        translateSVGPolygon(e.getAttribute(_points), coords);
        if (isRectangle(coords)) {
            Point2D.Double p1 = coords.get(0);
            Point2D.Double p2 = coords.get(1);
            Point2D.Double p3 = coords.get(2);
            Point2D.Double p4 = coords.get(3);
            //the ordering of points may vary, so we must identify what point stands for what corner
            Point2D.Double pNW, pNE, pSE, pSW;
            double l = Math.min(p1.x, Math.min(p2.x, Math.min(p3.x, p4.x)));
            double u = Math.max(p1.y, Math.max(p2.y, Math.max(p3.y, p4.y)));
            double r = Math.max(p1.x, Math.max(p2.x, Math.max(p3.x, p4.x)));
            double d = Math.min(p1.y, Math.min(p2.y, Math.min(p3.y, p4.y)));
            pNW = new Point2D.Double(l, u);
            pNE = new Point2D.Double(r, u);
            pSE = new Point2D.Double(r, d);
            pSW = new Point2D.Double(l, d);
            double h = Math.abs(pSE.y - pNE.y);
            double w = Math.abs(pNW.x - pNE.x);
            double x = pNE.x - w / 2d;
            double y = pNE.y - h / 2d;
            VRectangleOr res;
            SVGStyle ss = ss = getStyle(e.getAttribute(_style), e);
            if (ss != null) {
                if (ss.hasTransparencyInformation()) {
                    if (ss.getFillColor() == null) {
                        res = new VRectangleOr(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 0, ss.getAlphaTransparencyValue());
                        res.setFilled(false);
                    } else {
                        res = new VRectangleOr(x, -y, 0, w, h, ss.getFillColor(), Color.BLACK, 0, ss.getAlphaTransparencyValue());
                    }
                } else {
                    if (ss.getFillColor() == null) {
                        res = new VRectangleOr(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 0, 1.0f);
                        res.setFilled(false);
                    } else {
                        res = new VRectangleOr(x, -y, 0, w, h, ss.getFillColor(), Color.BLACK, 0, 1.0f);
                    }
                }
                Color border = ss.getStrokeColor();
                if (border != null) {
                    float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                    res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
                } else if (ss.hasStrokeColorInformation()) {
                    res.setDrawBorder(false);
                }
                if (ss.requiresSpecialStroke()) {
                    assignStroke(res, ss);
                }
            } else if (ctx != null) {
                if (ctx.hasTransparencyInformation()) {
                    if (ctx.getFillColor() == null) {
                        res = new VRectangleOr(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 0, ctx.getAlphaTransparencyValue());
                        res.setFilled(false);
                    } else {
                        res = new VRectangleOr(x, -y, 0, w, h, ctx.getFillColor(), Color.BLACK, 0, ctx.getAlphaTransparencyValue());
                    }
                } else {
                    if (ctx.getFillColor() == null) {
                        res = new VRectangleOr(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 0, 1.0f);
                        res.setFilled(false);
                    } else {
                        res = new VRectangleOr(x, -y, 0, w, h, ctx.getFillColor(), Color.BLACK, 0, 1.0f);
                    }
                }
                Color border = ctx.getStrokeColor();
                if (border != null) {
                    float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                    res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
                } else if (ctx.hasStrokeColorInformation()) {
                    res.setDrawBorder(false);
                }
            } else {
                res = new VRectangleOr(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 0, 1.0f);
            }
            if (meta) {
                setMetadata(res, ctx);
            }
            if (e.hasAttribute(_class)) {
                res.setType(e.getAttribute(_class));
            }
            return res;
        } else {
            return null;
        }
    }

    /**
     * Create a VRoundRect from an SVG polygon element.
     * After checking this is actually a rectangle - returns null if not.
     *
     * @param e an SVG polygon as a DOM element (org.w3c.dom.Element)
     */
    public static VRoundRect createRoundRectFromPolygon(Element e) {
        return createRoundRectFromPolygon(e, null, false);
    }

    /**
     * Create a VRoundRect from an SVG polygon element.
     * After checking this is actually a rectangle - returns null if not.
     *
     * @param e   an SVG polygon as a DOM element (org.w3c.dom.Element)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static VRoundRect createRoundRectFromPolygon(Element e, Context ctx) {
        return createRoundRectFromPolygon(e, ctx, false);
    }

    /**
     * Create a VRoundRect from an SVG polygon element.
     * After checking this is actually a rectangle - returns null if not.
     *
     * @param e    an SVG polygon as a DOM element (org.w3c.dom.Element)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static VRoundRect createRoundRectFromPolygon(Element e, Context ctx, boolean meta) {
        List<Point2D.Double> coords = new ArrayList<Point2D.Double>();
        translateSVGPolygon(e.getAttribute(_points), coords);
        if (isRectangle(coords)) {
            Point2D.Double p1 = coords.get(0);
            Point2D.Double p2 = coords.get(1);
            Point2D.Double p3 = coords.get(2);
            Point2D.Double p4 = coords.get(3);
            //the ordering of points may vary, so we must identify what point stands for what corner
            Point2D.Double pNW, pNE, pSE, pSW;
            double l = Math.min(p1.x, Math.min(p2.x, Math.min(p3.x, p4.x)));
            double u = Math.max(p1.y, Math.max(p2.y, Math.max(p3.y, p4.y)));
            double r = Math.max(p1.x, Math.max(p2.x, Math.max(p3.x, p4.x)));
            double d = Math.min(p1.y, Math.min(p2.y, Math.min(p3.y, p4.y)));
            pNW = new Point2D.Double(l, u);
            pNE = new Point2D.Double(r, u);
            pSE = new Point2D.Double(r, d);
            pSW = new Point2D.Double(l, d);
            double h = Math.abs(pSE.y - pNE.y);
            double w = Math.abs(pNW.x - pNE.x);
            double x = pNE.x - w / 2d;
            double y = pNE.y - h / 2d;
            VRoundRect res;
            SVGStyle ss = ss = getStyle(e.getAttribute(_style), e);
            if (ss != null) {
                if (ss.hasTransparencyInformation()) {
                    if (ss.getFillColor() == null) {
                        res = new VRoundRect(x, -y, 0, w, h, Color.WHITE, Color.BLACK, ss.getAlphaTransparencyValue(), Math.round(RRARCR * Math.min(w, h)), Math.round(RRARCR * Math.min(w, h)));
                        res.setFilled(false);
                    } else {
                        res = new VRoundRect(x, -y, 0, w, h, ss.getFillColor(), Color.BLACK, ss.getAlphaTransparencyValue(), Math.round(RRARCR * Math.min(w, h)), Math.round(RRARCR * Math.min(w, h)));
                    }
                } else {
                    if (ss.getFillColor() == null) {
                        res = new VRoundRect(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 1.0f, Math.round(RRARCR * Math.min(w, h)), Math.round(RRARCR * Math.min(w, h)));
                        res.setFilled(false);
                    } else {
                        res = new VRoundRect(x, -y, 0, w, h, ss.getFillColor(), Color.BLACK, 1.0f, Math.round(RRARCR * Math.min(w, h)), Math.round(RRARCR * Math.min(w, h)));
                    }
                }
                Color border = ss.getStrokeColor();
                if (border != null) {
                    float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                    res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
                } else if (ss.hasStrokeColorInformation()) {
                    res.setDrawBorder(false);
                }
                if (ss.requiresSpecialStroke()) {
                    assignStroke(res, ss);
                }
            } else if (ctx != null) {
                if (ctx.hasTransparencyInformation()) {
                    if (ctx.getFillColor() == null) {
                        res = new VRoundRect(x, -y, 0, w, h, Color.WHITE, Color.BLACK, ctx.getAlphaTransparencyValue(), Math.round(RRARCR * Math.min(w, h)), Math.round(RRARCR * Math.min(w, h)));
                        res.setFilled(false);
                    } else {
                        res = new VRoundRect(x, -y, 0, w, h, ctx.getFillColor(), Color.BLACK, ctx.getAlphaTransparencyValue(), Math.round(RRARCR * Math.min(w, h)), Math.round(RRARCR * Math.min(w, h)));
                    }
                } else {
                    if (ctx.getFillColor() == null) {
                        res = new VRoundRect(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 1.0f, Math.round(RRARCR * Math.min(w, h)), Math.round(RRARCR * Math.min(w, h)));
                        res.setFilled(false);
                    } else {
                        res = new VRoundRect(x, -y, 0, w, h, ctx.getFillColor(), Color.BLACK, 1.0f, Math.round(RRARCR * Math.min(w, h)), Math.round(RRARCR * Math.min(w, h)));
                    }
                }
                Color border = ctx.getStrokeColor();
                if (border != null) {
                    float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                    res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
                } else if (ctx.hasStrokeColorInformation()) {
                    res.setDrawBorder(false);
                }
            } else {
                res = new VRoundRect(x, -y, 0, w, h, Color.WHITE, Color.BLACK, 1.0f, Math.round(RRARCR * Math.min(w, h)), Math.round(RRARCR * Math.min(w, h)));
            }
            if (meta) {
                setMetadata(res, ctx);
            }
            if (e.hasAttribute(_class)) {
                res.setType(e.getAttribute(_class));
            }
            return res;
        } else {
            return null;
        }
    }

    /**
     * Create a VRectangle from an SVG rect element.
     *
     * @param e an SVG rect(angle) as a DOM element (org.w3c.dom.Element)
     */
    public static VRectangleOr createRectangle(Element e) {
        return createRectangle(e, null, false);
    }

    /**
     * Create a VRectangle from an SVG rect element.
     *
     * @param e   an SVG rect(angle) as a DOM element (org.w3c.dom.Element)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static VRectangleOr createRectangle(Element e, Context ctx) {
        return createRectangle(e, ctx, false);
    }

    /**
     * Create a VRectangle from an SVG rect element.
     *
     * @param e    an SVG rect(angle) as a DOM element (org.w3c.dom.Element)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static VRectangleOr createRectangle(Element e, Context ctx, boolean meta) {
        double x = Double.parseDouble(e.getAttribute(_x));
        double y = Double.parseDouble(e.getAttribute(_y));
        double w = Double.parseDouble(e.getAttribute(_width));
        double h = Double.parseDouble(e.getAttribute(_height));
        if (scale != 1.0) {
            x *= scale;
            y *= scale;
            w *= scale;
            h *= scale;
        }
        x += xoffset;
        y += yoffset;
        VRectangleOr res;
        SVGStyle ss = ss = getStyle(e.getAttribute(_style), e);
        if (ss != null) {
            if (ss.hasTransparencyInformation()) {
                if (ss.getFillColor() == null) {
                    res = new VRectangleOr(x + w, -y - h, 0, w, h, Color.WHITE, Color.BLACK, 0, ss.getAlphaTransparencyValue());
                    res.setFilled(false);
                } else {
                    res = new VRectangleOr(x + w, -y - h, 0, w, h, ss.getFillColor(), Color.BLACK, 0, ss.getAlphaTransparencyValue());
                }
            } else {
                if (ss.getFillColor() == null) {
                    res = new VRectangleOr(x + w, -y - h, 0, w, h, Color.WHITE, Color.BLACK, 0, 1.0f);
                    res.setFilled(false);
                } else {
                    res = new VRectangleOr(x + w, -y - h, 0, w, h, ss.getFillColor(), Color.BLACK, 0, 1.0f);
                }
            }
            Color border = ss.getStrokeColor();
            if (border != null) {
                float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
            } else if (ss.hasStrokeColorInformation()) {
                res.setDrawBorder(false);
            }
            if (ss.requiresSpecialStroke()) {
                assignStroke(res, ss);
            }
        } else if (ctx != null) {
            if (ctx.hasTransparencyInformation()) {
                if (ctx.getFillColor() == null) {
                    res = new VRectangleOr(x + w, -y - h, 0, w, h, Color.WHITE, Color.BLACK, 0, ctx.getAlphaTransparencyValue());
                    res.setFilled(false);
                } else {
                    res = new VRectangleOr(x + w, -y - h, 0, w, h, ctx.getFillColor(), Color.BLACK, 0, ctx.getAlphaTransparencyValue());
                }
            } else {
                if (ctx.getFillColor() == null) {
                    res = new VRectangleOr(x + w, -y - h, 0, w, h, Color.WHITE, Color.BLACK, 0, 1.0f);
                    res.setFilled(false);
                } else {
                    res = new VRectangleOr(x + w, -y - h, 0, w, h, ctx.getFillColor(), Color.BLACK, 0, 1.0f);
                }
            }
            Color border = ctx.getStrokeColor();
            if (border != null) {
                float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
            } else if (ctx.hasStrokeColorInformation()) {
                res.setDrawBorder(false);
            }
        } else {
            res = new VRectangleOr(x + w, -y - h, 0, w, h, Color.WHITE, Color.BLACK, 0, 1.0f);
        }
        if (meta) {
            setMetadata(res, ctx);
        }
        if (e.hasAttribute(_class)) {
            res.setType(e.getAttribute(_class));
        }
        return res;
    }

    /**
     * Create a VImage from an SVG image element.
     * This is a convenience method. An invocation of the form createImage(e, ctx, meta, imageStore, documentParentURL) behaves in exactly the same way as the invocation createImage(e, ctx, meta, imageStore, documentParentURL, null).
     *
     * @param e                 an SVG image as a DOM element (org.w3c.dom.Element)
     * @param ctx               used to propagate contextual style information (put null if none)
     * @param meta              store metadata associated with this node (URL, title) in glyph's associated object
     * @param imageStore        a simple hashtable (possibly empty at first) in which bitmap images will be stored so that they
     * do not get loaded in memory multiple times.
     * @param documentParentURL the URL of the parent directory containing the SVG/XML document. Provide an empty String if it is not know.
     * This may however cause problems when retrieving bitmap images associated with this SVG document, unless there URL
     * is expressed relative to the document's location.
     *
     * @see #createImage(Element e, Context ctx, boolean meta, Map imageStore, String documentParentURL, String fallbackParentURL)
     */
    public static Glyph createImage(Element e, Context ctx, boolean meta, Map<URL, ImageIcon> imageStore, String documentParentURL) {
        return SVGReader.createImage(e, ctx, meta, imageStore, documentParentURL, null);
    }

    /**
     * Create a VImage from an SVG image element.
     *
     * @param e                 an SVG image as a DOM element (org.w3c.dom.Element)
     * @param ctx               used to propagate contextual style information (put null if none)
     * @param meta              store metadata associated with this node (URL, title) in glyph's associated object
     * @param imageStore        a simple hashtable (possibly empty at first) in which bitmap images will be stored so that they
     * do not get loaded in memory multiple times.
     * @param documentParentURL the URL of the parent directory containing the SVG/XML document. Provide an empty String if it is not know.
     * This may however cause problems when retrieving bitmap images associated with this SVG document, unless there URL
     * is expressed relative to the document's location.
     * @param fallbackParentURL used to indicate a possible fallback directory from which to interpret relative paths in case documentParentURL
     * is not the right place where to look for those images (this can happen e.g. if a file was generated somewhere and then moved alone,
     * associated images staying in the original directory) ; set to null if no fallback directory is known.
     *
     * @see #createImage(Element e, Context ctx, boolean meta, Map imageStore, String documentParentURL)
     */
    public static VImage createImage(Element e, Context ctx, boolean meta, Map<URL, ImageIcon> imageStore, String documentParentURL, String fallbackParentURL) {
        double x = Double.parseDouble(e.getAttribute(_x)) + xoffset;
        double y = Double.parseDouble(e.getAttribute(_y)) + yoffset;
        String width = e.getAttribute(_width);
        // remove "px" from width and height
        if (width.endsWith("px")) {
            width = width.substring(0, width.length() - 2);
        }
        String height = e.getAttribute(_height);
        if (height.endsWith("px")) {
            height = height.substring(0, height.length() - 2);
        }
        double w = Double.parseDouble(width);
        double h = Double.parseDouble(height);
        double hw = w / 2;
        double hh = h / 2;
        VImage res = null;
        if (e.hasAttributeNS(xlinkURI, _href)) {
            String imagePath = e.getAttributeNS(xlinkURI, _href);
            if (imagePath.length() > 0) {
                ImageIcon ii = getImage(imagePath, documentParentURL, fallbackParentURL, imageStore);
                if (ii != null) {
                    // icon-specified dimensions
                    int aw = ii.getIconWidth();
                    int ah = ii.getIconHeight();
                    // width and height ratios
                    double wr = w / ((double)aw);
                    double hr = h / ((double)ah);
                    if (wr != 1.0 || hr != 1.0) {
                        res = new VImage(x + hw, -y - hh, 0, ii.getImage(), Math.min(wr, hr), 1.0f);
                    } else {
                        res = new VImage(x + hw, -y - hh, 0, ii.getImage(), 1.0, 1.0f);
                    }
                }
            }
        }
        if (res != null) {
            if (e.hasAttribute(_class)) {
                res.setType(e.getAttribute(_class));
            }
            if (meta) {
                setMetadata(res, ctx);
            }
        }
        return res;
    }

    /**
     * Create a VPolygon from an SVG polygon element.
     *
     * @param e an SVG polygon as a DOM element (org.w3c.dom.Element)
     */
    public static VPolygon createPolygon(Element e) {
        return createPolygon(e, null, false);
    }

    /**
     * Create a VPolygon from an SVG polygon element.
     *
     * @param e   an SVG polygon as a DOM element (org.w3c.dom.Element)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static VPolygon createPolygon(Element e, Context ctx) {
        return createPolygon(e, ctx, false);
    }

    /**
     * Create a VPolygon from an SVG polygon element.
     *
     * @param e    an SVG polygon as a DOM element (org.w3c.dom.Element)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static VPolygon createPolygon(Element e, Context ctx, boolean meta) {
        List<Point2D.Double> coords = new ArrayList<Point2D.Double>();
        translateSVGPolygon(e.getAttribute(_points), coords);
        Point2D.Double[] coords2 = new Point2D.Double[coords.size()];
        Point2D.Double lp;
        for (int i = 0; i < coords2.length; i++) {
            lp = coords.get(i);
            coords2[i] = new Point2D.Double(lp.x, -lp.y);
        }
        VPolygon res;
        SVGStyle ss = ss = getStyle(e.getAttribute(_style), e);
        if (ss != null) {
            if (ss.hasTransparencyInformation()) {
                if (ss.getFillColor() == null) {
                    res = new VPolygon(coords2, 0, Color.WHITE, Color.BLACK, ss.getAlphaTransparencyValue());
                    res.setFilled(false);
                } else {
                    res = new VPolygon(coords2, 0, ss.getFillColor(), Color.BLACK, ss.getAlphaTransparencyValue());
                }
            } else {
                if (ss.getFillColor() == null) {
                    res = new VPolygon(coords2, 0, Color.WHITE, Color.BLACK, 1.0f);
                    res.setFilled(false);
                } else {
                    res = new VPolygon(coords2, 0, ss.getFillColor(), Color.BLACK, 1.0f);
                }
            }
            Color border = ss.getStrokeColor();
            if (border != null) {
                float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
            } else if (ss.hasStrokeColorInformation()) {
                res.setDrawBorder(false);
            }
            if (ss.requiresSpecialStroke()) {
                assignStroke(res, ss);
            }
        } else if (ctx != null) {
            if (ctx.hasTransparencyInformation()) {
                if (ctx.getFillColor() == null) {
                    res = new VPolygon(coords2, 0, Color.WHITE, Color.BLACK, ctx.getAlphaTransparencyValue());
                    res.setFilled(false);
                } else {
                    res = new VPolygon(coords2, 0, ctx.getFillColor(), Color.BLACK, ctx.getAlphaTransparencyValue());
                }
            } else {
                if (ctx.getFillColor() == null) {
                    res = new VPolygon(coords2, 0, Color.WHITE, Color.BLACK, 1.0f);
                    res.setFilled(false);
                } else {
                    res = new VPolygon(coords2, 0, ctx.getFillColor(), Color.BLACK, 1.0f);
                }
            }
            Color border = ctx.getStrokeColor();
            if (border != null) {
                float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                res.setHSVbColor(hsv[0], hsv[1], hsv[2]);
            } else if (ctx.hasStrokeColorInformation()) {
                res.setDrawBorder(false);
            }
        } else {
            res = new VPolygon(coords2, 0, Color.WHITE, Color.BLACK, 1.0f);
        }
        if (meta) {
            setMetadata(res, ctx);
        }
        if (e.hasAttribute(_class)) {
            res.setType(e.getAttribute(_class));
        }
        return res;
    }

    /**
     * Create a set of VSegments from an SVG polyline element.
     *
     * @param e an SVG polyline as a DOM element (org.w3c.dom.Element)
     */
    public static VSegment[] createPolyline(Element e) {
        return createPolyline(e, null, false);
    }

    /**
     * Create a set of VSegments from an SVG polyline element.
     *
     * @param e   an SVG polyline as a DOM element (org.w3c.dom.Element)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static VSegment[] createPolyline(Element e, Context ctx) {
        return createPolyline(e, ctx, false);
    }

    /**
     * Create a set of VSegments from an SVG polyline element.
     *
     * @param e    an SVG polyline as a DOM element (org.w3c.dom.Element)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static VSegment[] createPolyline(Element e, Context ctx, boolean meta) {
        List<Point2D.Double> coords = new ArrayList<Point2D.Double>();
        translateSVGPolygon(e.getAttribute(_points), coords);
        VSegment[] res = new VSegment[coords.size() - 1];
        Color border = Color.black;
        SVGStyle ss = ss = getStyle(e.getAttribute(_style), e);
        if (ss != null) {
            border = ss.getStrokeColor();
            if (border == null) {
                border = (ss.hasStrokeColorInformation()) ? Color.WHITE : Color.BLACK;
            }
        } else if (ctx != null) {
            if (ctx.getStrokeColor() != null) {
                border = ctx.getStrokeColor();
            } else {
                border = (ctx.hasStrokeColorInformation()) ? Color.WHITE : Color.BLACK;
            }
        }
        Point2D.Double lp1, lp2;
        for (int i = 0; i < coords.size() - 1; i++) {
            lp1 = coords.get(i);
            lp2 = coords.get(i + 1);
            res[i] = new VSegment(lp1.x, -lp1.y, lp2.x, -lp2.y, 0, border, 1f);
            if (ss != null && ss.requiresSpecialStroke()) {
                assignStroke(res[i], ss);
            }
            if (meta) {
                setMetadata(res[i], ctx);
            }
            if (e.hasAttribute(_class)) {
                res[i].setType(e.getAttribute(_class));
            }
        }
        return res;
    }

    /**
     * Create a VSegment from an SVG line element.
     *
     * @param e an SVG polyline as a DOM element (org.w3c.dom.Element)
     */
    public static VSegment createLine(Element e) {
        return createLine(e, null, false);
    }

    /**
     * Create a VSegment from an SVG line element.
     *
     * @param e   an SVG polyline as a DOM element (org.w3c.dom.Element)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static VSegment createLine(Element e, Context ctx) {
        return createLine(e, ctx, false);
    }

    /**
     * Create a VSegment from an SVG line element.
     *
     * @param e    an SVG line as a DOM element (org.w3c.dom.Element)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static VSegment createLine(Element e, Context ctx, boolean meta) {
        VSegment res;
        double x1 = Double.parseDouble(e.getAttribute(_x1));
        double y1 = Double.parseDouble(e.getAttribute(_y1));
        double x2 = Double.parseDouble(e.getAttribute(_x2));
        double y2 = Double.parseDouble(e.getAttribute(_y2));
        if (scale != 1.0) {
            x1 *= scale;
            y1 *= scale;
            x2 *= scale;
            y2 *= scale;
        }
        x1 += xoffset;
        y1 += yoffset;
        x2 += xoffset;
        y2 += yoffset;
        Color border = Color.black;
        SVGStyle ss = ss = getStyle(e.getAttribute(_style), e);
        if (ss != null) {
            border = ss.getStrokeColor();
            if (border == null) {
                border = (ss.hasStrokeColorInformation()) ? Color.WHITE : Color.BLACK;
            }
            if (ss.hasTransparencyInformation()) {
                res = new VSegment(x1, -y1, x2, -y2, 0, border, ss.getAlphaTransparencyValue());
            } else {
                res = new VSegment(x1, -y1, x2, -y2, 0, border, 1f);
            }
        } else if (ctx != null) {
            if (ctx.getStrokeColor() != null) {
                border = ctx.getStrokeColor();
            } else {
                border = (ctx.hasStrokeColorInformation()) ? Color.WHITE : Color.BLACK;
            }
            if (ctx.hasTransparencyInformation()) {
                res = new VSegment(x1, -y1, x2, -y2, 0, border, ctx.getAlphaTransparencyValue());
            } else {
                res = new VSegment(x1, -y1, x2, -y2, 0, border, 1f);
            }
        } else {
            res = new VSegment(x1, -y1, x2, -y2, 0, border, 1f);
        }
        if (ss != null && ss.requiresSpecialStroke()) {
            assignStroke(res, ss);
        }
        if (meta) {
            setMetadata(res, ctx);
        }
        if (e.hasAttribute(_class)) {
            res.setType(e.getAttribute(_class));
        }
        return res;
    }

    /**
     * Create a DPath from an SVG text element.
     *
     * @param e  an SVG path as a DOM element (org.w3c.dom.Element)
     * @param ph a DPath that is going to be modified to match the coordinates provided as first argument (you can simply use <i>new DPath()</i>)
     */
    public static DPath createPath(Element e, DPath ph) {
        return createPath(e, ph, null, false);
    }

    /**
     * Create a DPath from an SVG text element.
     *
     * @param e   an SVG path as a DOM element (org.w3c.dom.Element)
     * @param ph  a DPath that is going to be modified to match the coordinates provided as first argument (you can simply use <i>new DPath()</i>)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static DPath createPath(Element e, DPath ph, Context ctx) {
        return createPath(e, ph, ctx, false);
    }

    /**
     * Create a DPath from an SVG text element.
     *
     * @param e    an SVG path as a DOM element (org.w3c.dom.Element)
     * @param ph   a DPath that is going to be modified to match the coordinates provided as first argument (you can simply use <i>new DPath()</i>)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static DPath createPath(Element e, DPath ph, Context ctx, boolean meta) {
        StringBuffer svg = new StringBuffer(e.getAttribute(_d));
        if (checkSVGPath(svg.toString())) {
            StringBuffer lastCommand = new StringBuffer("M");
            while (svg.length() > 0) {
                Utils.delLeadingSpaces(svg);
                processNextSVGPathCommand(svg, ph, lastCommand);
            }
            SVGStyle ss = getStyle(e.getAttribute(_style), e);
            if (ss != null && ss.hasStrokeColorInformation()) {
                Color border = ss.getStrokeColor();
                float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                ph.setHSVColor(hsv[0], hsv[1], hsv[2]);
                if (ss.requiresSpecialStroke()) {
                    assignStroke(ph, ss);
                }
            } else if (ctx != null) {
                Color border = ctx.getStrokeColor();
                if (border != null) {
                    float[] hsv = Color.RGBtoHSB(border.getRed(), border.getGreen(), border.getBlue(), new float[3]);
                    ph.setHSVColor(hsv[0], hsv[1], hsv[2]);
                }
            }
            if (meta) {
                setMetadata(ph, ctx);
            }
            if (e.hasAttribute(_class)) {
                ph.setType(e.getAttribute(_class));
            }
            return ph;
        } else {
            return null;
        }
    }

    /**
     * Create a DPath from an SVG text element.
     *
     * @param d  the <i>d</i> attribute value of an SVG path
     * @param ph a DPath that is going to be modified to match the coordinates provided as first argument (you can just use <i>new DPath()</i>)
     */
    public static DPath createPath(String d, DPath ph) {
        return createPath(d, ph, null, false);
    }

    /**
     * Create a DPath from an SVG text element.
     *
     * @param d   the <i>d</i> attribute value of an SVG path
     * @param ph  a DPath that is going to be modified to match the coordinates provided as first argument (you can just use <i>new DPath()</i>)
     * @param ctx used to propagate contextual style information (put null if none)
     */
    public static DPath createPath(String d, DPath ph, Context ctx) {
        return createPath(d, ph, ctx, false);
    }

    /**
     * Create a DPath from an SVG text element.
     *
     * @param d    the <i>d</i> attribute value of an SVG path
     * @param ph   a DPath that is going to be modified to match the coordinates provided as first argument (you can just use <i>new DPath()</i>)
     * @param ctx  used to propagate contextual style information (put null if none)
     * @param meta store metadata associated with this node (URL, title) in glyph's associated object
     */
    public static DPath createPath(String d, DPath ph, Context ctx, boolean meta) {
        StringBuffer svg = new StringBuffer(d);
        if (checkSVGPath(svg.toString())) {
            StringBuffer lastCommand = new StringBuffer("M");
            while (svg.length() > 0) {
                Utils.delLeadingSpaces(svg);
                processNextSVGPathCommand(svg, ph, lastCommand);
            }
            if (meta) {
                setMetadata(ph, ctx);
            }
            return ph;
        } else {
            return null;
        }
    }

    /**
     * Load a DOM-parsed SVG document d in VirtualSpace vs.
     * This is a convenience method. An invocation of the form load(d, vs, meta, documentURL) behaves in exactly the same way as the invocation load(d, vs, meta, documentURL, null).
     *
     * @param d           SVG document as a DOM tree
     * @param vs          virtual space
     * @param meta        store metadata associated with graphical elements (URL, title) in each Glyph's associated object
     * @param documentURL the URL where the SVG/XML document was found. Provide an empty String if it is not know.
     * This may however cause problems when retrieving bitmap images associated with this SVG document, unless there URL
     * is expressed relative to the document's location.
     *
     * @see #load(Document d, VirtualSpace vs, boolean meta, String documentURL, String fallbackParentURL)
     */
    public static void load(Document d, VirtualSpace vs, boolean meta, String documentURL) {
        SVGReader.load(d, vs, meta, documentURL, null);
    }

    /**
     * Load a DOM-parsed SVG document d in VirtualSpace vs.
     *
     * @param d                 SVG document as a DOM tree
     * @param vs                virtual space
     * @param meta              store metadata associated with graphical elements (URL, title) in each Glyph's associated object
     * @param documentURL       the URL where the SVG/XML document was found. Provide an empty String if it is not know.
     * This may however cause problems when retrieving bitmap images associated with this SVG document, unless there URL
     * is expressed relative to the document's location.
     * @param fallbackParentURL used to indicate a possible fallback directory from which to interpret relative paths in case the parent of
     * documentURL is not the right place where to look for those images (this can happen e.g. if a file was generated somewhere and then
     * moved alone, associated images staying in the original directory) ; set to null if no fallback directory is known.
     *
     * @see #load(Document d, VirtualSpace vs, boolean meta, String documentURL)
     */
    public static void load(Document d, VirtualSpace vs, boolean meta, String documentURL, String fallbackParentURL) {
        // The following way of retrieving the Document's URL is disabled because it requires Java 1.5/DOM Level 3 support
        String documentParentURL = documentURL.substring(0, documentURL.lastIndexOf("/") + 1);
        Element svgRoot = d.getDocumentElement();
        NodeList objects = svgRoot.getChildNodes();
        Map<URL, ImageIcon> imageStore = new HashMap<URL, ImageIcon>();
        for (int i = 0; i < objects.getLength(); i++) {
            Node obj = objects.item(i);
            if (obj.getNodeType() == Node.ELEMENT_NODE) {
                processNode((Element)obj, vs, null, false, meta, documentParentURL, fallbackParentURL, imageStore);
            }
        }
    }

    /*
     * e is a DOM element, vs is the name of the virtual space where the new glyph(s) is(are) put
     */
    private static void processNode(Element e, VirtualSpace vs,
                                    Context ctx, boolean mainFontSet, boolean meta,
                                    String documentParentURL, String fallbackParentURL,
                                    Map<URL, ImageIcon> imageStore) {
        String tagName = e.getTagName();
        if (tagName.equals(_rect)) {
            vs.addGlyph(createRectangle(e, ctx, meta));
        } else if (tagName.equals(_ellipse)) {
            vs.addGlyph(createEllipse(e, ctx, meta));
        } else if (tagName.equals(_circle)) {
            vs.addGlyph(createCircle(e, ctx, meta));
        } else if (tagName.equals(_path)) {
            vs.addGlyph(createPath(e, new DPath(), ctx, meta));
        } else if (tagName.equals(_text)) {
            vs.addGlyph(createText(e, ctx, meta));
        } else if (tagName.equals(_polygon)) {
            Glyph g = createRectangleFromPolygon(e, ctx, meta);
            //if e does not describe a rectangle
            if (g != null) {
                vs.addGlyph(g);
            } //create a VPolygon
            else {
                vs.addGlyph(createPolygon(e, ctx, meta));
            }
        } else if (tagName.equals(_polyline)) {
            Glyph[] segments = createPolyline(e, ctx, meta);
            for (int i = 0; i < segments.length; i++) {
                vs.addGlyph(segments[i]);
            }
        } else if (tagName.equals(_line)) {
            vs.addGlyph(createLine(e, ctx, meta));
        } else if (tagName.equals(_image)) {
            if (isSVGImage(e)) {
                String imagePath = e.getAttributeNS(xlinkURI, _href);
                URL imageURL = getImageURL(imagePath, documentParentURL, fallbackParentURL);
                String width = e.getAttribute(_width);
                // remove "px" from width and height
                if (width.endsWith("px")) {
                    width = width.substring(0, width.length() - 2);
                }
                String height = e.getAttribute(_height);
                if (height.endsWith("px")) {
                    height = height.substring(0, height.length() - 2);
                }
                double w = Double.parseDouble(width);
                double h = Double.parseDouble(height);
                double x = Double.parseDouble(e.getAttribute(_x));
                double y = Double.parseDouble(e.getAttribute(_y));
                double xos = xoffset;
                double yos = yoffset;
                xoffset += x;
                yoffset += y;
                String imPath = imageURL.toString();
                Document imageDoc = parseSVG(imPath, false);
                Element svgRoot = imageDoc.getDocumentElement();
                String viewBox = svgRoot.getAttribute(_viewBox);
                String[] vbs = viewBox.split(" ");
                String xorigin = vbs[0];
                String yorigin = vbs[1];
                String iwidth = vbs[2];
                String iheight = vbs[3];
                double xor = Double.parseDouble(xorigin);
                double yor = Double.parseDouble(yorigin);
                double iw = Double.parseDouble(iwidth);
                double ih = Double.parseDouble(iheight);
                scale = w / iw;
                xoffset -= xor * scale;
                yoffset -= yor * scale;
                load(imageDoc, vs, meta, imPath);
                scale = 1.0;
                xoffset = xos;
                yoffset = yos;
            } else {
                Glyph g = createImage(e, ctx, meta, imageStore, documentParentURL, fallbackParentURL);
                if (g != null) {
                    vs.addGlyph(g);
                }
            }
        } else if (tagName.equals(_g)) {
            if (ctx == null) {
                ctx = new Context();
            }
            double xos = xoffset;
            double yos = yoffset;
            if (e.hasAttribute(SVGReader._transform)) {
                String transform = e.getAttribute(_transform);
                if (transform.startsWith(_translate)) {
                    int transl = transform.indexOf("translate");
                    int rp = transform.indexOf(")", transl);
                    String tlate = transform.substring(transl + 10, rp);
                    String[] split = tlate.split(COMMA_SEP);
                    String tx, ty;
                    if (split.length >= 2) {
                        tx = split[0].trim();
                        ty = split[1].trim();
                    } else {
                        split = tlate.split(SPACE_SEP);
                        tx = split[0].trim();
                        ty = split[1].trim();
                    }
                    xoffset += Double.valueOf(tx) * scale;
                    yoffset += Double.valueOf(ty) * scale;
                }
            }
            NodeList objects = e.getChildNodes();
            boolean setAFont = false;
            if (e.hasAttribute(SVGReader._style)) {
                ctx.add(e.getAttribute(SVGReader._style));
                if (!mainFontSet) {
                    Font f;
                    if ((f = ctx.getDefinedFont()) != null) {
                        VText.setMainFont(f);
                        setAFont = true;
                    }
                } else {
                    setAFont = true;
                }
            }
            NodeList titles = e.getElementsByTagName(_title);
            if (titles.getLength() > 0) {
                try {
                    //try to get the title, be quiet if anything goes wrong
                    ctx.setTitle(((Element)titles.item(0)).getFirstChild().getNodeValue());
                } catch (Exception ex) {
                }
            }
            if (e.hasAttribute(SVGReader._id)) {
                try {
                    //try to get the group's id, be quiet if anything goes wrong
                    ctx.setClosestAncestorGroupID(e.getAttribute(SVGReader._id));
                } catch (Exception ex) {
                }
            }
            if (e.hasAttribute(SVGReader._id)) {
                try {
                    //try to get the group's id, be quiet if anything goes wrong
                    ctx.setClosestAncestorGroupClass(e.getAttribute(SVGReader._class));
                } catch (Exception ex) {
                }
            }
            for (int i = 0; i < objects.getLength(); i++) {
                Node obj = objects.item(i);
                if (obj.getNodeType() == Node.ELEMENT_NODE) {
                    processNode((Element)obj, vs,
                                ctx.duplicate(),
                                setAFont, meta,
                                documentParentURL, fallbackParentURL, imageStore);
                }
            }
            xoffset = xos;
            yoffset = yos;
        } else if (tagName.equals(_a)) {
            NodeList objects = e.getChildNodes();
            boolean setAFont = false;
            if (e.hasAttribute(SVGReader._style)) {
                if (ctx != null) {
                    ctx.add(e.getAttribute(SVGReader._style));
                } else {
                    ctx = new Context(e.getAttribute(SVGReader._style));
                }
                if (!mainFontSet) {
                    Font f;
                    if ((f = ctx.getDefinedFont()) != null) {
                        VText.setMainFont(f);
                        setAFont = true;
                    }
                } else {
                    setAFont = true;
                }
            }
            if (e.hasAttributeNS(xlinkURI, _href)) {
                if (ctx == null) {
                    ctx = new Context();
                }
                ctx.setURL(e.getAttributeNS(xlinkURI, _href));
            }
            if (e.hasAttributeNS(xlinkURI, _title)) {
                if (ctx == null) {
                    ctx = new Context();
                }
                ctx.setURLTitle(e.getAttributeNS(xlinkURI, _title));
            }
            for (int i = 0; i < objects.getLength(); i++) {
                Node obj = objects.item(i);
                if (obj.getNodeType() == Node.ELEMENT_NODE) {
                    processNode((Element)obj, vs,
                                (ctx != null) ? ctx.duplicate() : null,
                                setAFont, meta,
                                documentParentURL, fallbackParentURL, imageStore);
                }
            }
        } else if (tagName.equals(_title)) {
            //do nothing - is taken care of in each element's processing method if meta is true
        } else {
            System.err.println("SVGReader: unsupported element: " + tagName);
        }
    }

    /**
     * Returns the Font object corresponding to the provided description.
     * The font is taken from the cache if in there, or created if not (and then stored for subsequent requests)
     *
     * @param fontFamily the font's family name as expected by AWT's Font constructor
     * @param fontStyle  the font's style (one of Font.{PLAIN, BOLD, ITALIC, BOLD+ITALIC})
     * @param fontSize   the font's size
     */
    public static Font getFont(String fontFamily, int fontStyle, int fontSize) {
        Font bestFont = null;
        for (String family : fontFamily.split(",")) {
            Font res = null;
            if (fontCache.containsKey(family)) {
                List<List<Object>> v = fontCache.get(family);
                List<Object> fontData;
                for (int i = 0; i < v.size(); i++) {
                    fontData = v.get(i);
                    if ((Integer)fontData.get(0) == fontStyle
                        && (Integer)fontData.get(1) == fontSize) {
                        res = (Font)fontData.get(2);
                        break;
                    }
                }
                if (res == null) {
                    // if the font could not be found, create it and store it
                    fontData = new ArrayList<Object>();
                    fontData.add(fontStyle);
                    fontData.add(fontSize);
                    fontData.add(res = new Font(family, fontStyle, fontSize));
                    v.add(fontData);
                    fontCache.put(family, v);
                }
            } else {
                // if the font could not be found, create it and store it
                List<Object> fontData = new ArrayList<Object>();
                fontData.add(fontStyle);
                fontData.add(fontSize);
                fontData.add(res = new Font(family, fontStyle, fontSize));
                List<List<Object>> v = new ArrayList<List<Object>>();
                v.add(fontData);
                fontCache.put(family, v);
            }

            if (!family.equalsIgnoreCase("dialog") && res != null && res.getFamily().equalsIgnoreCase("dialog")) {
                bestFont = res;
                continue;
            } else {
                return res;
            }
        }

        return bestFont;
    }

    private static void setMetadata(Glyph g, Context ctx) {
        if (ctx != null
            && (ctx.getURL() != null || ctx.getURLTitle() != null || ctx.getTitle() != null
            || ctx.getClosestAncestorGroupID() != null || ctx.getClosestAncestorGroupClass() != null)) {
            g.setOwner(new Metadata(ctx.getURL(), ctx.getTitle(), ctx.getURLTitle(), ctx.getClosestAncestorGroupID(), ctx.getClosestAncestorGroupClass()));
        }
    }

    // context font, main font
    private static boolean specialFont(Font cFont, Font mFont) {
        if (cFont == null) {
            // if the context font is not defined, we do not have any information
            // to create a special font
            return false;
        } else if (!cFont.getFamily().equals(mFont.getFamily())
            || cFont.getSize() != mFont.getSize()
            || cFont.getStyle() != mFont.getStyle()) {
            return true;
        } else {
            return false;
        }
    }

    static URL getImageURL(String imagePath, String documentParentURL, String fallbackParentURL) {
        URL imageURL = null;
        // deal with absolute vs. relative paths
        if (imagePath.startsWith(FILE_SCHEME) || imagePath.startsWith(HTTP_SCHEME)) {
            // test file:/XXX, http://XXX, absolute URL with scheme, nothing to do
            try {
                imageURL = new URL(imagePath);
            } catch (MalformedURLException ex) {
                System.err.println("Failed to identify image location: " + imagePath);
            }
        } else if (imagePath.charAt(0) == '/') {
            // /XXX => absolute path without a scheme, prepend it: /XXX => file:///XXX
            imagePath = FILE_SCHEME + "/" + imagePath;
            try {
                imageURL = new URL(imagePath);
            } catch (MalformedURLException ex) {
                System.err.println("Failed to identify image location: " + imagePath);
            }
        } else if (imagePath.substring(1, 3).equals(":\\")) {
            // C:\XXX (Windows logical drive) => absolute path without a scheme, prepend it
            // and replace \ by / and : by | as follows C:\XXX\YYY => file:///C|/XXX/YYY
            imagePath = FILE_SCHEME + "//" + imagePath.replace('\\', '/').replace(':', '|');
            try {
                imageURL = new URL(imagePath);
            } catch (MalformedURLException ex) {
                System.err.println("Failed to identify image location: " + imagePath);
            }
        } else {
            // if none of the above, relative path, prepend parent URL
            String absImagePath = documentParentURL + imagePath;
            try {
                imageURL = new URL(absImagePath);
            } catch (MalformedURLException ex) {
                System.err.println("Failed to identify image location: " + imagePath);
            }
            if (imageURL.getProtocol().equals(FILE_PROTOCOL)) {
                // check that the image exists (if local), and if not attempt to fall back
                // on another directory that might contain the image (if defined)
                if (fallbackParentURL != null) {
                    try {
                        if (!(new File(new URI(imageURL.toString()))).exists()) {
                            // if it does not, attempt to load it using the fallback parent directory, if set
                            absImagePath = fallbackParentURL + imagePath;
                            try {
                                imageURL = new URL(absImagePath);
                            } catch (MalformedURLException ex) {
                            }
                        }
                    } catch (Exception ex) {
                        System.err.println("Image icon was found neither at\n"
                            + documentParentURL + imagePath
                            + "nor at\n"
                            + fallbackParentURL + imagePath);
                    }
                }
            }
        }
        return imageURL;
    }

    /*
     * get the in-memory ImageIcon of icon at imageLocation
     */
    static ImageIcon getImage(String imagePath, String documentParentURL, String fallbackParentURL, Map<URL, ImageIcon> imageStore) {
        URL imageURL = getImageURL(imagePath, documentParentURL, fallbackParentURL);
        ImageIcon res = null;
        if (imageStore.containsKey(imageURL)) {
            res = imageStore.get(imageURL);
        } else {
            try {
                res = new ImageIcon(imageURL);
                imageStore.put(imageURL, res);
            } catch (Exception ex) {
                System.err.println("Failed to load image from: " + imageURL.toString());
                res = null;
            }
        }
        return res;
    }

    static boolean isSVGImage(Element e) {
        if (e.hasAttributeNS(xlinkURI, _href)) {
            String imagePath = e.getAttributeNS(xlinkURI, _href);
            if (imagePath.endsWith(".svg")) {
                return true;
            }
        }
        return false;
    }

    static final String LOAD_EXTERNAL_DTD_URL = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    static Document parseSVG(String uri, boolean validation) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validation);
            if (!validation) {
                factory.setAttribute(LOAD_EXTERNAL_DTD_URL, Boolean.FALSE);
            }
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document res = builder.parse(uri);
            return res;
        } catch (FactoryConfigurationError e) {
            Exceptions.printStackTrace(e);
            return null;
        } catch (ParserConfigurationException e) {
            Exceptions.printStackTrace(e);
            return null;
        } catch (SAXException e) {
            Exceptions.printStackTrace(e);
            return null;
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
            return null;
        }
    }

}
