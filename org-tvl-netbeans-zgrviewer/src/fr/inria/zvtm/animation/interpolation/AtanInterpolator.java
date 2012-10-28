package fr.inria.zvtm.animation.interpolation;

import org.jdesktop.animation.timing.interpolation.Interpolator;

class AtanInterpolator implements Interpolator {

    int N = 4;
    double DEN = Math.atan(N);

    AtanInterpolator(int n) {
        this.N = n;
        this.DEN = Math.atan(this.N);
    }

    @Override
    public float interpolate(float fraction) {
        return (float)((Math.atan(N * (2 * fraction - 1)) / DEN + 1) / (2.0));
    }

}
