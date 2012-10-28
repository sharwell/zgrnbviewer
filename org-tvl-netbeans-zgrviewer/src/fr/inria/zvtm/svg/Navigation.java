package fr.inria.zvtm.svg;

import fr.inria.zvtm.animation.Animation;
import fr.inria.zvtm.animation.interpolation.SlowInSlowOutInterpolator;
import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.engine.portals.OverviewPortal;
import java.awt.Color;
import java.awt.geom.Point2D;

class Navigation {

    /*
     * Navigation constants
     */
    static final short MOVE_UP = 0;
    static final short MOVE_DOWN = 1;
    static final short MOVE_LEFT = 2;
    static final short MOVE_RIGHT = 3;
    Viewer application;
    VirtualSpaceManager vsm;
    Camera mCamera;
    Camera ovCamera;

    Navigation(Viewer app) {
        this.application = app;
        vsm = VirtualSpaceManager.INSTANCE;
    }

    void setCamera(Camera c) {
        this.mCamera = c;
    }

    /*
     * ------------- Navigation       -------------
     */
    void getGlobalView() {
        application.mView.getGlobalView(mCamera, Config.ANIM_MOVE_LENGTH, 1.05f);
    }

    /*
     * Higher view
     */
    void getHigherView() {
        Float alt = new Float(mCamera.getAltitude() + mCamera.getFocal());
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Config.ANIM_MOVE_LENGTH, mCamera,
                                                                                          alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /*
     * Higher view
     */
    void getLowerView() {
        Float alt = new Float(-(mCamera.getAltitude() + mCamera.getFocal()) / 2.0f);
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraAltAnim(Config.ANIM_MOVE_LENGTH, mCamera,
                                                                                          alt, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /*
     * Direction should be one of Viewer.MOVE_*
     */
    void translateView(short direction) {
        Point2D.Double trans;
        double[] rb = application.mView.getVisibleRegion(mCamera);
        if (direction == MOVE_UP) {
            double qt = (rb[1] - rb[3]) / 4.0;
            trans = new Point2D.Double(0, qt);
        } else if (direction == MOVE_DOWN) {
            double qt = (rb[3] - rb[1]) / 4.0;
            trans = new Point2D.Double(0, qt);
        } else if (direction == MOVE_RIGHT) {
            double qt = (rb[2] - rb[0]) / 4.0;
            trans = new Point2D.Double(qt, 0);
        } else {
            // direction==MOVE_LEFT
            double qt = (rb[0] - rb[2]) / 4.0;
            trans = new Point2D.Double(qt, 0);
        }
        Animation a = vsm.getAnimationManager().getAnimationFactory().createCameraTranslation(Config.ANIM_MOVE_LENGTH, mCamera,
                                                                                              trans, true, SlowInSlowOutInterpolator.getInstance(), null);
        vsm.getAnimationManager().startAnimation(a, false);
    }

    /*
     * -------------- Overview -------------------
     */
    OverviewPortal ovPortal;

    void createOverview() {
        ovPortal = new OverviewPortal(application.panelWidth - Config.OVERVIEW_WIDTH - 1, application.panelHeight - Config.OVERVIEW_HEIGHT - 1,
                                      Config.OVERVIEW_WIDTH, Config.OVERVIEW_HEIGHT, ovCamera, mCamera);
        ovPortal.setPortalListener(application.eh);
        ovPortal.setBackgroundColor(Config.BACKGROUND_COLOR);
        ovPortal.setObservedRegionColor(Config.OBSERVED_REGION_COLOR);
        ovPortal.setObservedRegionTranslucency(Config.OBSERVED_REGION_ALPHA);
        VirtualSpaceManager.INSTANCE.addPortal(ovPortal, application.mView);
        ovPortal.setBorder(Color.GREEN);
        updateOverview();
    }

    void updateOverview() {
        if (ovPortal != null) {
            ovCamera.setLocation(ovPortal.getGlobalView());
        }
    }

    void updateOverviewLocation() {
        if (ovPortal != null) {
            ovPortal.moveTo(application.panelWidth - Config.OVERVIEW_WIDTH - 1, application.panelHeight - Config.OVERVIEW_HEIGHT - 1);
        }
    }

    void toggleOverview() {
        ovPortal.setVisible(!ovPortal.isVisible());
        vsm.repaint(application.mView);
    }

}
