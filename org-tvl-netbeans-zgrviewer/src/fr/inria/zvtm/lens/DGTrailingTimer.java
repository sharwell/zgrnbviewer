package fr.inria.zvtm.lens;

import java.util.TimerTask;

public class DGTrailingTimer extends TimerTask {

    TemporalLens lens;
    private boolean enabled = true;

    DGTrailingTimer(TemporalLens l) {
        super();
        this.lens = l;
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
            lens.updateTimeBasedParams();
        }
    }

}
