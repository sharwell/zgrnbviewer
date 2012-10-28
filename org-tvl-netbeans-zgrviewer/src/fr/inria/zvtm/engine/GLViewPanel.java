/*
 * FILE: GLViewPanel.java DATE OF CREATION: Tue Oct 12 09:10:47 2004 AUTHOR :
 * Emmanuel Pietriga (emmanuel.pietriga@inria.fr) MODIF: Emmanuel Pietriga
 * (emmanuel.pietriga@inria.fr) Copyright (c) INRIA, 2004-2011. All Rights
 * Reserved Licensed under the GNU LGPL. For full terms see the file COPYING.
 * $Id: GLViewPanel.java 4590 2011-08-26 13:38:11Z epietrig $
 */
package fr.inria.zvtm.engine;

import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.VText;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.image.BufferedImage;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * JPanel used to paint the content of a view (all camera layers). Uses OpenGL
 * acceletation provided by the Java2D OpenGL rendering pipeline available since
 * J2SE 5.0 (Linux and Windows, not Mac OS X). The use of GLViewPanel requires
 * the following Java property: -Dsun.java2d.opengl=true
 *
 * @author Emmanuel Pietriga
 */
public class GLViewPanel extends ViewPanel {

    protected JPanel panel;

    /**
     * Get the underlying Swing component.
     *
     * @return the underlying JPanel
     */
    @Override
    public Component getComponent() {
        return panel;
    }

    Dimension oldSize;
    Timer edtTimer;

    @SuppressWarnings("LeakingThisInConstructor")
    GLViewPanel(List<Camera> cameras, View v, boolean arfome) {
        panel = new PanelImpl();

        ActionListener taskPerformer = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                panel.repaint();
            }

        };
        edtTimer = new Timer(25, taskPerformer);
        panel.addHierarchyListener(
            new HierarchyListener() {

            @Override
                public void hierarchyChanged(HierarchyEvent e) {
                    if (panel.isShowing()) {
                        start();
                    } else {
                        stop();
                    }
                }

            });
        parent = v;
        //init of camera array
        cams = new Camera[cameras.size()];  //array of Camera
        evHs = new ViewListener[cams.length];
        for (int nbcam = 0; nbcam < cameras.size(); nbcam++) {
            cams[nbcam] = cameras.get(nbcam);
        }
        //init other stuff
        panel.setBackground(backColor);
        panel.addMouseListener(this);
        panel.addMouseMotionListener(this);
        panel.addMouseWheelListener(this);
        panel.addComponentListener(this);
        setAutoRequestFocusOnMouseEnter(arfome);
        setAWTCursor(Cursor.CUSTOM_CURSOR);  //custom cursor means VTM cursor
        this.size = panel.getSize();
        if (VirtualSpaceManager.debugModeON()) {
            System.out.println("View refresh time set to " + getRefreshRate() + "ms");
        }
        start();
    }

    private void start() {
        size = panel.getSize();
        oldSize = size;
        edtTimer.start();
    }

    @Override
    void stop() {
        edtTimer.stop();
    }

    public void paint(Graphics g) {
        // stableRefToBackBufferGraphics is used here not as a Graphics from a back buffer image, but directly as the OpenGL graphics context
        // (simply reusing an already declared var instead of creating a new one for nothing)
        stableRefToBackBufferGraphics = (Graphics2D)g;
        try {
            updateCursorOnly = false;
            size = panel.getSize();
            if (size.width != oldSize.width || size.height != oldSize.height) {
                if (VirtualSpaceManager.debugModeON()) {
                    System.out.println("Resizing JPanel: (" + oldSize.width + "x" + oldSize.height + ") -> (" + size.width + "x" + size.height + ")");
                }
                oldSize = size;
                updateAntialias = true;
                updateFont = true;
            }
            if (updateFont) {
                stableRefToBackBufferGraphics.setFont(VText.getMainFont());
                updateFont = false;
            }
            if (updateAntialias) {
                if (antialias) {
                    stableRefToBackBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                } else {
                    stableRefToBackBufferGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                }
                updateAntialias = false;
            }
            standardStroke = stableRefToBackBufferGraphics.getStroke();
            standardTransform = stableRefToBackBufferGraphics.getTransform();
            if (notBlank) {
                if (repaintASAP || updateCursorOnly) {
                    repaintASAP = false; //do this first as the thread can be interrupted inside
                    //this branch and we want to catch new requests for repaint
                    updateCursorOnly = false;
                    stableRefToBackBufferGraphics.setPaintMode();
                    stableRefToBackBufferGraphics.setBackground(backColor);
                    stableRefToBackBufferGraphics.clearRect(0, 0, panel.getWidth(), panel.getHeight());
                    backgroundHook();
                    //begin actual drawing here
                    for (int nbcam = 0; nbcam < cams.length; nbcam++) {
                        if ((cams[nbcam] != null) && (cams[nbcam].enabled) && ((cams[nbcam].eager) || (cams[nbcam].shouldRepaint()))) {
                            camIndex = cams[nbcam].getIndex();
                            drawnGlyphs = cams[nbcam].parentSpace.getDrawnGlyphs(camIndex);
                            drawnGlyphs.clear();
                            double uncoef = (cams[nbcam].focal + cams[nbcam].altitude) / cams[nbcam].focal;
                            //compute region seen from this view through camera
                            double viewW = size.width;
                            double viewH = size.height;
                            double viewWC = cams[nbcam].vx - (viewW / 2 - visibilityPadding[0]) * uncoef;
                            double viewNC = cams[nbcam].vy + (viewH / 2 - visibilityPadding[1]) * uncoef;
                            double viewEC = cams[nbcam].vx + (viewW / 2 - visibilityPadding[2]) * uncoef;
                            double viewSC = cams[nbcam].vy - (viewH / 2 - visibilityPadding[3]) * uncoef;
                            gll = cams[nbcam].parentSpace.getDrawingList();
                            for (int i = 0; i < gll.length; i++) {
                                if (gll[i].visibleInViewport(viewWC, viewNC, viewEC, viewSC, cams[nbcam])) {
                                    //if glyph is at least partially visible in the reg. seen from this view, display
                                    gll[i].project(cams[nbcam], size);
                                    if (gll[i].isVisible()) {
                                        gll[i].draw(stableRefToBackBufferGraphics, size.width, size.height, cams[nbcam].getIndex(), standardStroke, standardTransform, 0, 0);
                                    }
                                    // notifying outside if branch because glyph sensitivity is not
                                    // affected by glyph visibility when managed through Glyph.setVisible()
                                    cams[nbcam].parentSpace.drewGlyph(gll[i], camIndex);
                                }
                            }
                        }
                    }
                    foregroundHook();
                    afterLensHook();
                    drawPortals();
                    portalsHook();
                    if (cursor_inside) {
                        //deal with mouse glyph only if mouse cursor is inside this window
                        try {
                            //we project the mouse cursor wrt the appropriate coord sys
                            parent.mouse.unProject(cams[activeLayer], this);
                            if (parent.mouse.isSensitive()) {
                                parent.mouse.getPicker().computePickedGlyphList(evHs[activeLayer], cams[activeLayer]);
                            }
                        } catch (NullPointerException ex) {
                            if (VirtualSpaceManager.debugModeON()) {
                                System.err.println("viewpanel.run.drawdrag " + ex);
                            }
                        }
                        stableRefToBackBufferGraphics.setColor(parent.mouse.hcolor);
                        if (drawDrag) {
                            stableRefToBackBufferGraphics.drawLine(origDragx, origDragy, parent.mouse.jpx, parent.mouse.jpy);
                        }
                        if (drawRect) {
                            stableRefToBackBufferGraphics.drawRect(Math.min(origDragx, parent.mouse.jpx), Math.min(origDragy, parent.mouse.jpy), Math.max(origDragx, parent.mouse.jpx) - Math.min(origDragx, parent.mouse.jpx), Math.max(origDragy, parent.mouse.jpy) - Math.min(origDragy, parent.mouse.jpy));
                        }
                        if (drawOval) {
                            if (circleOnly) {
                                stableRefToBackBufferGraphics.drawOval(origDragx - Math.abs(origDragx - parent.mouse.jpx), origDragy - Math.abs(origDragx - parent.mouse.jpx), 2 * Math.abs(origDragx - parent.mouse.jpx), 2 * Math.abs(origDragx - parent.mouse.jpx));
                            } else {
                                stableRefToBackBufferGraphics.drawOval(origDragx - Math.abs(origDragx - parent.mouse.jpx), origDragy - Math.abs(origDragy - parent.mouse.jpy), 2 * Math.abs(origDragx - parent.mouse.jpx), 2 * Math.abs(origDragy - parent.mouse.jpy));
                            }
                        }
                        if (sfopw) {
                            stableRefToBackBufferGraphics.drawImage(FIRST_ORDER_PAN_WIDGET, fopw_x, fopw_y, null);
                        }
                        if (drawVTMcursor) {
                            parent.mouse.draw(stableRefToBackBufferGraphics);
                            oldX = parent.mouse.jpx;
                            oldY = parent.mouse.jpy;
                        }
                    }
                    //end drawing here
                }
            } else {
                stableRefToBackBufferGraphics.setPaintMode();
                stableRefToBackBufferGraphics.setColor(blankColor);
                stableRefToBackBufferGraphics.fillRect(0, 0, panel.getWidth(), panel.getHeight());
                portalsHook();
            }
        } catch (NullPointerException ex0) {
            if (VirtualSpaceManager.debugModeON()) {
                System.err.println("GLViewPanel.paint " + ex0);
            }
        }
        if (repaintListener != null) {
            repaintListener.viewRepainted(this.parent);
        }
    }

    @Override
    public void setRefreshRate(int rr) {
        if (rr > 0) {
            edtTimer.setDelay(rr);
        }
    }

    @Override
    public int getRefreshRate() {
        return edtTimer.getDelay();
    }

    /**
     * Not implemented yet.
     */
    @Override
    public BufferedImage getImage() {
        return null;
    }

    private class PanelImpl extends JPanel {

        public PanelImpl() {
        }

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            GLViewPanel.this.paint(g);
        }
    }

}
