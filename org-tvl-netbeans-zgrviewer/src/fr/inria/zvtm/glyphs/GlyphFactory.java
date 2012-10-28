/*
 * FILE: GlyphFactory.java DATE OF CREATION: Mon Oct 21 07:55:36 2002 AUTHOR :
 * Emmanuel Pietriga (emmanuel@w3.org) MODIF: Thu Feb 20 10:44:34 2003 by
 * Emmanuel Pietriga Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved
 * Copyright (c) INRIA, 2004-2010. All Rights Reserved Licensed under the GNU
 * LGPL. For full terms see the file COPYING. $Id: GlyphFactory.java 3644
 * 2010-08-21 08:27:03Z epietrig $
 */
package fr.inria.zvtm.glyphs;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class GlyphFactory extends JDialog implements ActionListener, MouseListener, ChangeListener {

    public static Color PANEL_BKG = (Color)javax.swing.UIManager.getLookAndFeelDefaults().get("Panel.background");
    public static String V_Shape = "VShape";
    public static String V_Rectangle = "VRectangle";
    public static String V_Ellipse = "VEllipse";
    public static String V_Circle = "VCircle";

    private static List<String> initShapeTypes() {
        List<String> res = new ArrayList<String>();
        res.add(V_Shape);
        res.add(V_Rectangle);
        res.add(V_Ellipse);
        res.add(V_Circle);
        return res;
    }

    private static List<String> allowedShapeTypes = initShapeTypes();
    @SuppressWarnings("StaticNonFinalUsedInInitialization")
    private static String defaultShapeType = V_Shape;
    private static boolean changeableShapeType = true;
    private static Color defaultFillColor = new Color(204, 204, 255);
    private static boolean editableFillColor = true;
    private static Color defaultBorderColor = Color.black;
    private static boolean editableBorderColor = true;
    private static boolean defaultTransparencyOnOff = true;
    private static boolean editableTransparencyOnOff = true;
    private static double defaultAlphaValue = 1.0;
    private static boolean editableAlphaValue = true;
    private static boolean defaultOrientationOnOff = true;
    private static boolean editableOrientationOnOff = true;
    private static double defaultAngleValue = 0.0;
    private static boolean editableAngleValue = true;

    public static boolean hasEditableAngle() {
        return editableAngleValue;
    }

    private static long defaultSizeValue = 10;
    private static boolean editableSizeValue = true;
    private static int defaultVertexCount = 8;
    private static boolean editableVertexCount = true;
    private static double[] defaultVertexValues = {1.0, 0.5, 1.0, 0.5, 1.0, 0.5, 1.0, 0.5};
    private static boolean editableVertexValues = true;

    public static boolean hasEditableVertexValues() {
        return editableVertexValues;
    }

    private static int GLYPH_PANEL_WIDTH = 301;
    GlyphTracker gt;
    GlyphPanel glyphPanel;
    JComboBox glList;
    ColorIndicator ci1, ci2;
    JCheckBox transpChk, angleChk, aliasChk, gridChk, geomChk;
    JSpinner transpSpin, angleSpin, sizeSpin, vxSpin;
    JLabel vxVal, vxValLb, vxLb;
    JButton okBt, cancelBt, resetBt;
    double angle = defaultAngleValue;
    long size = defaultSizeValue;
    boolean orientable = defaultOrientationOnOff;
    double aspectRatio = 1.0;  //(width/height ratio for RectangularShape)
    double alpha = defaultAlphaValue;
    Color fillColor = defaultFillColor;
    Color borderColor = defaultBorderColor;
    double[] vertices = defaultVertexValues;

    /**
     * Call a GlyphFactory window that will return the glyph created in the
     * window (all parameters have default value and are editable)
     *
     * @param owner application frame that owns this Modal component
     */
    public static Glyph getGlyphFactoryDialog(Frame owner) {
        GlyphTracker res = new GlyphTracker();
        GlyphFactory gf = new GlyphFactory(res, owner);
        gf.addWindowListener(new GlyphFactory.Closer());
        gf.addComponentListener(new GlyphFactory.DisposeOnClose());
        gf.setVisible(true);  //blocks until the dialog is closed
        return res.getGlyph();
    }

    /**
     * Call a GlyphFactory window that will return the glyph created in the
     * window (all parameters have default value and are editable)
     *
     * @param owner application dialog that owns this Modal component
     */
    public static Glyph getGlyphFactoryDialog(Dialog owner) {
        GlyphTracker res = new GlyphTracker();
        GlyphFactory gf = new GlyphFactory(res, owner);
        gf.addWindowListener(new GlyphFactory.Closer());
        gf.addComponentListener(new GlyphFactory.DisposeOnClose());
        gf.setVisible(true);  //blocks until the dialog is closed
        return res.getGlyph();
    }

    /**
     * Call a GlyphFactory window that will return the glyph created in the
     * window (all parameters have default value and are editable)
     *
     * @param owner application frame that owns this Modal component
     * @param dsv default size value (positive number) ; takes its default value
     * if provided param is -1
     */
    public static Glyph getGlyphFactoryDialog(Frame owner, long dsv) {
        if (dsv != -1) {
            defaultSizeValue = dsv;
        }
        GlyphTracker res = new GlyphTracker();
        GlyphFactory gf = new GlyphFactory(res, owner);
        gf.addWindowListener(new GlyphFactory.Closer());
        gf.addComponentListener(new GlyphFactory.DisposeOnClose());
        gf.setVisible(true);  //blocks until the dialog is closed
        return res.getGlyph();
    }

    /**
     * Call a GlyphFactory window that will return the glyph created in the
     * window (all parameters have default value and are editable)
     *
     * @param owner application dialog that owns this Modal component
     * @param dsv default size value (positive number) ; takes its default value
     * if provided param is -1
     */
    public static Glyph getGlyphFactoryDialog(Dialog owner, long dsv) {
        if (dsv != -1) {
            defaultSizeValue = dsv;
        }
        GlyphTracker res = new GlyphTracker();
        GlyphFactory gf = new GlyphFactory(res, owner);
        gf.addWindowListener(new GlyphFactory.Closer());
        gf.addComponentListener(new GlyphFactory.DisposeOnClose());
        gf.setVisible(true);  //blocks until the dialog is closed
        return res.getGlyph();
    }

    /**
     * Call a GlyphFactory window that will return the glyph created in the
     * window (programer sets the default value(s) for every field and specifies
     * if the field can be changed or not)
     *
     * @param owner application frame that owns this Modal component
     * @param ast vector of allowed shape types (any sequence of static String
     * V_xxx defined in this class) ; takes its default value if provided param
     * is null
     * @param dst default shape type (must be one of the elements of ast -
     * default is V_Shape)
     * @param cst tells whether the shape type can be changed or not
     * @param dfc default fill color ; takes its default value if provided param
     * is null
     * @param efc tells whether the fill color can be changed or not
     * @param dbc default border color ; takes its default value if provided
     * param is null
     * @param ebc tells whether the border color can be changed or not
     * @param dto is transparency ON or OFF by default
     * @param eto tells whether the transparency switch can be changed or not
     * @param dav default alpha channel value (in range [0,1.0]) ; takes its
     * default value if provided param is -1.0
     * @param eav tells whether the alpha value can be changed or not
     * @param doo is orientation ON or OFF by default
     * @param eoo tells whether the orientation switch can be toggled
     * @param dagv default angle value (in range [0,2*Pi]) ; takes its default
     * value if provided param is -10.0
     * @param eagv tells whether the angle value can be changed or not
     * @param dsv default size value (positive number) ; takes its default value
     * if provided param is -1
     * @param esv tells whether the size value can be changed or not
     * @param dvc default vertex count (positive number) ; takes its default
     * value if provided param is -1 ; if dvc and dvv are not coherent, dvv's
     * length determines the vertex count
     * @param evc tells whether the vertex count can be changed or not
     * @param dvv default vertex values (each value in range [0,1.0]) ; takes
     * its default value if provided param is null ; if dvc and dvv are not
     * coherent, dvv's length determines the vertex count
     * @param evv tells whether vertex values can be changed or not
     */
    public static Glyph getGlyphFactoryDialog(Frame owner, List<String> ast, String dst, boolean cst, Color dfc, boolean efc, Color dbc, boolean ebc, boolean dto, boolean eto, double dav, boolean eav, boolean doo, boolean eoo, double dagv, boolean eagv, long dsv, boolean esv, int dvc, boolean evc, double[] dvv, boolean evv) {
        if (ast != null) {
            allowedShapeTypes = ast;
        }
        if (allowedShapeTypes.contains(dst)) {
            defaultShapeType = dst;
        }
        changeableShapeType = cst;
        if (dfc != null) {
            defaultFillColor = dfc;
        }
        editableFillColor = efc;
        if (dbc != null) {
            defaultBorderColor = dbc;
        }
        editableBorderColor = ebc;
        defaultTransparencyOnOff = dto;
        editableTransparencyOnOff = eto;
        if (dav != -1.0) {
            defaultAlphaValue = dav;
        }
        editableAlphaValue = eav;
        defaultOrientationOnOff = doo;
        editableOrientationOnOff = eoo;
        if (dagv != -10.0) {
            defaultAngleValue = dagv;
        }
        editableAngleValue = eagv;
        if (dsv != -1) {
            defaultSizeValue = dsv;
        }
        editableSizeValue = esv;
        if (dvc > 0) {
            defaultVertexCount = dvc;
        }
        editableVertexCount = evc;
        if (dvv != null) {
            defaultVertexValues = dvv;
        }
        editableVertexValues = evv;
        if (defaultVertexValues.length != defaultVertexCount) {
            defaultVertexCount = defaultVertexValues.length;
        }
        return getGlyphFactoryDialog(owner);
    }

    /**
     * Call a GlyphFactory window that will return the glyph created in the
     * window (programer sets the default value(s) for every field and specifies
     * if the field can be changed or not)
     *
     * @param owner application dialog that owns this Modal component
     * @param ast vector of allowed shape types (any sequence of static String
     * V_xxx defined in this class) ; takes its default value if provided param
     * is null
     * @param dst default shape type (must be one of the elements of ast -
     * default is V_Shape)
     * @param cst tells whether the shape type can be changed or not
     * @param dfc default fill color ; takes its default value if provided param
     * is null
     * @param efc tells whether the fill color can be changed or not
     * @param dbc default border color ; takes its default value if provided
     * param is null
     * @param ebc tells whether the border color can be changed or not
     * @param dto is transparency ON or OFF by default
     * @param eto tells whether the transparency switch can be changed or not
     * @param dav default alpha channel value (in range [0,1.0]) ; takes its
     * default value if provided param is -1.0
     * @param eav tells whether the alpha value can be changed or not
     * @param doo is orientation ON or OFF by default
     * @param eoo tells whether the orientation switch can be toggled
     * @param dagv default angle value (in range [0,2*Pi]) ; takes its default
     * value if provided param is -10.0
     * @param eagv tells whether the angle value can be changed or not
     * @param dsv default size value (positive number) ; takes its default value
     * if provided param is -1
     * @param esv tells whether the size value can be changed or not
     * @param dvc default vertex count (positive number) ; takes its default
     * value if provided param is -1 ; if dvc and dvv are not coherent, dvv's
     * length determines the vertex count
     * @param evc tells whether the vertex count can be changed or not
     * @param dvv default vertex values (each value in range [0,1.0]) ; takes
     * its default value if provided param is null ; if dvc and dvv are not
     * coherent, dvv's length determines the vertex count
     * @param evv tells whether vertex values can be changed or not
     */
    public static Glyph getGlyphFactoryDialog(Dialog owner, List<String> ast, String dst, boolean cst, Color dfc, boolean efc, Color dbc, boolean ebc, boolean dto, boolean eto, double dav, boolean eav, boolean doo, boolean eoo, double dagv, boolean eagv, long dsv, boolean esv, int dvc, boolean evc, double[] dvv, boolean evv) {
        if (ast != null) {
            allowedShapeTypes = ast;
        }
        if (allowedShapeTypes.contains(dst)) {
            defaultShapeType = dst;
        }
        changeableShapeType = cst;
        if (dfc != null) {
            defaultFillColor = dfc;
        }
        editableFillColor = efc;
        if (dbc != null) {
            defaultBorderColor = dbc;
        }
        editableBorderColor = ebc;
        defaultTransparencyOnOff = dto;
        editableTransparencyOnOff = eto;
        if (dav != -1.0) {
            defaultAlphaValue = dav;
        }
        editableAlphaValue = eav;
        defaultOrientationOnOff = doo;
        editableOrientationOnOff = eoo;
        if (dagv != -10.0) {
            defaultAngleValue = dagv;
        }
        editableAngleValue = eagv;
        if (dsv != -1) {
            defaultSizeValue = dsv;
        }
        editableSizeValue = esv;
        if (dvc > 0) {
            defaultVertexCount = dvc;
        }
        editableVertexCount = evc;
        if (dvv != null) {
            defaultVertexValues = dvv;
        }
        editableVertexValues = evv;
        if (defaultVertexValues.length != defaultVertexCount) {
            defaultVertexCount = defaultVertexValues.length;
        }
        return getGlyphFactoryDialog(owner);
    }

    static float[] doubleToFloatArray(double[] ar) {
        float[] res = new float[ar.length];
        for (int i = 0; i < ar.length; i++) {
            res[i] = (float)ar[i];
        }
        return res;
    }

    GlyphFactory(GlyphTracker glt, Frame owner) {
        super(owner, "ZVTM Glyph Factory", true);
        gt = glt;
        initUI(defaultShapeType, true, false, true);
    }

    GlyphFactory(GlyphTracker glt, Dialog owner) {
        super(owner, "ZVTM Glyph Factory", true);
        gt = glt;
        initUI(defaultShapeType, true, false, true);
    }

    void initUI(String si, boolean grid, boolean alias, boolean geom) {//depending on selected item/default shape type
        Container cp = this.getContentPane();
        try {
            glList.removeActionListener(this);
            ci1.removeMouseListener(this);
            ci2.removeMouseListener(this);
            transpChk.removeActionListener(this);
            transpSpin.removeChangeListener(this);
            angleChk.removeActionListener(this);
            angleSpin.removeChangeListener(this);
            sizeSpin.removeChangeListener(this);
            vxSpin.removeChangeListener(this);
            gridChk.removeActionListener(this);
            aliasChk.removeActionListener(this);
            geomChk.removeActionListener(this);
            glyphPanel.removeMouseListener((MouseListener)glyphPanel);
            glyphPanel.removeMouseMotionListener((MouseMotionListener)glyphPanel);
            okBt.removeActionListener(this);
            cancelBt.removeActionListener(this);
            resetBt.removeActionListener(this);
        } catch (NullPointerException ex) {/*
             * all these might be null (for instance when poping up a
             * GlyphFactory for the first time)
             */

        }
        cp.removeAll();
        //glyph definition panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Parameters"));
        mainPanel.setLayout(new GridLayout(1, 2));
        //glyph repr panel
        glyphPanel = setGlyphPanel(si);
        glyphPanel.setPreferredSize(new Dimension(GLYPH_PANEL_WIDTH, GLYPH_PANEL_WIDTH));
        glyphPanel.setMinimumSize(new Dimension(GLYPH_PANEL_WIDTH, GLYPH_PANEL_WIDTH));
        glyphPanel.setMaximumSize(new Dimension(GLYPH_PANEL_WIDTH, GLYPH_PANEL_WIDTH));
        //glyphPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.RAISED));
        mainPanel.add(glyphPanel);
        glyphPanel.addMouseListener((MouseListener)glyphPanel);
        glyphPanel.addMouseMotionListener((MouseMotionListener)glyphPanel);
        //glyph param panel
        JPanel paramPanel = new JPanel();
        GridBagLayout gridBag1 = new GridBagLayout();
        GridBagConstraints constraints1 = new GridBagConstraints();
        paramPanel.setLayout(gridBag1);
        constraints1.fill = GridBagConstraints.HORIZONTAL;
        constraints1.anchor = GridBagConstraints.CENTER;
        List<String> glyphTypes = new ArrayList<String>(GlyphFactory.allowedShapeTypes);
        glList = new JComboBox(glyphTypes.toArray());
        glList.setMaximumRowCount(5);
        buildConstraints(constraints1, 0, 0, 2, 1, 100, 14);
        gridBag1.setConstraints(glList, constraints1);
        paramPanel.add(glList);
        glList.setSelectedItem(si);
        glList.addActionListener(this);
        glList.setEnabled(changeableShapeType);
        ci1 = new ColorIndicator("Fill Color", fillColor);
        buildConstraints(constraints1, 0, 1, 1, 1, 50, 14);
        gridBag1.setConstraints(ci1, constraints1);
        paramPanel.add(ci1);
        ci2 = new ColorIndicator("Border Color", borderColor);
        buildConstraints(constraints1, 1, 1, 1, 1, 50, 14);
        gridBag1.setConstraints(ci2, constraints1);
        paramPanel.add(ci2);
        ci1.addMouseListener(this);
        ci2.addMouseListener(this);

        transpChk = new JCheckBox("Translucent", defaultTransparencyOnOff);
        buildConstraints(constraints1, 0, 2, 1, 1, 50, 14);
        gridBag1.setConstraints(transpChk, constraints1);
        paramPanel.add(transpChk);
        transpChk.addActionListener(this);
        transpChk.setEnabled(editableTransparencyOnOff);
        transpSpin = new JSpinner(new SpinnerNumberModel(defaultAlphaValue, 0.0, 1.0, 0.05));
        buildConstraints(constraints1, 1, 2, 1, 1, 50, 14);
        gridBag1.setConstraints(transpSpin, constraints1);
        paramPanel.add(transpSpin);
        transpSpin.setEnabled(transpChk.isSelected() ? editableAlphaValue : false);
        transpSpin.addChangeListener(this);

        angleChk = new JCheckBox("Orientation", defaultOrientationOnOff);
        buildConstraints(constraints1, 0, 3, 1, 1, 50, 14);
        gridBag1.setConstraints(angleChk, constraints1);
        paramPanel.add(angleChk);
        angleChk.addActionListener(this);
        angleChk.setEnabled(editableOrientationOnOff);
        angleSpin = new JSpinner(new SpinnerNumberModel(defaultAngleValue, 0.0, 2 * Math.PI, 0.02));
        buildConstraints(constraints1, 1, 3, 1, 1, 50, 14);
        gridBag1.setConstraints(angleSpin, constraints1);
        paramPanel.add(angleSpin);
        angleSpin.setEnabled(angleChk.isSelected() ? editableAngleValue : false);
        angleSpin.addChangeListener(this);

        JLabel szLb = new JLabel("Size");
        buildConstraints(constraints1, 0, 4, 1, 1, 50, 14);
        gridBag1.setConstraints(szLb, constraints1);
        paramPanel.add(szLb);
        sizeSpin = new JSpinner(new SpinnerNumberModel((int)defaultSizeValue, 0, Integer.MAX_VALUE, 10));
        buildConstraints(constraints1, 1, 4, 1, 1, 50, 14);
        gridBag1.setConstraints(sizeSpin, constraints1);
        paramPanel.add(sizeSpin);
        sizeSpin.setEnabled(editableSizeValue);
        sizeSpin.addChangeListener(this);

        vxLb = new JLabel("Vertex count");
        buildConstraints(constraints1, 0, 5, 1, 1, 50, 14);
        gridBag1.setConstraints(vxLb, constraints1);
        paramPanel.add(vxLb);
        vxSpin = new JSpinner(new SpinnerNumberModel(defaultVertexCount, 3, Integer.MAX_VALUE, 1));
        buildConstraints(constraints1, 1, 5, 1, 1, 50, 14);
        gridBag1.setConstraints(vxSpin, constraints1);
        paramPanel.add(vxSpin);
        vxSpin.setEnabled(editableVertexCount);
        vxSpin.addChangeListener(this);

        vxValLb = new JLabel("Selected vertex value");
        buildConstraints(constraints1, 0, 6, 1, 1, 50, 14);
        gridBag1.setConstraints(vxValLb, constraints1);
        paramPanel.add(vxValLb);
        vxVal = new JLabel(" ");
        buildConstraints(constraints1, 1, 6, 1, 1, 50, 14);
        gridBag1.setConstraints(vxVal, constraints1);
        paramPanel.add(vxVal);

        gridChk = new JCheckBox("Grid", grid);
        buildConstraints(constraints1, 0, 7, 1, 1, 50, 14);
        gridBag1.setConstraints(gridChk, constraints1);
        paramPanel.add(gridChk);
        gridChk.addActionListener(this);
        aliasChk = new JCheckBox("Antialiasing", alias);
        buildConstraints(constraints1, 1, 7, 1, 1, 50, 14);
        gridBag1.setConstraints(aliasChk, constraints1);
        paramPanel.add(aliasChk);
        aliasChk.addActionListener(this);

        geomChk = new JCheckBox("Indicators", geom);
        buildConstraints(constraints1, 0, 8, 2, 1, 100, 14);
        gridBag1.setConstraints(geomChk, constraints1);
        paramPanel.add(geomChk);
        geomChk.setSelected(true);
        geomChk.addActionListener(this);

        paramPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        mainPanel.add(paramPanel);
        //ok, cancel, reset buttons
        JPanel btPanel = new JPanel();
        btPanel.setLayout(new FlowLayout());
        okBt = new JButton("OK");
        okBt.addActionListener(this);
        btPanel.add(okBt);
        cancelBt = new JButton("Cancel");
        cancelBt.addActionListener(this);
        btPanel.add(cancelBt);
        resetBt = new JButton("Reset");
        resetBt.addActionListener(this);
        btPanel.add(resetBt);
        //main components
        GridBagLayout gridBag = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        cp.setLayout(gridBag);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.anchor = GridBagConstraints.CENTER;
        buildConstraints(constraints, 0, 0, 1, 1, 100, 99);
        gridBag.setConstraints(mainPanel, constraints);
        cp.add(mainPanel);
        buildConstraints(constraints, 0, 1, 1, 1, 100, 1);
        gridBag.setConstraints(btPanel, constraints);
        cp.add(btPanel);
        pack();
        this.setResizable(false);
        if (si.equals(GlyphFactory.V_Shape)) {
            vxSpin.setEnabled(true);
            vxLb.setEnabled(true);
            vxVal.setEnabled(true);
            vxValLb.setEnabled(true);
        } else {
            vxSpin.setEnabled(false);
            vxLb.setEnabled(false);
            vxVal.setEnabled(false);
            vxValLb.setEnabled(false);
            if (si.equals(GlyphFactory.V_Ellipse) || si.equals(GlyphFactory.V_Circle)) {
                angleSpin.setValue(new Double(0.0));
                orientable = false;
                angleSpin.setEnabled(false);
                angleChk.setSelected(false);
                angleChk.setEnabled(false);
                if (glyphPanel.selectedVertex == -2) {
                    glyphPanel.selectedVertex = -1;
                }
            }
        }
        if (aliasChk.isSelected()) {
            glyphPanel.setAntialiasing(true);
        } else {
            glyphPanel.setAntialiasing(false);
        }
        if (gridChk.isSelected()) {
            glyphPanel.setGrid(true);
        } else {
            glyphPanel.setGrid(false);
        }
        if (geomChk.isSelected()) {
            glyphPanel.setGeom(true);
        } else {
            glyphPanel.setGeom(false);
        }
    }

    GlyphPanel setGlyphPanel(String glClass) {
        if (glClass.equals(V_Shape)) {
            return new VShapePanel(this);
        } else if (glClass.equals(V_Rectangle)) {
            return new VRectPanel(this);
        } else if (glClass.equals(V_Circle)) {
            return new VCirPanel(this);
        } else if (glClass.equals(V_Ellipse)) {
            return new VEllPanel(this);
        } else {
            return new VShapePanel(this);
        }
    }

    void changeFillColor(Color c) {
        fillColor = c;
        ci1.setColor(fillColor);
        glyphPanel.setColor(fillColor, borderColor);
    }

    void changeBorderColor(Color c) {
        borderColor = c;
        ci2.setColor(borderColor);
        glyphPanel.setColor(fillColor, borderColor);
    }

    void changeOrient(double d) {
        angle = d;
        glyphPanel.setAngle(angle);
    }

    void changeSize(int s) {
        size = s;
    }

    void updateAngleSpin(double d) {
        angle = d;
        angleSpin.setValue(new Double(d));
    }

    void changeTransp(double d) {
        alpha = d;
        glyphPanel.setTransparency(alpha);
    }

    void changeVertices(int i) {
        if (i > vertices.length) {
            double[] tmpA = new double[vertices.length + 1];
            System.arraycopy(vertices, 0, tmpA, 0, vertices.length);
            tmpA[tmpA.length - 1] = 1.0;
            vertices = tmpA;
            glyphPanel.setVertices(vertices);
        } else if (i < vertices.length) {
            double[] tmpA = new double[vertices.length - 1];
            System.arraycopy(vertices, 0, tmpA, 0, vertices.length - 1);
            vertices = tmpA;
            glyphPanel.setVertices(vertices);
        }
    }

    void setVertexVal(double d) {
        if (d >= 0) {
            String s = Double.toString(d);
            vxVal.setText((s.length() > 6) ? s.substring(0, 6) : s);
        } else {
            vxVal.setText("");
        }
    }

    Glyph instantiateGlyph(String si) {
        Glyph g = null;
        if (si.equals(V_Shape)) {
            if (transpChk.isSelected()) {
                g = new VShape(0, 0, 0, size, doubleToFloatArray(vertices), fillColor, borderColor, (float)angle, (float)alpha);
            } else {
                g = new VShape(0, 0, 0, size, doubleToFloatArray(vertices), fillColor, borderColor, (float)angle);
            }
        } else if (si.equals(V_Rectangle)) {
            long w, h;
            if (aspectRatio >= 1.0) {
                w = size;
                h = Math.round(size / aspectRatio);
            } else {
                h = size;
                w = Math.round(size * aspectRatio);
            }
            if (angleChk.isSelected()) {
                if (transpChk.isSelected()) {
                    g = new VRectangleOr(0, 0, 0, w, h, fillColor, borderColor, (float)angle, (float)alpha);
                } else {
                    g = new VRectangleOr(0, 0, 0, w, h, fillColor, borderColor, (float)angle);
                }
            } else {
                if (transpChk.isSelected()) {
                    g = new VRectangle(0, 0, 0, w, h, fillColor, borderColor, (float)alpha);
                } else {
                    g = new VRectangle(0, 0, 0, w, h, fillColor, borderColor);
                }
            }
        } else if (si.equals(V_Ellipse)) {
            long w, h;
            if (aspectRatio >= 1.0) {
                w = size;
                h = Math.round(size / aspectRatio);
            } else {
                h = size;
                w = Math.round(size * aspectRatio);
            }
            if (transpChk.isSelected()) {
                g = new VEllipse(0, 0, 0, w, h, fillColor, borderColor, (float)alpha);
            } else {
                g = new VEllipse(0, 0, 0, w, h, fillColor, borderColor);
            }
        } else if (si.equals(V_Circle)) {
            g = new VCircle(0, 0, 0, size, fillColor, borderColor, (float)alpha);
        }
// 	//border color
// 	float[] hsv=new float[3];
// 	Color.RGBtoHSB(borderColor.getRed(), borderColor.getGreen(),borderColor.getBlue(),hsv);
// 	g.setHSVbColor(hsv[0],hsv[1],hsv[2]);
        return g;
    }

    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == transpChk) {
            if (transpChk.isSelected()) {
                transpSpin.setEnabled(editableAlphaValue);
            } else {
                transpSpin.setValue(new Double(1.0));
                transpSpin.setEnabled(false);
            }
        } else if (source == angleChk) {
            if (angleChk.isSelected()) {
                orientable = true;
                angleSpin.setEnabled(editableAngleValue);
                if (glyphPanel.selectedVertex == -2) {
                    glyphPanel.selectedVertex = -1;
                }
                glyphPanel.repaint();
            } else {
                angleSpin.setValue(new Double(0.0));
                orientable = false;
                angleSpin.setEnabled(false);
                if (glyphPanel.selectedVertex == -2) {
                    glyphPanel.selectedVertex = -1;
                }
                glyphPanel.repaint();
            }
        } else if (source == aliasChk) {
            if (aliasChk.isSelected()) {
                glyphPanel.setAntialiasing(true);
            } else {
                glyphPanel.setAntialiasing(false);
            }
        } else if (source == gridChk) {
            if (gridChk.isSelected()) {
                glyphPanel.setGrid(true);
            } else {
                glyphPanel.setGrid(false);
            }
        } else if (source == geomChk) {
            if (geomChk.isSelected()) {
                glyphPanel.setGeom(true);
            } else {
                glyphPanel.setGeom(false);
            }
        } else if (source == okBt) {
            Glyph g = instantiateGlyph((String)glList.getSelectedItem());
            gt.setGlyph(g);
            this.dispose();
        } else if (source == cancelBt) {
            gt.setGlyph(null);
            this.dispose();
        } else if (source == resetBt) {
            angle = defaultAngleValue;
            angleSpin.setValue(new Double(angle));
            size = defaultSizeValue;
            sizeSpin.setValue(new Integer((int)size));
            alpha = defaultAlphaValue;
            transpSpin.setValue(new Double(alpha));
            fillColor = defaultFillColor;
            ci1.setColor(fillColor);
            borderColor = defaultBorderColor;
            ci2.setColor(borderColor);
            vertices = defaultVertexValues;
            vxSpin.setValue(new Integer(vertices.length));
            glyphPanel.setColor(fillColor, borderColor);
            glyphPanel.setAngle(angle);
            glyphPanel.setTransparency(alpha);
            glyphPanel.setVertices(vertices);
        } else if (source == glList) {
            initUI((String)glList.getSelectedItem(), gridChk.isSelected(), aliasChk.isSelected(), geomChk.isSelected());
        }
    }

    public void stateChanged(ChangeEvent e) {
        Object source = e.getSource();
        if (source == transpSpin) {
            changeTransp(((Double)transpSpin.getValue()).doubleValue());
        } else if (source == vxSpin) {
            changeVertices(((Integer)vxSpin.getValue()).intValue());
        } else if (source == angleSpin) {
            changeOrient(((Double)angleSpin.getValue()).doubleValue());
        } else if (source == sizeSpin) {
            changeSize(((Integer)sizeSpin.getValue()).intValue());
        }
    }

    public void mousePressed(MouseEvent e) {
        Object source = e.getSource();
        if (source == ci1 && editableFillColor) {
            changeFillColor(JColorChooser.showDialog(this, "Choose a New Fill Color", fillColor));
        } else if (source == ci2 && editableBorderColor) {
            changeBorderColor(JColorChooser.showDialog(this, "Choose a New Border Color", borderColor));
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mouseReleased(MouseEvent e) {
    }

    static void buildConstraints(GridBagConstraints gbc, int gx, int gy, int gw, int gh, int wx, int wy) {
        gbc.gridx = gx;
        gbc.gridy = gy;
        gbc.gridwidth = gw;
        gbc.gridheight = gh;
        gbc.weightx = wx;
        gbc.weighty = wy;
    }

    static class Closer extends WindowAdapter {

        public void windowClosing(WindowEvent e) {
            Window w = e.getWindow();
            w.setVisible(false);
        }

    }

    static class DisposeOnClose extends ComponentAdapter {

        public void componentHidden(ComponentEvent e) {
            Window w = (Window)e.getComponent();
            w.dispose();
        }

    }
}
