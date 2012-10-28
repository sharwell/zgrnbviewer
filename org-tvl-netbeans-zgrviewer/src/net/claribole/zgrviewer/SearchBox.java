/*
 * FILE: SearchBox.java DATE OF CREATION: Thu Jan 09 15:47:07 2003 Copyright (c)
 * Emmanuel Pietriga, 2002. All Rights Reserved Copyright (c) INRIA, 2004-2011.
 * All Rights Reserved Licensed under the GNU LGPL. For full terms see the file
 * COPYING. $Id: SearchBox.java 4276 2011-02-25 07:47:51Z epietrig $
 */
package net.claribole.zgrviewer;

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

class SearchBox extends JFrame implements ActionListener, KeyListener {

    static int FRAME_WIDTH = 300;
    static int FRAME_HEIGHT = 110;
    GraphicsManager grMngr;
    JButton prevBt, nextBt;
    JTextField searchText;

    @SuppressWarnings("LeakingThisInConstructor")
    SearchBox(GraphicsManager gm) {
        super();
        this.grMngr = gm;
        Container cp = this.getContentPane();
        cp.setLayout(new GridLayout(2, 1));
        JPanel p1 = new JPanel();
        JPanel p2 = new JPanel();
        cp.add(p1);
        cp.add(p2);
        p1.add(new JLabel("Find:"));
        searchText = new JTextField(32);
        p1.add(searchText);
        searchText.addKeyListener(this);
        prevBt = new JButton("Previous");
        p2.add(prevBt);
        prevBt.addActionListener(this);
        nextBt = new JButton("Next");
        p2.add(nextBt);
        nextBt.addActionListener(this);
        //window
        WindowListener w0 = new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
            }

        };
        this.addWindowListener(w0);
        this.setTitle("Find");
        this.pack();
        this.setResizable(false);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == prevBt) {
            grMngr.search(searchText.getText(), -1);
        } else {
            grMngr.search(searchText.getText(), 1);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            grMngr.search(searchText.getText(), 1);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

}
