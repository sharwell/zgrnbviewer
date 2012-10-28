package fr.inria.zvtm.animation;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.swing.Timer;

class TickThread {

    private Timer edtTimer;
    //receivers is traversed a *lot* more often than it is mutated
    private final List<TickSource> receivers = new CopyOnWriteArrayList<TickSource>();

    public TickThread(String name) {
        ActionListener taskPerformer = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent evt) {
                for (TickSource ts : receivers) {
                    ts.tick();
                }
            }

        };
        edtTimer = new Timer(16, taskPerformer);
    }

    public void start() {
        edtTimer.start();
    }

    public void setResolution(int res) {
        edtTimer.setDelay(res);
    }

    public void addSubscriber(TickSource ts) {
        receivers.add(ts);
    }

    public void removeSubscriber(TickSource ts) {
        receivers.remove(ts);
    }

    public void requestStop() {
        edtTimer.stop();
    }

}
