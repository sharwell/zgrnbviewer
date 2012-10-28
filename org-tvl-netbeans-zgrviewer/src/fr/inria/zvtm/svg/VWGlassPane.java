package fr.inria.zvtm.svg;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseMotionAdapter;
import javax.swing.*;

class VWGlassPane extends JComponent {

    static final int BAR_WIDTH = 200;
    static final int BAR_HEIGHT = 10;
    static final AlphaComposite GLASS_ALPHA = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f);
    static final Color MSG_COLOR = Color.DARK_GRAY;
    GradientPaint PROGRESS_GRADIENT = new GradientPaint(0, 0, Color.ORANGE, 0, BAR_HEIGHT, Color.BLUE);
    String msg = Messages.EMPTY_STRING;
    int msgX = 0;
    int msgY = 0;
    int completion = 0;
    int prX = 0;
    int prY = 0;
    int prW = 0;
    Viewer application;

    VWGlassPane(Viewer app) {
        super();
        this.application = app;
        addMouseListener(new MouseAdapter() {
        });
        addMouseMotionListener(new MouseMotionAdapter() {
        });
        addKeyListener(new KeyAdapter() {
        });
    }

    public void setValue(int c) {
        completion = c;
        prX = application.panelWidth / 2 - BAR_WIDTH / 2;
        prY = application.panelHeight / 2 - BAR_HEIGHT / 2;
        prW = (int)(BAR_WIDTH * ((float)completion) / 100.0f);
        PROGRESS_GRADIENT = new GradientPaint(0, prY, Color.LIGHT_GRAY, 0, prY + BAR_HEIGHT, Color.DARK_GRAY);
        repaint(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }

    public void setLabel(String m) {
        msg = m;
        msgX = application.panelWidth / 2 - BAR_WIDTH / 2;
        msgY = application.panelHeight / 2 - BAR_HEIGHT / 2 - 10;
        repaint(msgX, msgY - 50, 400, 70);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        Rectangle clip = g.getClipBounds();
        g2.setComposite(GLASS_ALPHA);
        g2.setColor(Color.WHITE);
        g2.fillRect(clip.x, clip.y, clip.width, clip.height);
        g2.setComposite(AlphaComposite.Src);
        if (!msg.isEmpty()) {
            g2.setColor(MSG_COLOR);
            g2.setFont(Config.GLASSPANE_FONT);
            g2.drawString(msg, msgX, msgY);
        }
        g2.setPaint(PROGRESS_GRADIENT);
        g2.fillRect(prX, prY, prW, BAR_HEIGHT);
        g2.setColor(MSG_COLOR);
        g2.drawRect(prX, prY, BAR_WIDTH, BAR_HEIGHT);
    }

}
