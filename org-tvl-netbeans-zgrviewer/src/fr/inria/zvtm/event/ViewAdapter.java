/*
 * FILE: DefaultEventHandler.java DATE OF CREATION: Fri May 26 15:01:11 2006
 * AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr) MODIF: Emmanuel
 * Pietriga (emmanuel.pietriga@inria.fr) Copyright (c) INRIA, 2006-2010. All
 * Rights Reserved Licensed under the GNU LGPL. For full terms see the file
 * COPYING.
 *
 * $Id: ViewAdapter.java 3902 2010-10-04 11:08:07Z epietrig $
 */
package fr.inria.zvtm.event;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.glyphs.Glyph;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;

/**
 * An abstract adapter class for receiving view events. The methods in this
 * class are empty. This class exists as convenience for creating listener
 * objects.
 *
 * @author Emmanuel Pietriga
 */
public class ViewAdapter implements ViewListener {

    @Override
    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {
    }

    @Override
    public void press2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void release2(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void click2(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {
    }

    @Override
    public void press3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void release3(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void click3(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {
    }

    @Override
    public void mouseMoved(ViewPanel v, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void mouseDragged(ViewPanel v, int mod, int buttonNumber, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void mouseWheelMoved(ViewPanel v, short wheelDirection, int jpx, int jpy, MouseWheelEvent e) {
    }

    @Override
    public void enterGlyph(Glyph g) {
    }

    @Override
    public void exitGlyph(Glyph g) {
    }

    @Override
    public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e) {
    }

    @Override
    public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e) {
    }

    @Override
    public void Krelease(ViewPanel v, char c, int code, int mod, KeyEvent e) {
    }

    @Override
    public void viewActivated(View v) {
    }

    @Override
    public void viewDeactivated(View v) {
    }

    @Override
    public void viewIconified(View v) {
    }

    @Override
    public void viewDeiconified(View v) {
    }

    @Override
    public void viewClosing(View v) {
    }

}
