/*
 * FILE: SVGWriter.java
 * DATE OF CREATION: Nov 19 2001
 * AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com)
 * MODIF: Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 * Copyright (c) Xerox Corporation, XRCE/Contextual Computing, 2002. All Rights Reserved
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
 * $Id: SVGWriter.java 4377 2011-03-22 03:36:20Z epietrig $
 */
package fr.inria.zvtm.svg;

import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.DPath;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.Translucent;
import fr.inria.zvtm.glyphs.VCircle;
import fr.inria.zvtm.glyphs.VEllipse;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VPoint;
import fr.inria.zvtm.glyphs.VPolygon;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VSegment;
import fr.inria.zvtm.glyphs.VShape;
import fr.inria.zvtm.glyphs.VText;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import org.openide.util.Exceptions;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A class to export the content of a virtual space as an SVG document. Supports all glyph classes defined in package fr.inria.zvtm.glyphs, with transparency.
 *
 * @author Emmanuel Pietriga
 */
public class SVGWriter {

    //serif
    private static String _serifff = "serif";
    private static String _timesff = "times";
    private static String _garamondff = "garamond";
    private static String _minionff = "minion";
    private static String _cyberbitff = "cyberbit";
    private static String _georgiaff = "georgia";
    //sans-serif
    private static String _sansff = "sans";
    private static String _arialff = "arial";
    private static String _trebuchetff = "trebuchet";
    private static String _verdanaff = "verdana";
    private static String _universff = "univers";
    private static String _helveticaff = "helvetica";
    private static String _tahomaff = "tahoma";
    private static String _lucidaff = "lucida";
    //monospace
    private static String _courierff = "courier";
    private static String _monoff = "mono";
    //cursive
    private static String _cursiveff = "cursive";
    private static String _caflischff = "caflisch";
    private static String _poeticaff = "poetica";
    private static String _sanvitoff = "sanvito";
    private static String _corsivaff = "corsiva";
    //fantasy
    private static String _critterff = "critter";
    private static String _fantasyff = "fantasy";
    //stroke properties
    public static String _strokewidth = "stroke-width";
    public static String _strokelinecap = "stroke-linecap";
    public static String _strokelinejoin = "stroke-linejoin";
    public static String _strokemiterlimit = "stroke-miterlimit";
    public static String _strokedasharray = "stroke-dasharray";
    public static String _strokedashoffset = "stroke-dashoffset";
    public static String _strokecapbutt = "butt";
    public static String _strokecapround = "round";
    public static String _strokecapsquare = "square";
    public static String _strokejoinbevel = "bevel";
    public static String _strokejoinmiter = "miter";
    public static String _strokejoinround = "round";
    public static String _class = "class";
    public static float DEFAULT_MITER_LIMIT = 4.0f;
    public static float DEFAULT_DASH_OFFSET = 0.0f;
    /**
     * the SVG namespace URI
     */
    public static final String svgURI = "http://www.w3.org/2000/svg";
    /**
     * the xlink namespace URI
     */
    public static final String xlinkURI = "http://www.w3.org/1999/xlink";
    double farWest = 0;  //used to compute the translation of all glyphs so that they have positive coordinates
    double farNorth = 0; //used to compute the translation of all glyphs so that they have positive coordinates
    Document svgDoc; //exported document
    File destination;
    File img_subdir; //subdirectory in which images will be stored
    Map<Image, File> bitmapImages;  //used to remember bitmap images that have already been exported, so that they get exported once, and not as many times as they appear in the SVG

    public SVGWriter() {
    }

    /**
     * Export the content of a virtual space as a DOM object (that can then be serialized as an XML document using the parser of your choice (returns null if any error occurs)
     *
     * @param vs   virtual space to be exported
     * @param di   a DOMImplementation, like org.apache.xerces.dom.DOMImplementationImpl()
     * @param dest destination file to which the SVG is exported (SVGWriter does not serialize the SVG document, but needs this info in case there are bitmap images associated with your SVG document, so that they get output in a subdirectory whose name is based on the main SVG file name) - This is necessary ONLY if the exported VirtualSpace contains one or more instances of VImage (or any subclass). Otherwise, you can pass <i>null</i>. VImages get exported as separate PNG files referenced using <i>relative</i> URIs in the main SVG file. The scheme is as follows: for a main SVG file named abcd.svg, a directory named abcd_files will be created, and PNG images will be placed there, named zvtmXXX.png where XXX is a positive integer
     */
    public Document exportVirtualSpace(VirtualSpace vs, DOMImplementation di, File dest) {
        destination = dest;
        img_subdir = null;
        bitmapImages = new HashMap<Image, File>();
        if (di != null) {
            svgDoc = di.createDocument(svgURI, "svg", null);
            Element root = svgDoc.getDocumentElement();
            svgDoc.appendChild(svgDoc.createComment(" Generated by ZVTM (Zoomable Visual Transformation Machine) v0.9.8 http://zvtm.sourceforge.net"));
            double[] lurd = vs.findFarmostGlyphCoords();
            farWest = -lurd[0];
            farNorth = lurd[1];
            root.setAttribute("xmlns", svgURI);
            root.setAttribute("xmlns:xlink", xlinkURI);
            root.setAttribute(SVGReader._width, "800");
            root.setAttribute(SVGReader._height, "600");
            root.setAttribute("viewBox", "0 0 " + String.valueOf(lurd[2] - lurd[0]) + " " + String.valueOf(lurd[1] - lurd[3]));
            Element mainGroup = svgDoc.createElementNS(svgURI, SVGReader._g);
            mainGroup.setAttribute(SVGReader._style, createFontInformation(VText.getMainFont()));
            root.appendChild(mainGroup);
            Glyph[] visibleGlyphs = vs.getDrawingList();
            Element el;
            for (int i = 0; i < visibleGlyphs.length; i++) {
                el = processGlyph(visibleGlyphs[i]);
                if (el != null) {
                    mainGroup.appendChild(el);
                }
            }
            bitmapImages.clear();
            bitmapImages = null;
            return svgDoc;
        } else {
            return null;
        }
    }

    /**
     * Export the content of a virtual space as a DOM object (that can then be serialized as an XML document using the parser of your choice (returns null if any error occurs)
     *
     * @param vs   virtual space to be exported
     * @param di   a DOMImplementation, like org.apache.xerces.dom.DOMImplementationImpl()
     * @param dest destination file to which the SVG is exported (SVGWriter does not serialize the SVG document, but needs this info in case there are bitmap images associated with your SVG document, so that they get output in a subdirectory whose name is based on the main SVG file name) - This is necessary ONLY if the exported VirtualSpace contains one or more instances of VImage (or any subclass). Otherwise, you can pass <i>null</i>. VImages get exported as separate PNG files referenced using <i>relative</i> URIs in the main SVG file. The scheme is as follows: for a main SVG file named abcd.svg, a directory named abcd_files will be created, and PNG images will be placed there, named zvtmXXX.png where XXX is a positive integer
     * @param epp  object implementing the SVGWriterPostProcessing interface
     */
    public Document exportVirtualSpace(VirtualSpace vs, DOMImplementation di, File dest, SVGWriterPostProcessor epp) {
        destination = dest;
        img_subdir = null;
        bitmapImages = new HashMap<Image, File>();
        if (di != null) {
            svgDoc = di.createDocument(svgURI, "svg", null);
            Element root = svgDoc.getDocumentElement();
            svgDoc.appendChild(svgDoc.createComment(" Generated by ZVTM (Zoomable Visual Transformation Machine) v0.9.1 http://zvtm.sourceforge.net"));
            double[] lurd = vs.findFarmostGlyphCoords();
            farWest = -lurd[0];
            farNorth = lurd[1];
            root.setAttribute("xmlns", svgURI);
            root.setAttribute("xmlns:xlink", xlinkURI);
            root.setAttribute(SVGReader._width, "800");
            root.setAttribute(SVGReader._height, "600");
            root.setAttribute("viewBox", "0 0 " + String.valueOf(lurd[2] - lurd[0]) + " " + String.valueOf(lurd[1] - lurd[3]));
            Element mainGroup = svgDoc.createElementNS(svgURI, SVGReader._g);
            mainGroup.setAttribute(SVGReader._style, createFontInformation(VText.getMainFont()));
            root.appendChild(mainGroup);
            Glyph[] visibleGlyphs = vs.getDrawingList();
            Element el;
            for (int i = 0; i < visibleGlyphs.length; i++) {
                el = processGlyph(visibleGlyphs[i]);
                if (el != null) {
                    mainGroup.appendChild(el);
                    epp.newElementCreated(el, visibleGlyphs[i], svgDoc);
                }
            }
            bitmapImages.clear();
            bitmapImages = null;
            return svgDoc;
        } else {
            return null;
        }
    }

    private Element processGlyph(Glyph o) {
        if (o.isVisible()) {
            if (o instanceof VEllipse) {
                return createEllipse((VEllipse)o);
            } else if (o instanceof VRectangle) {
                return createRect((VRectangle)o);
            } else if (o instanceof DPath) {
                return createPath((DPath)o);
            } else if (o instanceof VText) {
                return createText((VText)o);
            } else if (o instanceof VCircle) {
                return createCircle((VCircle)o);
            } else if (o instanceof VPolygon) {
                return createPolygon((VPolygon)o);
            } else if (o instanceof VPoint) {
                return createPoint((VPoint)o);
            } else if (o instanceof VSegment) {
                return createLine((VSegment)o);
            } else if (o instanceof VShape) {
                return createPolygon((VShape)o);
            } else if (o instanceof VImage) {
                return createImage((VImage)o);
            } else {
                System.err.println("There is currently no support for outputing this glyph as SVG: " + o);
                return null;
            }
        } else {
            return null;
        }
    }

    public static String getGenericFontFamily(Font f) {
        String family = f.getFamily().toLowerCase();
        if (family.contains(_timesff) || family.contains(_garamondff) || family.contains(_minionff) || family.contains(_cyberbitff) || family.contains(_georgiaff) || (family.contains(_serifff) && !family.contains(_sansff))) {
            return "serif";
        } else if (family.contains(_sansff) || family.contains(_arialff) || family.contains(_trebuchetff) || family.contains(_verdanaff) || family.contains(_universff) || family.contains(_helveticaff) || family.contains(_tahomaff) || family.contains(_lucidaff)) {
            return "sans-serif";
        } else if (family.contains(_courierff) || family.contains(_monoff)) {
            return "monospace";
        } else if (family.contains(_cursiveff) || family.contains(_caflischff) || family.contains(_poeticaff) || family.contains(_sanvitoff) || family.contains(_corsivaff)) {
            return "cursive";
        } else if (family.contains(_fantasyff) || family.contains(_critterff)) {
            return "fantasy";
        } else {
            return "serif";
        }
    }

    private String createFontInformation(Font f) {
        return "font-family:" + f.getFamily() + "," + getGenericFontFamily(f) + ";font-style:" + ((f.getStyle() == Font.ITALIC || f.getStyle() == Font.BOLD + Font.ITALIC) ? "italic" : "normal") + ";font-weight:" + ((f.getStyle() == Font.BOLD || f.getStyle() == Font.BOLD + Font.ITALIC) ? "bold" : "normal") + ";font-size:" + Integer.toString(f.getSize());
    }

    /*
     * except for stroke color which is dealt with in shapeColors
     */
    private void createStrokeInformation(Glyph g, Element e) {
        if (g.getStroke() != null && g.getStroke() instanceof BasicStroke) {
            BasicStroke bs = (BasicStroke)g.getStroke();
            e.setAttribute(SVGWriter._strokewidth, Float.toString(bs.getLineWidth()));
            if (bs.getEndCap() != BasicStroke.CAP_BUTT) {
                e.setAttribute(SVGWriter._strokelinecap, (bs.getEndCap() == BasicStroke.CAP_SQUARE) ? _strokecapsquare : _strokecapround);
            }
            if (bs.getLineJoin() != BasicStroke.JOIN_MITER) {
                e.setAttribute(SVGWriter._strokelinejoin, (bs.getLineJoin() == BasicStroke.JOIN_BEVEL) ? _strokejoinbevel : _strokejoinround);
            }
            if (bs.getMiterLimit() != SVGWriter.DEFAULT_MITER_LIMIT) {
                e.setAttribute(SVGWriter._strokemiterlimit, Float.toString(bs.getMiterLimit()));
            }
            if (bs.getDashArray() != null) {
                e.setAttribute(SVGWriter._strokedasharray, SVGWriter.arrayOffloatAsCSStrings(bs.getDashArray()));
            }
            if (bs.getDashPhase() != SVGWriter.DEFAULT_DASH_OFFSET) {
                e.setAttribute(SVGWriter._strokedashoffset, Float.toString(bs.getDashPhase()));
            }
        }
    }

    /*
     * returns style attributes for fill color and stroke color
     */
    private String shapeColors(Glyph g) {
        String res;
        Color fill = g.getColor();
        Color border = g.getBorderColor();
        if (g.isFilled()) {
            res = "fill:rgb(" + fill.getRed() + "," + fill.getGreen() + "," + fill.getBlue() + ")";
        } else {
            res = "fill:none";
        }
        if (g.isBorderDrawn()) {
            res += ";stroke:rgb(" + border.getRed() + "," + border.getGreen() + "," + border.getBlue() + ")";
        } else {
            res += ";stroke:none";
        }
        if (g instanceof Translucent) {
            res += ";fill-opacity:" + String.valueOf(((Translucent)g).getTranslucencyValue());
        }
        return res;
    }

    private void createClassInforation(Glyph g, Element e) {
        if (g.getType() != null) {
            e.setAttribute(SVGWriter._class, g.getType());
        }
    }

    private Element createGroup() {
        Element res = svgDoc.createElementNS(svgURI, SVGReader._g);
        return res;
    }

    private Element createEllipse(VEllipse e) {
        Element shape = svgDoc.createElementNS(svgURI, SVGReader._ellipse);
        shape.setAttribute(SVGReader._cx, String.valueOf(e.vx + farWest));
        shape.setAttribute(SVGReader._cy, String.valueOf(-e.vy + farNorth));
        shape.setAttribute(SVGReader._rx, String.valueOf(e.getWidth() / 2d));
        shape.setAttribute(SVGReader._ry, String.valueOf(e.getHeight() / 2d));
        shape.setAttribute(SVGReader._style, shapeColors(e));
        if (e.getStroke() != null) {
            createStrokeInformation(e, shape);
        }
        createClassInforation(e, shape);
        return shape;
    }

    private Element createCircle(VCircle c) {
        Element shape = svgDoc.createElementNS(svgURI, SVGReader._circle);
        shape.setAttribute(SVGReader._cx, String.valueOf(c.vx + farWest));
        shape.setAttribute(SVGReader._cy, String.valueOf(-c.vy + farNorth));
        shape.setAttribute(SVGReader._r, String.valueOf(Math.round(c.getSize() / 2d)));
        shape.setAttribute(SVGReader._style, shapeColors(c));
        if (c.getStroke() != null) {
            createStrokeInformation(c, shape);
        }
        createClassInforation(c, shape);
        return shape;
    }

    private Element createRect(VRectangle r) {
        Element shape;
        if (r.getOrient() == 0) {
            shape = svgDoc.createElementNS(svgURI, SVGReader._rect);
            shape.setAttribute(SVGReader._x, String.valueOf(r.vx - r.getWidth() / 2d + farWest));
            shape.setAttribute(SVGReader._y, String.valueOf(-r.vy - r.getHeight() / 2d + farNorth));
            shape.setAttribute(SVGReader._width, String.valueOf(r.getWidth()));
            shape.setAttribute(SVGReader._height, String.valueOf(r.getHeight()));
            shape.setAttribute(SVGReader._style, shapeColors(r));
        } else {
            shape = svgDoc.createElementNS(svgURI, SVGReader._polygon);
            double x1 = -r.getWidth() / 2d;
            double y1 = -r.getHeight() / 2d;
            double x2 = r.getWidth() / 2d;
            double y2 = r.getHeight() / 2d;
            double[] xcoords = new double[4];
            double[] ycoords = new double[4];
            xcoords[0] = (x2 * Math.cos(Math.PI - r.getOrient()) + y1 * Math.sin(Math.PI - r.getOrient())) + r.vx + farWest;
            xcoords[1] = (x1 * Math.cos(Math.PI - r.getOrient()) + y1 * Math.sin(Math.PI - r.getOrient())) + r.vx + farWest;
            xcoords[2] = (x1 * Math.cos(Math.PI - r.getOrient()) + y2 * Math.sin(Math.PI - r.getOrient())) + r.vx + farWest;
            xcoords[3] = (x2 * Math.cos(Math.PI - r.getOrient()) + y2 * Math.sin(Math.PI - r.getOrient())) + r.vx + farWest;
            ycoords[0] = -(y1 * Math.cos(Math.PI - r.getOrient()) - x2 * Math.sin(Math.PI - r.getOrient())) + r.vy + farNorth;
            ycoords[1] = -(y1 * Math.cos(Math.PI - r.getOrient()) - x1 * Math.sin(Math.PI - r.getOrient())) + r.vy + farNorth;
            ycoords[2] = -(y2 * Math.cos(Math.PI - r.getOrient()) - x1 * Math.sin(Math.PI - r.getOrient())) + r.vy + farNorth;
            ycoords[3] = -(y2 * Math.cos(Math.PI - r.getOrient()) - x2 * Math.sin(Math.PI - r.getOrient())) + r.vy + farNorth;
            shape.setAttribute(SVGReader._points, String.valueOf(xcoords[0]) + "," + String.valueOf(ycoords[0]) + " " + String.valueOf(xcoords[1]) + "," + String.valueOf(ycoords[1]) + " " + String.valueOf(xcoords[2]) + "," + String.valueOf(ycoords[2]) + " " + String.valueOf(xcoords[3]) + "," + String.valueOf(ycoords[3]));
            shape.setAttribute(SVGReader._style, shapeColors(r));
        }
        if (r.getStroke() != null) {
            createStrokeInformation(r, shape);
        }
        createClassInforation(r, shape);
        return shape;
    }

    /**
     * Serialize a DPath's coordinates.
     */
    public String getSVGPathCoordinates(DPath p) {
        return getSVGPathCoordinates(p.getSVGPathIterator());
    }

    /**
     * Serialize a DPath's coordinates (from its PathIterator).
     */
    public String getSVGPathCoordinates(PathIterator pi) {
        StringBuilder coords = new StringBuilder();
        float[] seg = new float[6];
        int type;
        //anything but M, L, Q, C since we want the first command to explicitely appear in any case
        char lastOp = 'Z';
        while (!pi.isDone()) {
            //save the path as a sequence of instructions following the SVG model for "d" attributes
            type = pi.currentSegment(seg);
            switch (type) {
            case java.awt.geom.PathIterator.SEG_MOVETO: {
                if (lastOp != 'M') {
                    coords.append('M');
                } else {
                    coords.append(' ');
                }
                lastOp = 'M';
                coords.append(seg[0] + farWest).append(" ").append(seg[1] + farNorth);
                break;
            }
            case java.awt.geom.PathIterator.SEG_LINETO: {
                if (lastOp != 'L') {
                    coords.append('L');
                } else {
                    coords.append(' ');
                }
                lastOp = 'L';
                coords.append(seg[0] + farWest).append(" ").append(seg[1] + farNorth);
                break;
            }
            case java.awt.geom.PathIterator.SEG_QUADTO: {
                if (lastOp != 'Q') {
                    coords.append('Q');
                } else {
                    coords.append(' ');
                }
                lastOp = 'Q';
                coords.append(seg[0] + farWest).append(" ").append(seg[1] + farNorth).append(" ").append(seg[2] + farWest).append(" ").append(seg[3] + farNorth);
                break;
            }
            case java.awt.geom.PathIterator.SEG_CUBICTO: {
                if (lastOp != 'C') {
                    coords.append('C');
                } else {
                    coords.append(' ');
                }
                lastOp = 'C';
                coords.append(seg[0] + farWest).append(" ").append(seg[1] + farNorth).append(" ").append(seg[2] + farWest).append(" ").append(seg[3] + farNorth).append(" ").append(seg[4] + farWest).append(" ").append(seg[5] + farNorth);
                break;
            }
            }
            pi.next();
        }
        return coords.toString();
    }

    private Element createPath(DPath p) {
        Element path = svgDoc.createElementNS(svgURI, SVGReader._path);
        path.setAttribute(SVGReader._d, getSVGPathCoordinates(p));
        Color c = p.getColor();
        String color = "stroke:rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")";
        path.setAttribute(SVGReader._style, "fill:none;" + color);
        if (p.getStroke() != null) {
            createStrokeInformation(p, path);
        }
        createClassInforation(p, path);
        return path;
    }

    private Element createText(VText t) {
        Element text = svgDoc.createElementNS(svgURI, SVGReader._text);
        text.setAttribute(SVGReader._x, String.valueOf(t.vx + farWest));
        text.setAttribute(SVGReader._y, String.valueOf(-t.vy + farNorth));
        if (t.getTextAnchor() == VText.TEXT_ANCHOR_START) {
            text.setAttribute(SVGReader._textanchor, "start");
        } else if (t.getTextAnchor() == VText.TEXT_ANCHOR_MIDDLE) {
            text.setAttribute(SVGReader._textanchor, "middle");
        } else if (t.getTextAnchor() == VText.TEXT_ANCHOR_END) {
            text.setAttribute(SVGReader._textanchor, "end");
        }
        text.appendChild(svgDoc.createTextNode(t.getText()));
        Color c = t.getColor();
        //only fill, do not add stroke:rgb("+c.getRed()+","+c.getGreen()+","+c.getBlue()+") as it creates a wide stroke
        String style = "fill:rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")";
        if (t.usesSpecificFont()) {
            style = createFontInformation(t.getFont()) + ";" + style;
        }
        text.setAttribute(SVGReader._style, style);
        createClassInforation(t, text);
        return text;
    }

    private Element createPoint(VPoint p) {
        Element shape = svgDoc.createElementNS(svgURI, SVGReader._rect);
        shape.setAttribute(SVGReader._x, String.valueOf(p.vx + farWest));
        shape.setAttribute(SVGReader._y, String.valueOf(-p.vy + farNorth));
        shape.setAttribute(SVGReader._width, "1");
        shape.setAttribute(SVGReader._height, "1");
        Color c = p.getColor();
        shape.setAttribute(SVGReader._style, "stroke:rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")");
        createClassInforation(p, shape);
        return shape;
    }

    private Element createLine(VSegment s) {
        Element shape = svgDoc.createElementNS(svgURI, SVGReader._line);
        Point2D.Double[] endPoints = s.getEndPoints();
        shape.setAttribute("x1", String.valueOf(endPoints[0].x + farWest));
        shape.setAttribute("y1", String.valueOf(-endPoints[0].y + farNorth));
        shape.setAttribute("x2", String.valueOf(endPoints[1].x + farWest));
        shape.setAttribute("y2", String.valueOf(-endPoints[1].y + farNorth));
        Color c = s.getColor();
        shape.setAttribute(SVGReader._style, "stroke:rgb(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")");
        if (s.getStroke() != null) {
            createStrokeInformation(s, shape);
        }
        createClassInforation(s, shape);
        return shape;
    }

    private Element createPolygon(VShape s) {
        Element shape = svgDoc.createElementNS(svgURI, SVGReader._polygon);
        float[] vertices = s.getVertices();
        double vertexAngle = -s.getOrient();
        double[] xcoords = new double[vertices.length];
        double[] ycoords = new double[vertices.length];
        for (int j = 0; j < vertices.length; j++) {
            xcoords[j] = s.vx + s.getSize() / 2d * Math.cos(vertexAngle) * vertices[j] + farWest;
            ycoords[j] = -s.vy - s.getSize() / 2d * Math.sin(vertexAngle) * vertices[j] + farNorth;
            vertexAngle -= 2 * Math.PI / vertices.length;
        }
        String coords = "";
        for (int j = 0; j < vertices.length - 1; j++) {
            coords += String.valueOf(xcoords[j]) + "," + String.valueOf(ycoords[j]) + " ";
        }//last point outside loop just to avoid white space char at end of attrib value
        coords += String.valueOf(xcoords[vertices.length - 1]) + "," + String.valueOf(ycoords[vertices.length - 1]);
        shape.setAttribute(SVGReader._points, coords);
        shape.setAttribute(SVGReader._style, shapeColors(s));
        if (s.getStroke() != null) {
            createStrokeInformation(s, shape);
        }
        createClassInforation(s, shape);
        return shape;
    }

    private Element createPolygon(VPolygon p) {
        Element polygon = svgDoc.createElementNS(svgURI, SVGReader._polygon);
        Point2D.Double[] vertices = p.getVertices();
        double[] xcoords = new double[vertices.length];
        double[] ycoords = new double[vertices.length];
        for (int j = 0; j < vertices.length; j++) {
            xcoords[j] = p.vx + vertices[j].x + farWest;
            ycoords[j] = -p.vy - vertices[j].y + farNorth;
        }
        String coords = "";
        for (int j = 0; j < vertices.length - 1; j++) {
            coords += String.valueOf(xcoords[j]) + "," + String.valueOf(ycoords[j]) + " ";
        }//last point outside loop just to avoid white space char at end of attrib value
        coords += String.valueOf(xcoords[vertices.length - 1]) + "," + String.valueOf(ycoords[vertices.length - 1]);
        polygon.setAttribute(SVGReader._points, coords);
        polygon.setAttribute(SVGReader._style, shapeColors(p));
        if (p.getStroke() != null) {
            createStrokeInformation(p, polygon);
        }
        createClassInforation(p, polygon);
        return polygon;
    }

    /*
     * Dump the image itself as an external PNG or JPEG
     * file and reference it using the xlink:href attribute
     */
    private Element createImage(VImage i) {
        Element shape;
        try {
            shape = svgDoc.createElementNS(svgURI, SVGReader._image);
            shape.setAttribute(SVGReader._x, String.valueOf(i.vx - i.getWidth() / 2d + farWest));
            shape.setAttribute(SVGReader._y, String.valueOf(-i.vy - i.getHeight() / 2d + farNorth));
            shape.setAttribute(SVGReader._width, String.valueOf(i.getWidth()));
            shape.setAttribute(SVGReader._height, String.valueOf(i.getHeight()));
            Image im = i.getImage();
            ImageWriter writer = ImageIO.getImageWritersByFormatName("png").next();
            File f = null;
            //create a subdirectory based on the main SVG file name, removing extension (probably .svg) and appending "_files"
            if (img_subdir == null || !img_subdir.exists() || !img_subdir.isDirectory()) {
                String dirName = destination.getName();
                int lio;
                if ((lio = dirName.lastIndexOf(".")) > 0) {
                    dirName = dirName.substring(0, lio);
                }
                dirName += "_files";
                img_subdir = new File(destination.getParentFile(), dirName);
                img_subdir.mkdirs();
            }
            if (bitmapImages.containsKey(im)) {
                f = bitmapImages.get(im);
            } else {
                f = File.createTempFile("zvtm", ".png", img_subdir);
                writer.setOutput(ImageIO.createImageOutputStream(f));
                BufferedImage bi = new BufferedImage(im.getWidth(null), im.getHeight(null), BufferedImage.TYPE_INT_ARGB);
                (bi.createGraphics()).drawImage(im, null, null);
                writer.write(bi);
                bitmapImages.put(im, f);
            }
            shape.setAttributeNS(xlinkURI, "xlink:href", img_subdir.getName() + "/" + f.getName());  //relative URI as the png files are supposed
            //to be in img_subdir w.r.t the SVG file
        } catch (Exception ex) {
            shape = svgDoc.createElementNS(svgURI, SVGReader._rect);
            shape.setAttribute(SVGReader._x, String.valueOf(i.vx - i.getWidth() / 2d + farWest));
            shape.setAttribute(SVGReader._y, String.valueOf(-i.vy - i.getHeight() / 2d + farNorth));
            shape.setAttribute(SVGReader._width, String.valueOf(i.getWidth()));
            shape.setAttribute(SVGReader._height, String.valueOf(i.getHeight()));
            System.err.println("SVGWriter:An error occured while exporting " + i.toString() + " to PNG.\n" + ex);
            if (!Utils.javaVersionIs140OrLater()) {
                System.err.println("ZVTM/SVGWriter:Error: the Java Virtual Machine in use seems to be older than version 1.4.0 ; package javax.imageio is probably missing, which prevents generating bitmap files for representing VImage objects. Install a JVM version 1.4.0 or later if you want to use this functionality.");
            }

            Exceptions.printStackTrace(ex);
        }
        createClassInforation(i, shape);
        return shape;
    }

    /*
     * Convert an array of floats to a single string containing all values separated by commas.
     * @return example "1.5,2.1,4" for input {1.5f, 2f, 4f}
     */
    public static String arrayOffloatAsCSStrings(float[] ar) {
        String res = "";
        for (int i = 0; i < ar.length - 1; i++) {
            res += Float.toString(ar[i]) + ",";
        }
        res += Float.toString(ar[ar.length - 1]);
        return res;
    }

}
