package fr.inria.zvtm.engine;

import fr.inria.zvtm.event.SelectionListener;
import fr.inria.zvtm.glyphs.Glyph;

class DefaultSelectionAction implements SelectionListener {

    @Override
    public void glyphSelected(Glyph g, boolean b) {
        g.highlight(b, null);
    }

}
