/*
 * FILE: VirtualSpaceManager.java DATE OF CREATION: Jul 11 2000 AUTHOR :
 * Emmanuel Pietriga (emmanuel.pietriga@xrce.xerox.com) MODIF: Emmanuel Pietriga
 * (emmanuel.pietriga@inria.fr) Copyright (c) Xerox Corporation, XRCE/Contextual
 * Computing, 2000-2002. All Rights Reserved Copyright (c) 2003 World Wide Web
 * Consortium. All Rights Reserved Copyright (c) INRIA, 2004-2011. All Rights
 * Reserved
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * For full terms see the file COPYING.
 *
 * $Id: VirtualSpaceManager.java 4294 2011-03-03 10:22:53Z epietrig $
 */
package fr.inria.zvtm.engine;

import fr.inria.zvtm.animation.AnimationManager;
import fr.inria.zvtm.engine.portals.Portal;
import fr.inria.zvtm.event.RepaintListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.VText;
import java.awt.AWTEvent;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;
import javax.swing.JMenuBar;
import org.openide.util.WeakSet;

/**
 * Virtual space manager. This is the main entry point to the toolkit. Virtual
 * spaces and views are instanciated from here.
 *
 * @author Emmanuel Pietriga
 *
 */
public class VirtualSpaceManager implements AWTEventListener {

    /**
     * Called by VText to update default font.
     */
    public void onMainFontUpdated() {
        for (View view : allViews) {
            view.updateFont();
        }
        Object g;
        for (Iterator<VirtualSpace> e = allVirtualSpaces.values().iterator(); e.hasNext();) {
            for (Iterator<Glyph> e2 = e.next().getAllGlyphs().iterator(); e2.hasNext();) {
                g = e2.next();
                if (g instanceof VText) {
                    ((VText)g).invalidate();
                }
            }
        }
        repaint();
    }

    /**
     * Select only glyphs that are visible and sensitive to the cursor.
     */
    public static short VIS_AND_SENS_GLYPHS = 0;
    /**
     * Select only glyphs that are visible.
     */
    public static short VISIBLE_GLYPHS = 1;
    /**
     * Select only glyphs that are sensitive to the cursor.
     */
    public static short SENSITIVE_GLYPHS = 2;
    /**
     * Select all glyphs in the region.
     */
    public static short ALL_GLYPHS = 3;
    /**
     * print exceptions and warning
     */
    static boolean debug = false;
    /**
     * key is space name (String)
     */
    protected Map<String, VirtualSpace> allVirtualSpaces;
    private List<VirtualSpace> virtualSpaceList;
    /**
     * All views managed by this VSM
     */
    protected final Set<View> allViews = new WeakSet<View>();
    /**
     * used to quickly retrieve a view by its name
     */
    protected Map<String, WeakReference<View>> name2view;
    /**
     * View which has the focus (or which was the last to have it among all views)
     */
    WeakReference<View> activeView;
    /**
     * enables detection of multiple full fills in one view repaint - default
     * value assigned to new views - STILL VERY BUGGY - ONLY SUPPORTS VRectangle
     * and VCircle for now - setting it to true will prevent some glyphs from
     * being painted if they are not visible in the final rendering (because of
     * occlusion). This can enhance performance (in configurations where
     * occlusion does happen).
     */
    boolean defaultMultiFill = false;
    /**
     * Animation Manager
     */
    private final AnimationManager animationManager;
    public static final VirtualSpaceManager INSTANCE = new VirtualSpaceManager();

    /**
     * Automatic instantiation as a singleton. THere is always a single VSM per
     * application.
     */
    private VirtualSpaceManager() {
        if (debug) {
            System.out.println("Debug mode ON");
        }
        animationManager = new AnimationManager(this);
        allVirtualSpaces = new HashMap<String, VirtualSpace>();
        virtualSpaceList = new ArrayList<VirtualSpace>(0);
        name2view = new HashMap<String, WeakReference<View>>();
    }

    /**
     * set debug mode ON or OFF
     */
    public static void setDebug(boolean b) {
        debug = b;
    }

    /**
     * get debug mode state (ON or OFF)
     */
    public static boolean debugModeON() {
        return debug;
    }

    /**
     * Returns a reference to the AnimationManager associated with this
     * VirtualSpaceManager.
     */
    public AnimationManager getAnimationManager() {
        return animationManager;
    }

    /**
     * Enable/disable detection of multiple full fills in one view repaint for
     * this View. Off by default. If enabled, all glyphs below the higest glyph
     * in the drawing stack that fills the viewport will not be painted, as they
     * will be invisible anyway. This computation has a cost. Assess its
     * usefulness and evaluate performance (there is tradeoff).
     *
     * @see #getDefaultDetectMultiFills()
     */
    public void setDefaultDetectMultiFills(boolean b) {
        defaultMultiFill = b;
    }

    /**
     * Tells whether detection of multiple full fills in one view repaint for
     * this View is enabled or disabled. Off by default. If enabled, all glyphs
     * below the higest glyph in the drawing stack that fills the viewport will
     * not be painted, as they will be invisible anyway. This computation has a
     * cost. Assess its usefulness and evaluate performance (there is tradeoff).
     *
     * @see #setDefaultDetectMultiFills(boolean b)
     */
    public boolean getDefaultDetectMultiFills() {
        return defaultMultiFill;
    }

    /*
     * -------------- Active entities ------------------
     */
    Object activeJFrame = null;

    @Override
    public void eventDispatched(AWTEvent e) {
        if (e.getID() == WindowEvent.WINDOW_ACTIVATED) {
            activeJFrame = e.getSource();
        }
    }

    /**
     * Manually set what view is active.
     */
    public void setActiveView(View v) {
        activeView = new WeakReference<View>(v);
    }

    /**
     * Get currently active view.
     */
    public View getActiveView() {
        return activeView.get();
    }

    /**
     * Get active camera (in focused view).
     *
     * @return null if no view is active
     */
    public Camera getActiveCamera() {
        View activeView = getActiveView();
        if (activeView == null) {
            return null;
        }

        return activeView.getActiveCamera();
    }

    /*
     * -------------- PORTALS ------------------
     */
    /**
     * Add a portal to a View.
     *
     * @param p Portal to be added
     * @param v owning View
     */
    public Portal addPortal(Portal p, View v) {
        return v.addPortal(p);
    }

    /**
     * Destroy a portal (remove it from the View).
     */
    public void destroyPortal(Portal p) {
        View v = p.getOwningView();
        v.removePortal(p);
    }

    /*
     * ----------------- VIEWS ----------------
     */
    /**
     * Create a new External View.<br>
     *
     * @param c vector of cameras making this view (if more than one camera,
     * cameras will be superimposed on different layers)
     * @param name view name - pass View.ANONYMOUS to generate a unique, random
     * name.
     * @param viewType one of View.STD_VIEW, View.OPENGL_VIEW - determines the
     * type of view and acceleration method.The use of OPENGL_VIEW requires the
     * following Java property: -Dsun.java2d.opengl=true
     * @param w width of window in pixels
     * @param h height of window in pixels
     * @param visible should the view be made visible automatically or not
     * @see #addFrameView(List c, String name, String viewType, int w, int h,
     * boolean bar, boolean visible, boolean decorated, JMenuBar mnb)
     */
    public View addFrameView(List<Camera> c, String name, String viewType, int w, int h, boolean visible) {
        return addFrameView(new ArrayList<Camera>(c), name, viewType, w, h, false, visible, true, null);
    }

    /**
     * Create a new external view.<br> The use of OPENGL_VIEW requires the
     * following Java property: -Dsun.java2d.opengl=true
     *
     * @param c vector of cameras making this view (if more than one camera,
     * cameras will be superimposed on different layers)
     * @param name view name - pass View.ANONYMOUS to generate a unique, random
     * name.
     * @param viewType one of View.STD_VIEW, View.OPENGL_VIEW - determines the
     * type of view and acceleration method
     * @param w width of window in pixels
     * @param h height of window in pixels
     * @param bar true -&gt; add a status bar to this view (below main panel)
     * @param visible should the view be made visible automatically or not
     * @param decorated should the view be decorated with the underlying window
     * manager's window frame or not
     * @param mnb a menu bar (null if none), already configured with
     * ActionListeners already attached to items (it is just added to the view)
     * @see #addFrameView(List c, String name, String viewType, int w, int h,
     * boolean visible)
     */
    public View addFrameView(List<Camera> c, String name, String viewType, int w, int h,
                             boolean bar, boolean visible, boolean decorated, JMenuBar mnb) {
        View v = null;
        if (View.ANONYMOUS.equals(name)) {
            name = UUID.randomUUID().toString();
            while (name2view.containsKey(name)) {
                name = UUID.randomUUID().toString();
            }
        }
        v = (mnb != null) ? new EView(viewType, new ArrayList<Camera>(c), name, w, h, bar, visible, decorated, mnb) : new EView(viewType, new ArrayList<Camera>(c), name, w, h, visible, decorated);
        addView(v);
        return v;
    }

    /**
     * Create a new view embedded in a JPanel, suitable for inclusion in any
     * Swing component hierarchy, including a JApplet.
     *
     * @param c vector of cameras superimposed in this view
     * @param name view name - pass View.ANONYMOUS to generate a unique, random
     * name.
     * @param viewType one of View.STD_VIEW, View.OPENGL_VIEW - determines the
     * type of view and acceleration method.The use of OPENGL_VIEW requires the
     * following Java property: -Dsun.java2d.opengl=true
     * @param w width of window in pixels
     * @param h height of window in pixels
     */
    public PView addPanelView(List<Camera> c, String name, String viewType, int w, int h) {
        if (View.ANONYMOUS.equals(name)) {
            name = UUID.randomUUID().toString();
            while (name2view.containsKey(name)) {
                name = UUID.randomUUID().toString();
            }
        }
        PView tvi = new PView(viewType, new ArrayList<Camera>(c), name, w, h);
        addView(tvi);
        return tvi;
    }

    /**
     * Adds a newly created view to the list of existing views Side-effect:
     * attempts to start the animation manager
     */
    protected void addView(View v) {
        allViews.add(v);
        name2view.put(v.name, new WeakReference<View>(v));
        animationManager.start(); //starts animationManager if not already running
    }

    /**
     * Get View whose name is n.
     *
     * @return null if no match
     */
    public View getView(String n) {
        WeakReference<View> ref = name2view.get(n);
        if (ref == null) {
            return null;
        }

        if (ref.get() == null) {
            name2view.remove(n);
        }

        return ref.get();
    }

    /**
     * Destroy a View identified by its index in the list of views.
     */
    protected void destroyView(View v) {
        if (v == null) {
            return;
        }

        allViews.remove(v);
        updateViewIndex();
    }

    /**
     * Update mapping between view name and view index in the list of views when
     * complex changes are made to the list of views (like removing a view).
     */
    protected void updateViewIndex() {
        name2view.clear();
        for (View view : allViews) {
            name2view.put(view.name, new WeakReference<View>(view));
        }
    }

    /**
     * Destroy a view.
     */
    protected void destroyView(String viewName) {
        destroyView(getView(viewName));
    }

    /**
     * Ask for all Views to be repainted. This is an asynchronous call. In some
     * cases it is not possible to detect graphical changes so repaint calls
     * have to be issued manually (unless you are willing to wait for another
     * event to trigger repaint).
     *
     * @see #repaint(View v)
     * @see #repaint(View v, RepaintListener rl)
     */
    public void repaint() {
        for (View view : allViews) {
            view.repaint();
        }
    }

    /**
     * Ask for View v to be repainted. This is an asynchronous call. In some
     * cases it is not possible to detect graphical changes so repaint calls
     * have to be issued manually (unless you are willing to wait for another
     * event to trigger repaint).
     *
     * @see #repaint()
     * @see #repaint(View v, RepaintListener rl)
     */
    public void repaint(View v) {
        v.repaint();
    }

    /**
     * Ask for View v to be repainted. This is an asynchronous call. In some
     * cases it is not possible to detect graphical changes so repaint calls
     * have to be issued manually (unless you are willing to wait for another
     * event to trigger repaint).
     *
     * @param v the view to repaint
     * @param rl a repaint listener to be notified when this repaint cycle is
     * completed (it must be removed manually if you are not interested in being
     * notified about following repaint cycles)
     * @see #repaint(View v)
     * @see View#removeRepaintListener()
     */
    public void repaint(View v, RepaintListener rl) {
        v.repaint(rl);
    }

    /*
     * ----------- VIRTUAL SPACE ---------------
     */
    /**
     * Create a new virtual space.
     *
     * @param name name of this virtual space. Pass VirtualSpace.ANONYMOUS to
     * generate a name randomly (guaranteed to be unique).
     * @return the new virtual space
     */
    public VirtualSpace addVirtualSpace(String name) {
        if (VirtualSpace.ANONYMOUS.equals(name)) {
            name = UUID.randomUUID().toString();
            while (allVirtualSpaces.containsKey(name)) {
                name = UUID.randomUUID().toString();
            }
        }
        VirtualSpace tvs = new VirtualSpace(name);
        allVirtualSpaces.put(name, tvs);
        updateVirtualSpaceList();
        return tvs;
    }

    /**
     * Destroy a virtual space.
     *
     * @param name name of this virtual space
     */
    public void destroyVirtualSpace(String name) {
        if (allVirtualSpaces.containsKey(name)) {
            allVirtualSpaces.get(name).destroy();
            allVirtualSpaces.remove(name);
            updateVirtualSpaceList();
        }
    }

    /**
     * Destroy a virtual space.
     *
     * @param vs virtual space to destroy
     */
    public void destroyVirtualSpace(VirtualSpace vs) {
        vs.destroy();
        String n = vs.getName();
        if (allVirtualSpaces.containsKey(n)) {
            allVirtualSpaces.remove(n);
            updateVirtualSpaceList();
        }
    }

    void updateVirtualSpaceList() {
        virtualSpaceList = new ArrayList<VirtualSpace>(allVirtualSpaces.values());
    }

    /**
     * Get the virtual space owning Glyph g.
     */
    public VirtualSpace getOwningSpace(Glyph g) {
        VirtualSpace vs;
        for (Iterator<VirtualSpace> e = allVirtualSpaces.values().iterator(); e.hasNext();) {
            vs = e.next();
            if (vs.getAllGlyphs().contains(g)) {
                return vs;
            }
        }
        return null;
    }

    /**
     * Get virtual space named n.
     *
     * @return null if no virtual space named n
     */
    public VirtualSpace getVirtualSpace(String n) {
        return allVirtualSpaces.get(n);
    }

    /**
     * Get all virtual spaces.
     *
     * @return an unmodifiable list of all virtual spaces managed by this VSM.
     */
    public List<VirtualSpace> getVirtualSpaces() {
        return Collections.unmodifiableList(virtualSpaceList);
    }

    /**
     * Get active virtual space, i.e., the space owning the camera currently
     * active.
     *
     * @return null if no camera/view is active
     */
    public VirtualSpace getActiveSpace() {
        Camera activeCamera = getActiveCamera();
        if (activeCamera == null) {
            return null;
        }

        return activeCamera.getOwningSpace();
    }

}
