package fr.inria.zvtm.tests;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.ViewAdapter;
import fr.inria.zvtm.glyphs.Glyph;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

class TestEventHandler extends ViewAdapter {

    static float ZOOM_SPEED_COEF = 1.0f/50.0f;
    static double PAN_SPEED_COEF = 50.0;

    AllGlyphsTest application;

    int lastJPX,lastJPY;

    TestEventHandler(AllGlyphsTest t){
        this.application = t;
    }

    public void press1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        lastJPX = jpx;
        lastJPY = jpy;
        v.setDrawDrag(true);
    }

    public void release1(ViewPanel v,int mod,int jpx,int jpy, MouseEvent e){
        application.mCam.setXspeed(0);
        application.mCam.setYspeed(0);
        v.setDrawDrag(false);
    }

    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e){
        if (application.testView.getCursor().getDynaPicker().isDynaSpotActivated()){
            v.getVCursor().getDynaPicker().dynaPick(application.mCam);
        }
    }

    public void mouseDragged(ViewPanel v,int mod,int buttonNumber,int jpx,int jpy, MouseEvent e){
        if (buttonNumber == 1){
            Camera c = application.mCam;
            double a = (c.focal+Math.abs(c.altitude)) / c.focal;
            application.mCam.setXspeed((c.altitude>0) ? (long)((jpx-lastJPX)*(a/PAN_SPEED_COEF)) : (long)((jpx-lastJPX)/(a*PAN_SPEED_COEF)));
            application.mCam.setYspeed((c.altitude>0) ? (long)((lastJPY-jpy)*(a/PAN_SPEED_COEF)) : (long)((lastJPY-jpy)/(a*PAN_SPEED_COEF)));
        }
    }

    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e){
        Camera c = application.mCam;
        double a = (c.focal+Math.abs(c.altitude)) / c.focal;
        if (wheelDirection == WHEEL_DOWN){
            c.altitudeOffset(-a*5);
            VirtualSpaceManager.INSTANCE.repaint();
        }
        else {
            //wheelDirection == WHEEL_UP
            c.altitudeOffset(a*5);
            VirtualSpaceManager.INSTANCE.repaint();
        }
    }

    public void enterGlyph(Glyph g){
        g.highlight(true, null);
        System.out.println(g);
    }

    public void exitGlyph(Glyph g){
        g.highlight(false, null);
    }

    public void Kpress(ViewPanel v,char c,int code,int mod, KeyEvent e){
        if (c == 't'){application.translate();}
        else if (c == 'r'){application.rotate();}
        else if (c == 'd'){application.toggleDynaSpot();}
    }

    public void viewClosing(View v){
        System.exit(0);
    }

}
