/*
 * To change this template, choose Tools | Templates and open the template in
 * the editor.
 */
package net.claribole.zgrviewer;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.glyphs.VImage;

/**
 *
 * @author sam
 */
public interface ToolPalette {
    
    public static final short STD_NAV_MODE = 0;
    public static final short FL_NAV_MODE = 1;
    public static final short DM_NAV_MODE = 2;
    public static final short PL_NAV_MODE = 3;
    public static final short HIGHLIGHT_MODE = 4;
    public static final short BRING_AND_GO_MODE = 5;
    public static final short LINK_SLIDING_MODE = 6;
    public static final short EDIT_MODE = 7;

    public Camera getPaletteCamera();

    public boolean isStdNavMode();

    public boolean isFadingLensNavMode();

    public boolean isDragMagNavMode();

    public boolean isProbingLensNavMode();

    public boolean isHighlightMode();

    public boolean isBringAndGoMode();

    public boolean isLinkSlidingMode();

    public boolean isEditMode();

    public boolean isEnabled();

    public boolean isShowing();

    public void show();

    public void hide();

    public boolean insidePaletteTriggerZone(int x, int y);

    public void selectButton(short mode);

    public void selectButton(VImage image);

    public void hideLogicalTools();

    public void showLogicalTools();

    public void displayPalette(boolean b);

    public void updateHiddenPosition();
}
