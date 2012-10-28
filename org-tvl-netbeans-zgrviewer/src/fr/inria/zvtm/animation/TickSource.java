package fr.inria.zvtm.animation;

import org.jdesktop.animation.timing.TimingSource;

//A custom tick source to replace the one provided by the Timing Framework.
//NOTE: setResolution() has no effect (the resolution will always be the one supplied by the TickThread)
//Every animation has its own TickSource (that way they can be started or stopped independently)
//but all tick sources share the same TickThread (enforced by AnimationManager)
//@ThreadSafe
class TickSource extends TimingSource {

    private TickThread tickThread;

    public TickSource(TickThread tickThread) {
        this.tickThread = tickThread;
    }

    @Override
    public void setResolution(int resolution) {
        //This has purposely no effect. Animation resolution is set globally,
        //see AnimationManager#setResolution.
    }

    @Override
    public void setStartDelay(int delay) {
        //This has purposely no effect. We do not currently provide a start
        //delay for animations, altough this could be done.
    }

    @Override
    public void start() {
        tickThread.addSubscriber(this);
    }

    @Override
    public void stop() {
        tickThread.removeSubscriber(this);
    }

    //called by TickThread instance, which needs at least package acccess
    void tick() {
        timingEvent();
    }

}
