package fr.inria.zvtm.glyphs;

import fr.inria.zvtm.glyphs.projection.BProjectedCoords;
import java.awt.geom.Ellipse2D;

class ProjEclipse extends BProjectedCoords {

    Ellipse2D eclipsed = new Ellipse2D.Float();
    Ellipse2D shadowSource = new Ellipse2D.Float();
    Ellipse2D leclipsed = new Ellipse2D.Float();
    Ellipse2D lshadowSource = new Ellipse2D.Float();
}
