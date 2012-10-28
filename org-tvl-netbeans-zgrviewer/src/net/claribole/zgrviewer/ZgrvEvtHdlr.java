/*
 * FILE: ZgrvEvtHdlr.java DATE OF CREATION: Thu Jan 09 15:18:48 2003 Copyright
 * (c) 2003 World Wide Web Consortium. All Rights Reserved Copyright (c) INRIA,
 * 2004-2011. All Rights Reserved Licensed under the GNU LGPL. For full terms
 * see the file COPYING.
 *
 * $Id: ZgrvEvtHdlr.java 4607 2011-10-06 12:49:37Z epietrig $
 */
package net.claribole.zgrviewer;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.IdentityInterpolator;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VCursor;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VText;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.List;

public class ZgrvEvtHdlr extends BaseEventHandler implements ViewListener {

    protected ZGRApplication application;
    protected GraphicsManager grMngr;
    protected double mvx, mvy;
    boolean editingSpline = false;
    boolean movingEdgeLabel = false;
    boolean movingNode = false;

    public ZgrvEvtHdlr(ZGRApplication app, GraphicsManager gm) {
        this.application = app;
        this.grMngr = gm;
    }

    @Override
    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
        if (toolPaletteIsActive) {
            return;
        }
        lastJPX = jpx;
        lastJPY = jpy;
        Glyph g = v.lastGlyphEntered();
        if (inZoomWindow) {
            if (grMngr.dmPortal.coordInsideBar(jpx, jpy)) {
                draggingZoomWindow = true;
            } else {
                draggingZoomWindowContent = true;
            }
        } else if (inMagWindow) {
            v.getVCursor().stickGlyph(grMngr.magWindow);
            draggingMagWindow = true;
        } else if (grMngr.getToolPalette().isBringAndGoMode() && g != null) {
            grMngr.startBringAndGo(g);
        } else if (grMngr.getToolPalette().isLinkSlidingMode()) {
            Point location = e.getComponent().getLocationOnScreen();
            relative = e.getPoint();
            LS_SX = v.getVCursor().getVSXCoordinate();
            LS_SY = v.getVCursor().getVSYCoordinate();
            grMngr.attemptLinkSliding(LS_SX, LS_SY, location.x, location.y);
        } else if (grMngr.getToolPalette().isEditMode()) {
            if (g != null) {
                // moving edge control point
                if (g.getType() != null && g.getType().equals(GeometryEditor.SPLINE_GEOM_EDITOR)) {
                    editingSpline = true;
                    v.getVCursor().stickGlyph(g);
                } else {
                    // moving something else
                    grMngr.geom.clearSplineEditingGlyphs();
                    if (g instanceof VText && g.getOwner() != null && g.getOwner() instanceof LEdge) {
                        // moving an edge label
                        movingEdgeLabel = true;
                        v.getVCursor().stickGlyph(g);
                    } else if (g.getOwner() != null && g.getOwner() instanceof LNode) {
                        // moving a node (label of shape)
                        movingNode = true;
                        grMngr.geom.stickNodeComponents(g, (LNode)g.getOwner());
                        v.getVCursor().stickGlyph(g);
                    } else {
                        // might be attempting to edit an edge
                        attemptEditEdge(v);
                    }
                }
            } else {
                // might be attempting to edit an edge
                grMngr.geom.clearSplineEditingGlyphs();
                attemptEditEdge(v);
            }
        } else {
            grMngr.rememberLocation(v.cams[0].getLocation());
            if (mod == NO_MODIFIER || mod == SHIFT_MOD || mod == META_MOD || mod == META_SHIFT_MOD) {
                manualLeftButtonMove = true;
                lastJPX = jpx;
                lastJPY = jpy;
                //grMngr.vsm.setActiveCamera(v.cams[0]);
                v.showFirstOrderPanWidget(jpx, jpy);
                //v.setDrawDrag(true);
                // because we would not be consistent
                // (when dragging the mouse, we computeMouseOverList, but if there is an anim triggered by {X,Y,A}speed,
                // and if the mouse is not moving, this list is not computed
                // so here we choose to disable this computation when dragging the mouse with button 3 pressed)
                v.getVCursor().setSensitivity(false);

                activeCam = grMngr.vsm.getActiveCamera();
            } else if (mod == ALT_MOD) {
                zoomingInRegion = true;
                x1 = v.getVCursor().getVSXCoordinate();
                y1 = v.getVCursor().getVSYCoordinate();
                v.setDrawRect(true);
            }
        }
    }

    @Override
    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
        if (ConfigManager.DYNASPOT && !toolPaletteIsActive && !v.getVCursor().getDynaPicker().isDynaSpotActivated()) {
            grMngr.activateDynaSpot(true, false);
        }
        if (grMngr.isBringingAndGoing) {
            grMngr.endBringAndGo(v.lastGlyphEntered());
        } else if (grMngr.isLinkSliding) {
            grMngr.endLinkSliding();
        }
        if (toolPaletteIsActive) {
            return;
        }
        draggingZoomWindow = false;
        draggingZoomWindowContent = false;
        if (editingSpline || movingEdgeLabel) {
            v.getVCursor().unstickLastGlyph();
            editingSpline = movingEdgeLabel = false;
        } else if (movingNode) {
            v.getVCursor().unstickLastGlyph();
            grMngr.geom.unstickAll();
            movingNode = false;
        }
        if (draggingMagWindow) {
            draggingMagWindow = false;
            v.getVCursor().unstickLastGlyph();
        }
        if (zoomingInRegion) {
            v.setDrawRect(false);
            x2 = v.getVCursor().getVSXCoordinate();
            y2 = v.getVCursor().getVSYCoordinate();
            if ((Math.abs(x2 - x1) >= 4) && (Math.abs(y2 - y1) >= 4)) {
                grMngr.mainView.centerOnRegion(grMngr.vsm.getActiveCamera(), ConfigManager.ANIM_MOVE_LENGTH, x1, y1, x2, y2);
            }
            zoomingInRegion = false;
        } else if (manualLeftButtonMove) {
            grMngr.mainCamera.setXspeed(0);
            grMngr.mainCamera.setYspeed(0);
            grMngr.mainCamera.setZspeed(0);
            v.hideFirstOrderPanWidget();
            //v.setDrawDrag(false);
            v.getVCursor().setSensitivity(true);
            if (autoZooming) {
                unzoom(v);
            }
            manualLeftButtonMove = false;
        }
    }

    @Override
    public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {
        if (toolPaletteIsActive) {
            if (v.lastGlyphEntered() != null) {
                grMngr.getToolPalette().selectButton((VImage)v.lastGlyphEntered());
            }
        } else {
            if (grMngr.getToolPalette().isBringAndGoMode()) {
                return;
            }
            if (grMngr.getToolPalette().isFadingLensNavMode() || grMngr.getToolPalette().isProbingLensNavMode()) {
                lastJPX = jpx;
                lastJPY = jpy;
                lastVX = v.getVCursor().getVSXCoordinate();
                lastVY = v.getVCursor().getVSYCoordinate();
                if (grMngr.lensType != GraphicsManager.NO_LENS) {
                    grMngr.zoomInPhase2(lastVX, lastVY);
                } else {
                    if (cursorNearBorder) {
                        // do not activate the lens when cursor is near the border
                        return;
                    }
                    grMngr.zoomInPhase1(jpx, jpy);
                }
            } else if (grMngr.getToolPalette().isDragMagNavMode()) {
                grMngr.triggerDM(jpx, jpy, this);
            } else if (!grMngr.getToolPalette().isEditMode()) {
                if (clickNumber == 2) {
                    click2(v, mod, jpx, jpy, clickNumber, e);
                } else {
                    Glyph g = v.lastGlyphEntered();
                    if (mod == SHIFT_MOD) {
                        grMngr.highlightElement(g, v.cams[0], v.getVCursor(), true);
                    } else {
                        if (g != null && g != grMngr.boundingBox) {
                            grMngr.mainView.centerOnGlyph(g, v.cams[0], ConfigManager.ANIM_MOVE_LENGTH, true, ConfigManager.MAG_FACTOR);
                        }
                    }
                }

            }
        }
    }

    Glyph startG, endG;
    LEdge edge;

    @Override
    public void press2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
        if (grMngr.getToolPalette().isEditMode()) {
            if (startG != null) {
                endG = v.lastGlyphEntered();
                edge = grMngr.addEdge(startG, endG, "test", true);
                startG = endG = null;
            } else {
                startG = v.lastGlyphEntered();
            }
        }
        //else if (mod == ALT_MOD){
        //	grMngr.removeEdge(edge);
        //}
    }

    @Override
    public void release2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {
        if (toolPaletteIsActive) {
            return;
        }
        Glyph g = v.lastGlyphEntered();
        if (g != null && g != grMngr.boundingBox) {
            if (g.getOwner() != null) {
                getAndDisplayURL((LElem)g.getOwner(), g);
            }
        } else {
            attemptDisplayEdgeURL(v.getVCursor(), v.cams[0]);
        }
    }

    @Override
    public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
        if (ConfigManager.DYNASPOT) {
            grMngr.activateDynaSpot(false, false);
        }
        if (toolPaletteIsActive) {
            return;
        }
        if (grMngr.getToolPalette().isFadingLensNavMode() || grMngr.getToolPalette().isProbingLensNavMode()) {
            lastJPX = jpx;
            lastJPY = jpy;
        } else {
            v.parent.setActiveLayer(1);
            throw new UnsupportedOperationException("Not implemented yet.");
//			application.displayMainPieMenu(true);
        }
    }

    @Override
    public void release3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
        if (ConfigManager.DYNASPOT) {
            grMngr.activateDynaSpot(true, false);
        }
        if (toolPaletteIsActive) {
            return;
        }
        Glyph g = v.getVCursor().getPicker().lastGlyphEntered();
        if (g != null && ZGRMessages.PM_ENTRY().equals(g.getType())) {
            throw new UnsupportedOperationException("Not implemented yet.");
//			application.pieMenuEvent(g);
        }
//		if (application.mainPieMenu != null){
//			application.displayMainPieMenu(false);
//		}
//		if (application.subPieMenu != null){
//			application.displaySubMenu(null, false);
//		}
        v.parent.setActiveLayer(0);
    }

    @Override
    public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {
        if (toolPaletteIsActive) {
            return;
        }
        if (grMngr.getToolPalette().isFadingLensNavMode() || grMngr.getToolPalette().isProbingLensNavMode()) {
            lastJPX = jpx;
            lastJPY = jpy;
            lastVX = v.getVCursor().getVSXCoordinate();
            lastVY = v.getVCursor().getVSYCoordinate();
            if (grMngr.lensType != GraphicsManager.NO_LENS) {
                grMngr.zoomOutPhase2();
            } else {
                if (cursorNearBorder) {
                    // do not activate the lens when cursor is near the border
                    return;
                }
                grMngr.zoomOutPhase1(jpx, jpy, lastVX, lastVY);
            }
        }
    }

    @Override
    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e) {
        lx = jpx;
        ly = jpy;
        if ((jpx - GraphicsManager.LENS_R1) < 0) {
            lx = GraphicsManager.LENS_R1;
            cursorNearBorder = true;
        } else if ((jpx + GraphicsManager.LENS_R1) > grMngr.panelWidth) {
            lx = grMngr.panelWidth - GraphicsManager.LENS_R1;
            cursorNearBorder = true;
        } else {
            cursorNearBorder = false;
        }
        if ((jpy - GraphicsManager.LENS_R1) < 0) {
            ly = GraphicsManager.LENS_R1;
            cursorNearBorder = true;
        } else if ((jpy + GraphicsManager.LENS_R1) > grMngr.panelHeight) {
            ly = grMngr.panelHeight - GraphicsManager.LENS_R1;
            cursorNearBorder = true;
        }
        if (grMngr.lensType != 0 && grMngr.lens != null) {
            grMngr.moveLens(lx, ly, e.getWhen());
        } else if (grMngr.getToolPalette().isEnabled()) {
            if (grMngr.getToolPalette().insidePaletteTriggerZone(jpx, jpy)) {
                if (!grMngr.getToolPalette().isShowing()) {
                    grMngr.getToolPalette().show();
                    toolPaletteIsActive = true;
                }
            } else {
                if (grMngr.getToolPalette().isShowing()) {
                    grMngr.getToolPalette().hide();
                    toolPaletteIsActive = false;
                }
            }
        }
        if (ConfigManager.DYNASPOT) {
            v.getVCursor().getDynaPicker().dynaPick(grMngr.mainCamera);
        }
    }

    @Override
    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e) {
        if (toolPaletteIsActive || grMngr.isBringingAndGoing) {
            return;
        }
        if (v.getVCursor().getDynaPicker().isDynaSpotActivated()) {
            grMngr.activateDynaSpot(false, false);
        }
        if (editingSpline) {
            grMngr.geom.updateEdgeSpline();
        } else if (movingEdgeLabel || movingNode) {
            // do nothing but prevent exec of else
            return;
        } else if (grMngr.isLinkSliding) {
            // ignore events triggered by AWT robot
            grMngr.linkSlider(v.getVCursor().getVSXCoordinate(), v.getVCursor().getVSYCoordinate(), false);
        } else if (mod != ALT_MOD && buttonNumber == 1) {
            if (draggingZoomWindow) {
                grMngr.dmPortal.move(jpx - lastJPX, jpy - lastJPY);
                lastJPX = jpx;
                lastJPY = jpy;
                grMngr.vsm.repaint();
            } else if (draggingZoomWindowContent) {
                tfactor = (grMngr.dmCamera.focal + (grMngr.dmCamera.altitude)) / grMngr.dmCamera.focal;
                synchronized (grMngr.dmCamera) {
                    grMngr.dmCamera.move(Math.round(tfactor * (lastJPX - jpx)),
                                         Math.round(tfactor * (jpy - lastJPY)));
                    lastJPX = jpx;
                    lastJPY = jpy;
                    grMngr.updateMagWindow();
                }
            } else if (draggingMagWindow) {
                grMngr.updateZoomWindow();
            } else {
                if (mod == SHIFT_MOD || mod == META_SHIFT_MOD) {
                    grMngr.mainCamera.setXspeed(0);
                    grMngr.mainCamera.setYspeed(0);
                    grMngr.mainCamera.setZspeed((lastJPY - jpy) * ZOOM_SPEED_COEF);
                } else {
                    tfactor = (activeCam.focal + Math.abs(activeCam.altitude)) / activeCam.focal;
                    jpxD = jpx - lastJPX;
                    jpyD = lastJPY - jpy;
                    grMngr.mainCamera.setXspeed((activeCam.altitude > 0) ? jpxD * (tfactor / PAN_SPEED_FACTOR) : jpxD / (tfactor * PAN_SPEED_FACTOR));
                    grMngr.mainCamera.setYspeed((activeCam.altitude > 0) ? jpyD * (tfactor / PAN_SPEED_FACTOR) : jpyD / (tfactor * PAN_SPEED_FACTOR));
                    grMngr.mainCamera.setZspeed(0);
                    if (ConfigManager.isSDZoomEnabled()) {
                        dragValue = Math.sqrt(Math.pow(jpxD, 2) + Math.pow(jpyD, 2));
                        if (!autoZooming && dragValue > ConfigManager.SD_ZOOM_THRESHOLD) {
                            autoZooming = true;
                            Animation a = grMngr.vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(300, v.cams[0],
                                                                                                                     new Float(ConfigManager.autoZoomFactor * (v.cams[0].getAltitude() + v.cams[0].getFocal())), true,
                                                                                                                     IdentityInterpolator.getInstance(), null);
                            grMngr.vsm.getAnimationManager().startAnimation(a, false);
                        }
                    }
                }
            }
        }
        if (grMngr.lensType != GraphicsManager.NO_LENS && grMngr.lens != null) {
            grMngr.moveLens(jpx, jpy, e.getWhen());
        }
    }

    @Override
    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e) {
        if (grMngr.lensType != GraphicsManager.NO_LENS && grMngr.lens != null) {
            if (wheelDirection == ViewListener.WHEEL_UP) {
                grMngr.magnifyFocus(GraphicsManager.WHEEL_MM_STEP, grMngr.lensType, grMngr.mainCamera);
            } else {
                grMngr.magnifyFocus(-GraphicsManager.WHEEL_MM_STEP, grMngr.lensType, grMngr.mainCamera);
            }
        } else if (inZoomWindow) {
            tfactor = (grMngr.dmCamera.focal + Math.abs(grMngr.dmCamera.altitude)) / grMngr.dmCamera.focal;
            if (wheelDirection == WHEEL_UP) {
                // zooming in
                grMngr.dmCamera.altitudeOffset(-tfactor * WHEEL_ZOOMOUT_FACTOR);
            } else {
                // wheelDirection == WHEEL_DOWN, zooming out
                grMngr.dmCamera.altitudeOffset(tfactor * WHEEL_ZOOMIN_FACTOR);
            }
            grMngr.updateMagWindow();
            grMngr.vsm.repaint();
        } else {
            tfactor = (grMngr.mainCamera.focal + Math.abs(grMngr.mainCamera.altitude)) / grMngr.mainCamera.focal;
            mvx = v.getVCursor().getVSXCoordinate();
            mvy = v.getVCursor().getVSYCoordinate();
            if (wheelDirection == WHEEL_UP) {
                // zooming out
                grMngr.mainCamera.vx -= Math.round((mvx - grMngr.mainCamera.vx) * WHEEL_ZOOMOUT_FACTOR / grMngr.mainCamera.focal);
                grMngr.mainCamera.vy -= Math.round((mvy - grMngr.mainCamera.vy) * WHEEL_ZOOMOUT_FACTOR / grMngr.mainCamera.focal);
                grMngr.mainCamera.altitudeOffset(tfactor * WHEEL_ZOOMOUT_FACTOR);
                grMngr.cameraMoved(null, null, 0);
            } else {
                // wheelDirection == WHEEL_DOWN, zooming in
                if (grMngr.mainCamera.getAltitude() > -90) {
                    grMngr.mainCamera.vx += Math.round((mvx - grMngr.mainCamera.vx) * WHEEL_ZOOMIN_FACTOR / grMngr.mainCamera.focal);
                    grMngr.mainCamera.vy += Math.round((mvy - grMngr.mainCamera.vy) * WHEEL_ZOOMIN_FACTOR / grMngr.mainCamera.focal);
                }
                grMngr.mainCamera.altitudeOffset(-tfactor * WHEEL_ZOOMIN_FACTOR);
                grMngr.cameraMoved(null, null, 0);
            }
        }
    }

    @Override
    public void enterGlyph(Glyph g) {
        grMngr.mainView.setStatusBarText(" ");
        if (g == grMngr.magWindow) {
            inMagWindow = true;
            return;
        }
        // do not highlight graph's bounding box
        if (g == grMngr.boundingBox) {
            return;
        }
        if (grMngr.vsm.getActiveView().getActiveLayer() == 1) {
            // interacting with pie menu
            g.highlight(true, null);
            VirtualSpace vs = grMngr.vsm.getVirtualSpace(GraphicsManager.menuSpace);
            vs.onTop(g);
            throw new UnsupportedOperationException("Not implemented yet.");
//			int i = fr.inria.zvtm.engine.Utils.indexOfGlyph(application.mainPieMenu.getItems(), g);
//			if (i != -1){
//				vs.onTop(application.mainPieMenu.getLabels()[i]);
//			}
//			else {
//				i = fr.inria.zvtm.engine.Utils.indexOfGlyph(application.subPieMenu.getItems(), g);
//				if (i != -1){
//					vs.onTop(application.subPieMenu.getLabels()[i]);
//				}
//			}
        } else {
            if (g.getType() != null && g.getType().equals(GeometryEditor.SPLINE_GEOM_EDITOR)) {
                grMngr.mainView.setCursorIcon(Cursor.MOVE_CURSOR);
            } else if (grMngr.getToolPalette().isHighlightMode()) {
                grMngr.highlightElement(g, null, null, true);
                // g is guaranteed to be != null, don't care about camera and cursor
            } else {
                // node highlighting is taken care of above (in a slightly different manner)
                g.highlight(true, null);
            }
        }
    }

    @Override
    public void exitGlyph(Glyph g) {
        if (g == grMngr.magWindow) {
            inMagWindow = false;
            return;
        }
        // do not highlight graph's bounding box
        if (g == grMngr.boundingBox) {
            return;
        }
        if (grMngr.vsm.getActiveView().getActiveLayer() == 1) {
            g.highlight(false, null);
            throw new UnsupportedOperationException("Not implemented yet.");
//			if (application.mainPieMenu != null && g == application.mainPieMenu.getBoundary()){
//				Glyph lge = grMngr.vsm.getActiveView().mouse.getPicker().lastGlyphEntered();
//				if (lge != null && lge.getType() == ZGRMessages.PM_SUBMN){
//					application.mainPieMenu.setSensitivity(false);
//					application.displaySubMenu(lge, true);
//				}
//			}
//			else if (application.subPieMenu != null && g == application.subPieMenu.getBoundary()){
//				application.displaySubMenu(null, false);
//				application.mainPieMenu.setSensitivity(true);
//			}
        } else {
            if (g.getType() != null && g.getType().equals(GeometryEditor.SPLINE_GEOM_EDITOR)) {
                grMngr.mainView.setCursorIcon(Cursor.CUSTOM_CURSOR);
            } else if (application.getGraphicsManager().getToolPalette().isHighlightMode()) {
                grMngr.unhighlightAll();
            } else {
                // node highlighting is taken care of above (in a slightly different manner)
                g.highlight(false, null);
            }
        }
    }

    @Override
    public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e) {
    }

    @Override
    public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e) {
        if (code == KeyEvent.VK_PAGE_UP) {
            grMngr.getHigherView();
        } else if (code == KeyEvent.VK_PAGE_DOWN) {
            grMngr.getLowerView();
        } else if (code == KeyEvent.VK_HOME) {
            grMngr.getGlobalView();
        } else if (code == KeyEvent.VK_UP) {
            grMngr.translateView(GraphicsManager.MOVE_DOWN);
        } else if (code == KeyEvent.VK_DOWN) {
            grMngr.translateView(GraphicsManager.MOVE_UP);
        } else if (code == KeyEvent.VK_LEFT) {
            grMngr.translateView(GraphicsManager.MOVE_RIGHT);
        } else if (code == KeyEvent.VK_RIGHT) {
            grMngr.translateView(GraphicsManager.MOVE_LEFT);
        } else if (code == KeyEvent.VK_L || code == KeyEvent.VK_SPACE) {
            Glyph g = v.lastGlyphEntered();
            if (g != null) {
                if (g.getOwner() != null) {
                    getAndDisplayURL((LElem)g.getOwner(), g);
                }
            } else {
                attemptDisplayEdgeURL(v.getVCursor(), v.cams[0]);
            }
        }
    }

    @Override
    public void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e) {
    }

    @Override
    public void viewActivated(View v) {
    }

    @Override
    public void viewDeactivated(View v) {
    }

    @Override
    public void viewIconified(View v) {
    }

    @Override
    public void viewDeiconified(View v) {
    }

    @Override
    public void viewClosing(View v) {
//        application.exit();
    }

    public void attemptDisplayEdgeURL(VCursor mouse, Camera cam) {
        Glyph g;
        List<Glyph> otherGlyphs = mouse.getPicker().getIntersectingGlyphs(cam);
        if (otherGlyphs != null && otherGlyphs.size() > 0) {
            g = otherGlyphs.get(0);
            if (g.getOwner() != null) {
                getAndDisplayURL((LElem)g.getOwner(), g);
            }
        }
    }

    public void getAndDisplayURL(LElem noa, Glyph g) {
        String url = noa.getURL(g);
        if (url != null && url.length() > 0) {
            throw new UnsupportedOperationException("Not implemented yet.");
//			application.displayURLinBrowser(url);
        }
    }

    public void attemptEditEdge(ViewPanel v) {
        List<Glyph> otherGlyphs = v.getVCursor().getPicker().getIntersectingGlyphs(v.cams[0]);
        if (otherGlyphs != null && otherGlyphs.size() > 0) {
            for (Glyph eg : otherGlyphs) {
                if (eg.getOwner() != null && eg.getOwner() instanceof LEdge) {
                    grMngr.geom.editEdgeSpline((LEdge)eg.getOwner());
                }
            }
        }
    }

    /*
     * cancel a speed-dependant autozoom
     */
    protected void unzoom(ViewPanel v) {
        Animation a = grMngr.vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(300, v.cams[0],
                                                                                                 new Float(ConfigManager.autoUnzoomFactor * (v.cams[0].getAltitude() + v.cams[0].getFocal())), true,
                                                                                                 IdentityInterpolator.getInstance(), null);
        grMngr.vsm.getAnimationManager().startAnimation(a, false);
        autoZooming = false;
    }

}
