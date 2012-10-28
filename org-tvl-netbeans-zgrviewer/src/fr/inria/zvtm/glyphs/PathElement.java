package fr.inria.zvtm.glyphs;

import fr.inria.zvtm.engine.Camera;
import java.awt.Shape;

abstract class PathElement {

    short type;
    double x;
    double y;

    abstract void initCams(int nbCam);

    abstract void addCamera(int verifIndex);

    abstract void removeCamera(int index);

    abstract void project(int i, int hw, int hh, Camera c, double coef, double px, double py);

    abstract void projectForLens(int i, int hw, int hh, double lx, double ly, double coef, double px, double py);

    abstract double getX(int i);

    abstract double getY(int i);

    abstract double getlX(int i);

    abstract double getlY(int i);

    abstract Shape getShape(int i);

    abstract Shape getlShape(int i);

}
