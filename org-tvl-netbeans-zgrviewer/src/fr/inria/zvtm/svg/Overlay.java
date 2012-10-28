package fr.inria.zvtm.svg;

import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.event.ViewListener;
import fr.inria.zvtm.glyphs.Glyph;
import fr.inria.zvtm.glyphs.RImage;
import fr.inria.zvtm.glyphs.VImage;
import fr.inria.zvtm.glyphs.VRectangle;
import fr.inria.zvtm.glyphs.VText;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import javax.swing.ImageIcon;

class Overlay implements ViewListener {

    Viewer application;
    boolean showingAbout = false;
    VRectangle fadeAbout;
    VImage insituLogo, inriaLogo;
    VText[] aboutLines;
    VRectangle fadedRegion;
    VText sayGlyph;

    Overlay(Viewer app) {
        this.application = app;
    }

    void init() {
        fadedRegion = new VRectangle(0, 0, 0, 10, 10, Config.FADE_REGION_FILL, Config.FADE_REGION_STROKE, 0.85f);
        application.aboutSpace.addGlyph(fadedRegion);
        fadedRegion.setVisible(false);
        sayGlyph = new VText(0, -10, 0, Config.SAY_MSG_COLOR, Messages.EMPTY_STRING, VText.TEXT_ANCHOR_MIDDLE);
        sayGlyph.setFont(Config.SAY_MSG_FONT);
        application.aboutSpace.addGlyph(sayGlyph);
        sayGlyph.setVisible(false);
    }

    void showAbout() {
        if (!showingAbout) {
            fadeAbout = new VRectangle(0, 0, 0, Math.round(application.panelWidth / 1.05), Math.round(application.panelHeight / 1.5),
                                       Config.FADE_REGION_FILL, Config.FADE_REGION_STROKE, 0.85f);
            aboutLines = new VText[4];
            aboutLines[0] = new VText(0, 150, 0, Color.WHITE, Messages.APP_NAME, VText.TEXT_ANCHOR_MIDDLE, 4.0f);
            aboutLines[1] = new VText(0, 110, 0, Color.WHITE, Messages.V + Messages.VERSION, VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[2] = new VText(0, 40, 0, Color.WHITE, Messages.AUTHORS, VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            RImage.setReflectionHeight(0.7f);
            inriaLogo = new RImage(-150, -40, 0, (new ImageIcon(this.getClass().getResource(Config.INRIA_LOGO_PATH))).getImage(), 1.0f);
            insituLogo = new RImage(200, -40, 0, (new ImageIcon(this.getClass().getResource(Config.INSITU_LOGO_PATH))).getImage(), 1.0f);
            aboutLines[3] = new VText(0, -200, 0, Color.WHITE, Messages.ABOUT_DEPENDENCIES, VText.TEXT_ANCHOR_MIDDLE, 2.0f);
            aboutLines[3].setFont(Config.MONOSPACE_ABOUT_FONT);
            application.aboutSpace.addGlyph(fadeAbout);
            application.aboutSpace.addGlyph(inriaLogo);
            application.aboutSpace.addGlyph(insituLogo);
            for (int i = 0; i < aboutLines.length; i++) {
                application.aboutSpace.addGlyph(aboutLines[i]);
            }
            showingAbout = true;
        }
        application.mView.setActiveLayer(1);
        if (application.nm.ovPortal.isVisible()) {
            application.nm.toggleOverview();
        }
    }

    void hideAbout() {
        if (showingAbout) {
            showingAbout = false;
            if (insituLogo != null) {
                application.aboutSpace.removeGlyph(insituLogo);
                insituLogo = null;
            }
            if (inriaLogo != null) {
                application.aboutSpace.removeGlyph(inriaLogo);
                inriaLogo = null;
            }
            if (fadeAbout != null) {
                application.aboutSpace.removeGlyph(fadeAbout);
                fadeAbout = null;
            }
            for (int i = 0; i < aboutLines.length; i++) {
                if (aboutLines[i] != null) {
                    application.aboutSpace.removeGlyph(aboutLines[i]);
                    aboutLines[i] = null;
                }
            }
        }
        application.mView.setActiveLayer(0);
        application.nm.ovPortal.setVisible(true);
    }

    void say(final String msg) {
        final fr.inria.zvtm.engine.SwingWorker worker = new fr.inria.zvtm.engine.SwingWorker() {

            @Override
            public Object construct() {
                showMessage(msg);
                sleep(Config.SAY_DURATION);
                hideMessage();
                return null;
            }

        };
        worker.start();
    }

    void showMessage(String msg) {
        synchronized (this) {
            fadedRegion.setWidth(application.panelWidth / 2 - 1);
            fadedRegion.setHeight(50);
            sayGlyph.setText(msg);
            fadedRegion.setVisible(true);
            sayGlyph.setVisible(true);
        }
    }

    void hideMessage() {
        synchronized (this) {
            fadedRegion.setVisible(false);
            sayGlyph.setVisible(false);
        }
    }

    @Override
    public void press1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void release1(ViewPanel v, int mod, int jpx, int jpy, MouseEvent e) {
    }

    @Override
    public void click1(ViewPanel v, int mod, int jpx, int jpy, int clickNumber, MouseEvent e) {
        hideAbout();
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
    public void Kpress(ViewPanel v, char c, int code, int mod, KeyEvent e) {
        hideAbout();
    }

    @Override
    public void Ktype(ViewPanel v, char c, int code, int mod, KeyEvent e) {
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
        application.exit();
    }

}
