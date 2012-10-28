/*
 * AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 * Copyright (c) INRIA, 2011. All Rights Reserved
 * Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id: Viewer.java 4311 2011-03-04 12:33:50Z epietrig $
 */
package fr.inria.zvtm.svg;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.EView;
import fr.inria.zvtm.engine.SwingWorker;
import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.glyphs.Glyph;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.xerces.dom.DOMImplementationImpl;
import org.apache.xml.serialize.DOMSerializer;
import org.apache.xml.serialize.LineSeparator;
import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.openide.util.Exceptions;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class Viewer {

    /*
     * screen dimensions, actual dimensions of windows
     */
    static int SCREEN_WIDTH = Toolkit.getDefaultToolkit().getScreenSize().width;
    static int SCREEN_HEIGHT = Toolkit.getDefaultToolkit().getScreenSize().height;
    static int VIEW_MAX_W = 1024;  // 1400
    static int VIEW_MAX_H = 768;   // 1050
    int VIEW_W, VIEW_H;
    int VIEW_X, VIEW_Y;
    /*
     * dimensions of zoomable panel
     */
    int panelWidth, panelHeight;
    VirtualSpaceManager vsm;
    VirtualSpace svgSpace, aboutSpace;
    EView mView;
    MainEventHandler eh;
    Navigation nm;
    Overlay ovm;
    VWGlassPane gp;
    File SCENE_FILE, SCENE_FILE_DIR;

    /*
     * --------------- init ------------------
     */
    public Viewer(File svgF, boolean fullscreen, boolean opengl, boolean antialiased) {
        init();
        initGUI(fullscreen, opengl, antialiased);
        if (svgF != null) {
            loadSVG(svgF);
        }
    }

    final void init() {
        // parse properties
        Scanner sc = new Scanner(Viewer.class.getResourceAsStream("/properties")).useDelimiter("\\s*=\\s*");
        while (sc.hasNext()) {
            String token = sc.next();
            if (token.equals("version")) {
                Messages.VERSION = sc.next();
            }
        }
    }

    final void initGUI(boolean fullscreen, boolean opengl, boolean antialiased) {
        windowLayout();
        Glyph.setDefaultCursorInsideHighlightColor(Config.HIGHLIGHT_COLOR);
        vsm = VirtualSpaceManager.INSTANCE;
        ovm = new Overlay(this);
        nm = new Navigation(this);
        svgSpace = vsm.addVirtualSpace(Messages.svgSpaceName);
        Camera mCamera = svgSpace.addCamera();
        mCamera.setZoomFloor(-99.0);
        nm.ovCamera = svgSpace.addCamera();
        aboutSpace = vsm.addVirtualSpace(Messages.aboutSpaceName);
        aboutSpace.addCamera();
        List<Camera> cameras = new ArrayList<Camera>();
        cameras.add(mCamera);
        nm.setCamera(mCamera);
        cameras.add(aboutSpace.getCamera(0));
        mView = (EView)vsm.addFrameView(cameras, Messages.mViewName, (opengl) ? View.OPENGL_VIEW : View.STD_VIEW, VIEW_W, VIEW_H,
                                        false, false, !fullscreen, (!fullscreen) ? Config.initMenu(this) : null);
        if (fullscreen) {
            GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().setFullScreenWindow((JFrame)mView.getFrame());
        } else {
            mView.setVisible(true);
        }
        updatePanelSize();
        ovm.init();
        gp = new VWGlassPane(this);
        ((JFrame)mView.getFrame()).setGlassPane(gp);
        eh = new MainEventHandler(this);
        mView.setListener(eh, 0);
        mView.setListener(ovm, 1);
        mView.setNotifyCursorMoved(true);
        mView.setAntialiasing(antialiased);
        mView.setBackgroundColor(Config.BACKGROUND_COLOR);
        mView.getPanel().getComponent().addComponentListener(eh);
        ComponentAdapter ca0 = new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                updatePanelSize();
            }

        };
        mView.getFrame().addComponentListener(ca0);
        nm.createOverview();
    }

    void windowLayout() {
        if (Utils.osIsWindows()) {
            VIEW_X = VIEW_Y = 0;
        } else if (Utils.osIsMacOS()) {
            VIEW_X = 80;
            SCREEN_WIDTH -= 80;
        }
        VIEW_W = (SCREEN_WIDTH <= VIEW_MAX_W) ? SCREEN_WIDTH : VIEW_MAX_W;
        VIEW_H = (SCREEN_HEIGHT <= VIEW_MAX_H) ? SCREEN_HEIGHT : VIEW_MAX_H;
    }

    void updatePanelSize() {
        Dimension d = mView.getPanel().getComponent().getSize();
        panelWidth = d.width;
        panelHeight = d.height;
        nm.updateOverviewLocation();
    }

    /*
     * --------------- SVG Parsing ------------------
     */
    void reset() {
        svgSpace.removeAllGlyphs();
    }

    void openFile() {
        final JFileChooser fc = new JFileChooser(SCENE_FILE_DIR);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setDialogTitle("Find SVG File");
        int returnVal = fc.showOpenDialog(mView.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final SwingWorker worker = new SwingWorker() {

                @Override
                public Object construct() {
                    reset();
                    loadSVG(fc.getSelectedFile());
                    return null;
                }

            };
            worker.start();
        }
    }

    void reload() {
        if (SCENE_FILE == null) {
            return;
        }
        final SwingWorker worker = new SwingWorker() {

            @Override
            public Object construct() {
                reset();
                loadSVG(SCENE_FILE);
                return null;
            }

        };
        worker.start();
    }

    static final String LOAD_EXTERNAL_DTD_URL = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    final void loadSVG(File svgF) {
        gp.setVisible(true);
        gp.setValue(20);
        gp.setLabel(Messages.LOADING + svgF.getName());
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setAttribute(LOAD_EXTERNAL_DTD_URL, Boolean.FALSE);
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            String svgURL = svgF.toURI().toURL().toString();
            Document xmlSVG = builder.parse(svgURL);
            gp.setValue(60);
            SVGReader.load(xmlSVG, svgSpace, true, svgURL);
            nm.getGlobalView();
            nm.updateOverview();
        } catch (FactoryConfigurationError e) {
            Exceptions.printStackTrace(e);
        } catch (ParserConfigurationException e) {
            Exceptions.printStackTrace(e);
        } catch (SAXException e) {
            Exceptions.printStackTrace(e);
        } catch (MalformedURLException e) {
            Exceptions.printStackTrace(e);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
        SCENE_FILE = svgF;
        SCENE_FILE_DIR = SCENE_FILE.getParentFile();
        mView.setTitle(Messages.mViewName + " - " + SCENE_FILE.getName());
        gp.setVisible(false);
    }

    /*
     * --------------- SVG exporting ------------------
     */
    static final String SVG_OUTPUT_ENCODING = "UTF-8";

    void export() {
        final JFileChooser fc = new JFileChooser(SCENE_FILE_DIR);
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fc.setDialogTitle("Export SVG File");
        int returnVal = fc.showSaveDialog(mView.getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            final SwingWorker worker = new SwingWorker() {

                @Override
                public Object construct() {
                    exportSVG(fc.getSelectedFile());
                    return null;
                }

            };
            worker.start();
        }
    }

    void exportSVG(File f) {
        SVGWriter sw = new SVGWriter();
        if (f.exists()) {
            f.delete();
        }
        Document d = sw.exportVirtualSpace(svgSpace, new DOMImplementationImpl(), f);
        OutputFormat format = new OutputFormat(d, SVG_OUTPUT_ENCODING, true);
        format.setLineSeparator(LineSeparator.Web);
        try {
            OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(f), SVG_OUTPUT_ENCODING);
            DOMSerializer serializer = (new XMLSerializer(osw, format)).asDOMSerializer();
            serializer.serialize(d);
        } catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }

    /*
     * --------------- Main/exit ------------------
     */
    void exit() {
        System.exit(0);
    }

    public static void main(String[] args) {
        boolean fs = false;
        boolean ogl = false;
        boolean aa = true;
        File svgF = null;
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                if (args[i].substring(1).equals("fs")) {
                    fs = true;
                } else if (args[i].substring(1).equals("opengl")) {
                    System.setProperty("sun.java2d.opengl", "true");
                    ogl = true;
                } else if (args[i].substring(1).equals("noaa")) {
                    aa = false;
                } else if (args[i].substring(1).equals("h") || args[i].substring(1).equals("-help")) {
                    Messages.printCmdLineHelp();
                    System.exit(0);
                }
            } else {
                File f = new File(args[i]);
                if (f.exists()) {
                    svgF = f;
                }
            }
        }
        if (!fs && Utils.osIsMacOS()) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
        }
        System.out.println(Messages.H_4_HELP);
        new Viewer(svgF, fs, ogl, aa);
    }

}
