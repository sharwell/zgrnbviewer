package fr.inria.zvtm.glyphs;

import fr.inria.zvtm.engine.Camera;
import java.awt.Shape;
import java.awt.geom.Point2D;

final class MOVElement extends PathElement {

    /*
     * Move from previous point to (x,y) in virtual space without drawing
     * anything
     */
    Point2D[] pc;
    Point2D[] lpc;

    MOVElement(double x, double y, int nbCam) {
        type = DPath.MOV;
        this.x = x;
        this.y = y;
        if (nbCam > 0) {
            initCams(nbCam);
        }
    }

    @Override
    void initCams(int nbCam) {
        pc = new Point2D[nbCam];
        lpc = new Point2D[nbCam];
        for (int i = 0; i < nbCam; i++) {
            pc[i] = new Point2D.Double();
            lpc[i] = new Point2D.Double();
        }
    }

    @Override
    void addCamera(int verifIndex) {
        if (pc != null) {
            if (verifIndex == pc.length) {
                Point2D[] ta = pc;
                pc = new Point2D[ta.length + 1];
                System.arraycopy(ta, 0, pc, 0, ta.length);
                pc[pc.length - 1] = new Point2D.Double();
                ta = lpc;
                lpc = new Point2D[ta.length + 1];
                System.arraycopy(ta, 0, lpc, 0, ta.length);
                lpc[lpc.length - 1] = new Point2D.Double();
            } else {
                System.err.println("DPath:Error while adding camera " + verifIndex);
            }
        } else {
            if (verifIndex == 0) {
                pc = new Point2D[1];
                pc[0] = new Point2D.Double();
                lpc = new Point2D[1];
                lpc[0] = new Point2D.Double();
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
        pc[i].setLocation(hw + (x - c.vx) * coef, hh - (y - c.vy) * coef);
    }

    @Override
    void projectForLens(int i, int hw, int hh, double lx, double ly, double coef, double px, double py) {
        lpc[i].setLocation(hw + (x - lx) * coef, hh - (y - ly) * coef);
    }

    @Override
    double getX(int i) {
        return pc[i].getX();
    }

    @Override
    double getY(int i) {
        return pc[i].getY();
    }

    @Override
    double getlX(int i) {
        return lpc[i].getX();
    }

    @Override
    double getlY(int i) {
        return lpc[i].getY();
    }

    @Override
    Shape getShape(int i) {
        return null;
    }

    @Override
    Shape getlShape(int i) {
        return null;
    }

}
