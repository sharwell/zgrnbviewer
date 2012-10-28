package fr.inria.zvtm.lens;

import java.util.TimerTask;

class DICTrailingTimer extends TimerTask {

    TemporalLens lens;
    private boolean enabled = true;

    DICTrailingTimer(TemporalLens l) {
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
