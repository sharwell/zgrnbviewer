/*
 * FILE: BaseEventHandler.java DATE OF CREATION: Mon Nov 27 08:30:31 2006
 * Copyright (c) INRIA, 2006-2011. All Rights Reserved Licensed under the GNU
 * LGPL. For full terms see the file COPYING.
 *
 * $Id: BaseEventHandler.java 4277 2011-02-28 09:26:31Z epietrig $
 */
package net.claribole.zgrviewer;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.event.PortalListener;
import fr.inria.zvtm.glyphs.VSegment;
import java.awt.Point;

public abstract class BaseEventHandler implements PortalListener {

    protected static final float WHEEL_ZOOMOUT_FACTOR = 21.0f;
    protected static final float WHEEL_ZOOMIN_FACTOR = 22.0f;
    protected static final float ZOOM_SPEED_COEF = 1.0f / 50.0f;
    protected static final float PAN_SPEED_FACTOR = 50.0f;
    protected Camera activeCam;
    protected VSegment navSeg;
    protected boolean cursorNearBorder = false;
    // remember last mouse coords to compute translation  (dragging)
    protected int lastJPX, lastJPY;
    protected double lastVX, lastVY;
    protected int jpxD, jpyD;
    protected double tfactor;
    // remember last mouse coords to display selection rectangle (dragging)
    protected double x1, y1, x2, y2;
    // lens optimization
    protected int lx, ly;
    protected boolean zoomingInRegion = false;
    protected boolean manualLeftButtonMove = false;
    protected boolean manualRightButtonMove = false;

    /*
     * speed-dependant autozoom data
     */
    protected boolean autoZooming = false;
    protected double dragValue;
    protected boolean toolPaletteIsActive = false;

    /*
     * DragMag interaction
     */
    protected boolean inZoomWindow = false;
    protected boolean inMagWindow = false;
    protected boolean draggingMagWindow = false;
    protected boolean draggingZoomWindow = false;
    protected boolean draggingZoomWindowContent = false;
    /*
     * Link Sliding
     */
    protected double LS_SX, LS_SY;
    protected Point relative;

    /**
     * cursor enters portal
     */
    @Override
    public void enterPortal(Portal p) {
        inZoomWindow = true;
    }

    /**
     * cursor exits portal
     */
    @Override
    public void exitPortal(Portal p) {
        inZoomWindow = false;
    }

    protected void resetDragMagInteraction() {
        inMagWindow = false;
        inZoomWindow = false;
        draggingZoomWindow = false;
        draggingZoomWindowContent = false;
    }

}
