/*
 * FILE: PeriodicActionManager.java DATE OF CREATION: Thu Jan 09 14:14:35 2003
 * Copyright (c) Emmanuel Pietriga, 2002. All Rights Reserved Copyright (c)
 * INRIA, 2004-2011. All Rights Reserved Licensed under the GNU LGPL. For full
 * terms see the file COPYING.
 *
 * $Id: PeriodicActionManager.java 4276 2011-02-25 07:47:51Z epietrig $
 */
package net.claribole.zgrviewer;

import fr.inria.zvtm.engine.Java2DPainter;
import fr.inria.zvtm.glyphs.Glyph;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.lang.ref.WeakReference;

class PeriodicActionManager implements Runnable, MouseMotionListener, Java2DPainter {

    static int SLEEP_TIME = 500;  // check for tooltip changes every 1.0s
    static int TOOLTIP_TIME = 1000; // tooltip info should appear only if mouse is idle for at least 1.5s
    static Color TP_BACKGROUND = new Color(255, 255, 147);
    static Color TP_FOREGROUND = Color.black;
    static Font TP_FONT = new Font("Dialog", Font.PLAIN, 10);
    static int TP_PADDING = 4;
    static int TP_MARGIN = 15;

    private boolean invalidBounds = true;
    private WeakReference<GraphicsManager> _grMngr;
    private Thread runTP;
    private long lastMouseMoved = System.currentTimeMillis();
    private Glyph tippedGlyph;
    private String tipLabel;
    private int lX, lY, rX, rY, rW, rH;
    private boolean updatePalette = false;

    PeriodicActionManager(GraphicsManager gm) {
        this._grMngr = new WeakReference<GraphicsManager>(gm);
    }

    public void start() {
        runTP = new Thread(this, "ZGRViewer Periodic Action Manager");
        runTP.setPriority(Thread.MIN_PRIORITY);
        runTP.start();
    }

    public synchronized void stop() {
        runTP = null;
        notify();
    }

    @Override
    public void run() {
        Thread me = Thread.currentThread();
        while (runTP == me && _grMngr.get() != null) {
            updateTooltip();
            checkToolPalette();
            try {
                Thread.sleep(SLEEP_TIME);   //sleep ... ms
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    void updateTooltip() {
        GraphicsManager grMngr = this._grMngr.get();
        if (grMngr == null) {
            return;
        }

        if ((System.currentTimeMillis() - lastMouseMoved) > TOOLTIP_TIME) {
            Glyph g = grMngr.mainView.getPanel().lastGlyphEntered();
            if (g != null && g != grMngr.boundingBox && tippedGlyph != g) {
                tippedGlyph = g;
                if (tippedGlyph.getOwner() != null && tippedGlyph.getOwner() instanceof LElem) {
                    tipLabel = ((LElem)tippedGlyph.getOwner()).getTooltip(tippedGlyph);
                }
                if (tipLabel != null && tipLabel.length() > 0) {
                    lX = grMngr.mainView.mouse.getPanelXCoordinate() + TP_MARGIN;
                    lY = grMngr.mainView.mouse.getPanelYCoordinate() + TP_MARGIN;
                    invalidBounds = true;
                    grMngr.vsm.repaint();
                }
            }
        }
    }

    void removeTooltip() {
        GraphicsManager grMngr = this._grMngr.get();
        if (grMngr == null) {
            return;
        }

        tipLabel = null;
        tippedGlyph = null;
        invalidBounds = true;
        grMngr.vsm.repaint();
    }

    void computeTipRectangle(int labelWidth, int labelHeight) {
        rX = lX - TP_PADDING;
        rY = lY - labelHeight;
        rW = labelWidth + TP_PADDING + TP_PADDING;
        rH = labelHeight + TP_PADDING;
    }

    @Override
    public void paint(Graphics2D g2d, int viewWidth, int viewHeight) {
        if (tipLabel != null) {
            Font origFont = g2d.getFont();
            g2d.setFont(TP_FONT);
            if (invalidBounds) {
                Rectangle2D r2d = g2d.getFontMetrics().getStringBounds(tipLabel, g2d);
                computeTipRectangle((int)r2d.getWidth(), (int)r2d.getHeight());
                invalidBounds = false;
            }
            g2d.setColor(TP_BACKGROUND);
            g2d.fillRect(rX, rY, rW, rH);
            g2d.setColor(TP_FOREGROUND);
            g2d.drawRect(rX, rY, rW, rH);
            g2d.drawString(tipLabel, lX, lY);
            g2d.setFont(origFont);
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        lastMouseMoved = System.currentTimeMillis();
        removeTooltip();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        lastMouseMoved = System.currentTimeMillis();
        removeTooltip();
    }

    void requestToolPaletteRelocation() {
        updatePalette = true;
    }

    void checkToolPalette() {
        GraphicsManager grMngr = this._grMngr.get();
        if (grMngr == null) {
            return;
        }

        if (updatePalette) {
            grMngr.getToolPalette().updateHiddenPosition();
            updatePalette = false;
        }
    }

}
