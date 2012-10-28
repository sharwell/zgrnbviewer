package fr.inria.zvtm.glyphs;

import fr.inria.zvtm.engine.Camera;
import java.awt.Shape;
import java.awt.geom.Line2D;

final class SEGElement extends PathElement {

    /*
     * Draw a segment from previous point to (x,y) in virtual space
     */
    Line2D[] pc;
    Line2D[] lpc;

    SEGElement(double x, double y, int nbCam) {
        type = DPath.SEG;
        this.x = x;
        this.y = y;
        if (nbCam > 0) {
            initCams(nbCam);
        }
    }

    @Override
    void initCams(int nbCam) {
        pc = new Line2D[nbCam];
        lpc = new Line2D[nbCam];
        for (int i = 0; i < nbCam; i++) {
            pc[i] = new Line2D.Double();
            lpc[i] = new Line2D.Double();
        }
    }

    @Override
    void addCamera(int verifIndex) {
        if (pc != null) {
            if (verifIndex == pc.length) {
                Line2D[] ta = pc;
                pc = new Line2D[ta.length + 1];
                System.arraycopy(ta, 0, pc, 0, ta.length);
                pc[pc.length - 1] = new Line2D.Double();
                ta = lpc;
                lpc = new Line2D[ta.length + 1];
                System.arraycopy(ta, 0, lpc, 0, ta.length);
                lpc[lpc.length - 1] = new Line2D.Double();
            } else {
                System.err.println("DPath:Error while adding camera " + verifIndex);
            }
        } else {
            if (verifIndex == 0) {
                pc = new Line2D[1];
                pc[0] = new Line2D.Double();
                lpc = new Line2D[1];
                lpc[0] = new Line2D.Double();
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
        pc[i].setLine(px, py, hw + (x - c.vx) * coef, hh - (y - c.vy) * coef);
    }

    @Override
    void projectForLens(int i, int hw, int hh, double lx, double ly, double coef, double px, double py) {
        lpc[i].setLine(px, py, hw + (x - lx) * coef, hh - (y - ly) * coef);
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
