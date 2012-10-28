package fr.inria.zvtm.glyphs;

import fr.inria.zvtm.engine.Camera;
import java.awt.Shape;
import java.awt.geom.CubicCurve2D;

class CBCElement extends PathElement {

    /*
     * Draw a cubic curve from previous point to (x,y) in virtual space,
     * controlled by points (ctrlx1, ctrly1) and (ctrlx2, ctrly2)
     */
    double ctrlx1;
    double ctrly1;
    double ctrlx2;
    double ctrly2;
    CubicCurve2D[] pc;
    CubicCurve2D[] lpc;

    CBCElement(double x, double y, double ctrlx1, double ctrly1, double ctrlx2, double ctrly2, int nbCam) {
        type = DPath.CBC;
        this.x = x;
        this.y = y;
        this.ctrlx1 = ctrlx1;
        this.ctrly1 = ctrly1;
        this.ctrlx2 = ctrlx2;
        this.ctrly2 = ctrly2;
        if (nbCam > 0) {
            initCams(nbCam);
        }
    }

    @Override
    void initCams(int nbCam) {
        pc = new CubicCurve2D[nbCam];
        lpc = new CubicCurve2D[nbCam];
        for (int i = 0; i < nbCam; i++) {
            pc[i] = new CubicCurve2D.Double();
            lpc[i] = new CubicCurve2D.Double();
        }
    }

    @Override
    void addCamera(int verifIndex) {
        if (pc != null) {
            if (verifIndex == pc.length) {
                CubicCurve2D[] ta = pc;
                pc = new CubicCurve2D[ta.length + 1];
                System.arraycopy(ta, 0, pc, 0, ta.length);
                pc[pc.length - 1] = new CubicCurve2D.Double();
                ta = lpc;
                lpc = new CubicCurve2D[ta.length + 1];
                System.arraycopy(ta, 0, lpc, 0, ta.length);
                lpc[lpc.length - 1] = new CubicCurve2D.Double();
            } else {
                System.err.println("DPath:Error while adding camera " + verifIndex);
            }
        } else {
            if (verifIndex == 0) {
                pc = new CubicCurve2D[1];
                pc[0] = new CubicCurve2D.Double();
                lpc = new CubicCurve2D[1];
                lpc[0] = new CubicCurve2D.Double();
            } else {
                System.err.println("DPath:Error while adding camera " + verifIndex);
            }
        }
    }

    @Override
    void removeCamera(int index) {
        pc[index] = null;
        lpc[index] = null;
    }

    @Override
    void project(int i, int hw, int hh, Camera c, double coef, double px, double py) {
        pc[i].setCurve(px, py,
                       hw + (ctrlx1 - c.vx) * coef, hh - (ctrly1 - c.vy) * coef,
                       hw + (ctrlx2 - c.vx) * coef, hh - (ctrly2 - c.vy) * coef,
                       hw + (x - c.vx) * coef, hh - (y - c.vy) * coef);
    }

    @Override
    void projectForLens(int i, int hw, int hh, double lx, double ly, double coef, double px, double py) {
        lpc[i].setCurve(px, py,
                        hw + (ctrlx1 - lx) * coef, hh - (ctrly1 - ly) * coef,
                        hw + (ctrlx2 - lx) * coef, hh - (ctrly2 - ly) * coef,
                        hw + (x - lx) * coef, hh - (y - ly) * coef);
    }

    @Override
    double getX(int i) {
        return pc[i].getX2();
    }

    @Override
    double getY(int i) {
        return pc[i].getY2();
    }

    @Override
    double getlX(int i) {
        return lpc[i].getX2();
    }

    @Override
    double getlY(int i) {
        return lpc[i].getY2();
    }

    @Override
    Shape getShape(int i) {
        return pc[i];
    }

    @Override
    Shape getlShape(int i) {
        return lpc[i];
    }

}
