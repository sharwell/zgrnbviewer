package fr.inria.zvtm.svg;

import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

class Config {

    private Config() {
    }

    /*
     * Fonts
     */
    static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);
    static final Font GLASSPANE_FONT = new Font("Arial", Font.PLAIN, 12);
    static final Font SAY_MSG_FONT = new Font("Arial", Font.PLAIN, 24);
    static final Font MONOSPACE_ABOUT_FONT = new Font("Courier", Font.PLAIN, 8);

    /*
     * Other colors
     */
    static final Color SAY_MSG_COLOR = Color.LIGHT_GRAY;
    static Color BACKGROUND_COLOR = Color.WHITE;
    static final Color FADE_REGION_FILL = Color.BLACK;
    static final Color FADE_REGION_STROKE = Color.WHITE;
    static final Color HIGHLIGHT_COLOR = Color.RED;

    /*
     * Overview
     */
    static final int OVERVIEW_WIDTH = 200;
    static final int OVERVIEW_HEIGHT = 200;
    static final Color OBSERVED_REGION_COLOR = Color.GREEN;
    static final float OBSERVED_REGION_ALPHA = 0.5f;
    static final Color OV_BORDER_COLOR = Color.BLACK;
    static final Color OV_INSIDE_BORDER_COLOR = Color.BLACK;

    /*
     * Durations/Animations
     */
    static final int ANIM_MOVE_LENGTH = 300;
    static final int SAY_DURATION = 500;

    /*
     * External resources
     */
    static final String INSITU_LOGO_PATH = "/images/insitu.png";
    static final String INRIA_LOGO_PATH = "/images/inria.png";

    static JMenuBar initMenu(final Viewer app) {
        final JMenuItem exitMI = new JMenuItem("Exit");
        exitMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem openMI = new JMenuItem("Open...");
        openMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem reloadMI = new JMenuItem("Reload");
        reloadMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem exportMI = new JMenuItem("Export...");
        exportMI.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        final JMenuItem aboutMI = new JMenuItem("About...");
        ActionListener a0 = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (e.getSource() == exitMI) {
                    app.exit();
                } else if (e.getSource() == openMI) {
                    app.openFile();
                } else if (e.getSource() == reloadMI) {
                    app.reload();
                } else if (e.getSource() == exportMI) {
                    app.export();
                } else if (e.getSource() == aboutMI) {
                    app.ovm.showAbout();
                }
            }

        };
        JMenuBar jmb = new JMenuBar();
        JMenu fileM = new JMenu("File");
        JMenu helpM = new JMenu("Help");
        fileM.add(openMI);
        fileM.add(reloadMI);
        fileM.addSeparator();
        fileM.add(exportMI);
        fileM.addSeparator();
        fileM.add(exitMI);
        helpM.add(aboutMI);
        jmb.add(fileM);
        jmb.add(helpM);
        openMI.addActionListener(a0);
        reloadMI.addActionListener(a0);
        exportMI.addActionListener(a0);
        exitMI.addActionListener(a0);
        aboutMI.addActionListener(a0);
        return jmb;
    }

}
