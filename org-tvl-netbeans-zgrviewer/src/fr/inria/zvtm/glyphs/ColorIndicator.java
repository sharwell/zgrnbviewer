package fr.inria.zvtm.glyphs;

import java.awt.Color;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

class ColorIndicator extends JPanel {

    Color color;
    String label;
    JPanel p;

    ColorIndicator(String l, Color c) {
        super();
        color = c;
        label = l;
        setBorder(BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), label));
        p = new JPanel();
        p.setBackground(color);
        add(p);
    }

    void setColor(Color c) {
        color = c;
        p.setBackground(color);
        repaint();
    }

    Color getColor() {
        return color;
    }

}
