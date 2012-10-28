package fr.inria.zvtm.tests;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.event.SwingListener;

import java.awt.event.MouseEvent;

class MyEventListener extends SwingListener {

	SwingTest application;

	static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;


	int lastJPX, lastJPY;

	MyEventListener(SwingTest app){
		this.application = app;
	}

	@Override public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e){
        lastJPX=jpx;
        lastJPY=jpy;
        v.setDrawDrag(true);
		pickAndForward(v, e);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.cam.setXspeed(0);
        application.cam.setYspeed(0);
        application.cam.setZspeed(0);
        v.setDrawDrag(false);
		pickAndForward(v, e);
    }

	public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 1){
            Camera c = v.cams[0];
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            if (mod == SHIFT_MOD) {
                application.cam.setXspeed(0);
                application.cam.setYspeed(0);
                application.cam.setZspeed(((lastJPY-jpy)*(ZOOM_SPEED_COEF)));
                //50 is just a speed factor (too fast otherwise)
            }
            else {
                application.cam.setXspeed((c.altitude>0) ? ((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : ((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
                application.cam.setYspeed((c.altitude>0) ? ((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : ((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
                application.cam.setZspeed(0);
            }
        }
    }

}
