package fr.inria.zvtm.widgets;

import fr.inria.zvtm.engine.Utils;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.PRectangle;
import fr.inria.zvtm.glyphs.VText;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of a linear menu.
 */
public class Menu {

    protected static final double DEFAULT_WIDTH = 200;
    protected static final double DEFAULT_ITEM_HEIGHT = 25;
    protected static final Color MENU_BGCOLOR = new Color(70, 70, 70);
    protected static final Color MENU_HCOLOR = new Color(120, 120, 120);
    protected static final String MENU_BOX_TYPE = "MENU_BOX";
    protected final VirtualSpace space;
    protected final List<Glyph> elems = new ArrayList<Glyph>();

    public Menu(VirtualSpace space) {
        this.space = space;
    }

    /**
     * Shows the menu
     */
    public void show() {
        for (Glyph elem : elems) {
            space.show(elem);
        }
    }

    /**
     * Hides the menu
     */
    public void hide() {
        for (Glyph elem : elems) {
            space.hide(elem);
        }
    }

    /**
     * Clears the menu
     */
    public void clear() {
        for (Glyph elem : elems) {
            space.removeGlyph(elem);
        }
        elems.clear();
    }

    /**
     * Override this method if you need to customize menu labels
     * (e.g. set color...)
     */
    protected VText makeLabel(MenuItem<?> item, double xpos, double ypos) {
        VText retval = new VText(xpos, ypos, 0, Color.WHITE,
                                 item.getText());
        retval.setTextAnchor(VText.TEXT_ANCHOR_MIDDLE);
        return retval;
    }

    /**
     * Builds a menu from the given items.
     */
    public void populate(List<MenuItem<?>> items, Point2D.Double coords) {
        double xpos = coords.getX();
        double ypos = coords.getY();
        for (MenuItem<?> item : items) {
            PRectangle menuBox = new PRectangle(xpos, ypos, 0,
                                                DEFAULT_WIDTH, DEFAULT_ITEM_HEIGHT,
                                                Utils.makeDiagGradient((float)DEFAULT_WIDTH,
                                                                       (float)DEFAULT_ITEM_HEIGHT, MENU_BGCOLOR));
            menuBox.setCursorInsidePaint(Utils.makeDiagGradient(
                (float)DEFAULT_WIDTH, (float)DEFAULT_ITEM_HEIGHT,
                MENU_HCOLOR));
            menuBox.setType(MENU_BOX_TYPE);
            menuBox.setOwner(item);

            VText label = makeLabel(item, xpos, ypos);
            label.setSensitivity(false);

            elems.add(menuBox);
            elems.add(label);
            space.addGlyph(menuBox);
            space.addGlyph(label);
            ypos -= (DEFAULT_ITEM_HEIGHT + 1);
        }
    }

    /**
     * Returns the MenuItem associated to a given Glyph, if
     * there is such an item.
     *
     * @return the MenuItem associated to the given Glyph, or null
     * if there is no such item.
     */
    public MenuItem<?> getItemForGlyph(Glyph glyph) {
        int idx = elems.indexOf(glyph);
        if (idx == -1) {
            return null;
        }
        assert (elems.get(idx).getOwner() instanceof MenuItem);
        return (MenuItem)(elems.get(idx).getOwner());
    }

    /**
     * Should be called from outside to highlight the menu items
     * if needed.
     */
    public void onEnterGlyph(Glyph g) {
        if (g.getType().equals(MENU_BOX_TYPE) && elems.contains(g)) {
            g.highlight(true, null);
        }
    }

    /**
     * Should be called from outside to highlight the menu items
     * if needed.
     */
    public void onExitGlyph(Glyph g) {
        if (g.getType().equals(MENU_BOX_TYPE) && elems.contains(g)) {
            g.highlight(false, null);
        }
    }

}
