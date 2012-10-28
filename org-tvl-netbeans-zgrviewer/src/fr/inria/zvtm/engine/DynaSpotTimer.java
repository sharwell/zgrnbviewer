package fr.inria.zvtm.engine;

import java.util.TimerTask;

class DynaSpotTimer extends TimerTask {

    DynaPicker dp;

    DynaSpotTimer(DynaPicker dp) {
        super();
        this.dp = dp;
    }

    @Override
    public void run() {
        dp.updateDynaSpot(System.currentTimeMillis());
    }

}
