package fr.inria.zvtm.glyphs;

import fr.inria.zvtm.engine.Camera;
import java.awt.Shape;
import java.awt.geom.QuadCurve2D;

final class QDCElement extends PathElement {

    /*
     * Draw a quadratic curve from previous point to (x,y) in virtual space,
     * controlled by point (ctrlx, ctrly)
     */
    double ctrlx;
    double ctrly;
    QuadCurve2D[] pc;
    QuadCurve2D[] lpc;

    QDCElement(double x, double y, double ctrlx, double ctrly, int nbCam) {
        type = DPath.QDC;
        this.x = x;
        this.y = y;
        this.ctrlx = ctrlx;
        this.ctrly = ctrly;
        if (nbCam > 0) {
            initCams(nbCam);
        }
    }

    @Override
    void initCams(int nbCam) {
        pc = new QuadCurve2D[nbCam];
        lpc = new QuadCurve2D[nbCam];
        for (int i = 0; i < nbCam; i++) {
            pc[i] = new QuadCurve2D.Double();
            lpc[i] = new QuadCurve2D.Double();
        }
    }

    @Override
    void addCamera(int verifIndex) {
        if (pc != null) {
            if (verifIndex == pc.length) {
                QuadCurve2D[] ta = pc;
                pc = new QuadCurve2D[ta.length + 1];
                System.arraycopy(ta, 0, pc, 0, ta.length);
                pc[pc.length - 1] = new QuadCurve2D.Double();
                ta = lpc;
                lpc = new QuadCurve2D[ta.length + 1];
                System.arraycopy(ta, 0, lpc, 0, ta.length);
                lpc[lpc.length - 1] = new QuadCurve2D.Double();
            } else {
                System.err.println("DPath:Error while adding camera " + verifIndex);
            }
        } else {
            if (verifIndex == 0) {
                pc = new QuadCurve2D[1];
                pc[0] = new QuadCurve2D.Double();
                lpc = new QuadCurve2D[1];
                lpc[0] = new QuadCurve2D.Double();
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
        pc[i].setCurve(px, py, hw + (ctrlx - c.vx) * coef, hh - (ctrly - c.vy) * coef, hw + (x - c.vx) * coef, hh - (y - c.vy) * coef);
    }

    @Override
    void projectForLens(int i, int hw, int hh, double lx, double ly, double coef, double px, double py) {
        lpc[i].setCurve(px, py, hw + (ctrlx - lx) * coef, hh - (ctrly - ly) * coef, hw + (x - lx) * coef, hh - (y - ly) * coef);
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
