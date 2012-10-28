package fr.inria.zvtm.engine.portals;

import java.util.TimerTask;

class TrailingTimer extends TimerTask {

    TrailingCameraPortal portal;
    private boolean enabled = true;

    TrailingTimer(TrailingCameraPortal p) {
        super();
        this.portal = p;
    }

    public void setEnabled(boolean b) {
        enabled = b;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void run() {
        if (enabled) {
            portal.updateWidgetLocation();
        }
    }

}
