/*   AUTHOR : Romain Primet (romain.primet@inria.fr)
 *
 *  (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en Automatique), 2011.
 *  Licensed under the GNU LGPL. For full terms see the file COPYING.
 *
 * $Id:$
 */

package fr.inria.zvtm.tests;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import fr.inria.zvtm.engine.Camera;
import fr.inria.zvtm.engine.View;
import fr.inria.zvtm.engine.ViewPanel;
import fr.inria.zvtm.engine.VirtualSpace;
import fr.inria.zvtm.engine.VirtualSpaceManager;
import fr.inria.zvtm.event.SwingListener;
import fr.inria.zvtm.glyphs.VSwingComponent;

/**
 * Testing VSwingComponent
 */
public class SwingTest {
    JPanel drawnPanel;

    final VirtualSpaceManager vsm = VirtualSpaceManager.INSTANCE;
    Camera cam;
    View view;
    VirtualSpace testSpace;
    int foo = 0;
    
    public SwingTest() throws Exception {
        TableModel dataModel = new AbstractTableModel() {
            public int getColumnCount() { return 10; }
            public int getRowCount() { return 10;}
            public Object getValueAt(int row, int col) { return new Integer(row*col+foo); }
        };
        JTable table = new JTable(dataModel);
        table.setVisible(true);
        new Timer(500, new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                foo += 1;
            }
        }).start();
        drawnPanel = new JPanel();
        drawnPanel.setBounds(0,0,300,300);
        drawnPanel.add(table);
        JButton btn = new JButton("42");
        btn.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent ae){
                System.out.println("I got clicked!!");
            }
        });
        drawnPanel.add(btn);
        drawnPanel.add(new JTextField("if(cesfo) assert(!frites)"));
        SwingUtilities.invokeAndWait(new Runnable(){
            public void run(){
                drawnPanel.doLayout();
            }
        });
        VSwingComponent vsc = new VSwingComponent(drawnPanel);
        testSpace = vsm.addVirtualSpace("testSpace");
        cam = testSpace.addCamera();
        cam.setZoomFloor(-90);
        cam.altitudeOffset(-20); //check that event redirection works correctly at any zoom level
        vsc.move(100, -50);
        testSpace.addGlyph(vsc);
        view = vsm.addFrameView(Arrays.asList(cam),
                "VSwingTest", View.STD_VIEW, 800, 600, true);
        view.setActiveRepaintInterval(500);
        view.getCursor().setColor(Color.GREEN);
        view.setListener(new MyEventListener(this));
    }

    public static void main(String[] args) throws Exception {
        new SwingTest();    
    }
}

