/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tvl.netbeans.zgrviewer;

import fr.inria.zvtm.widgets.PieMenu;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import javax.swing.*;
import net.claribole.zgrviewer.*;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileAttributeEvent;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileRenameEvent;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

import static org.tvl.netbeans.zgrviewer.Bundle.*;

@MultiViewElement.Registration(
    displayName = "#LBL_GraphViz_VISUAL",
    iconBase = "",
    mimeType = "text/x-graphviz",
    persistenceType = TopComponent.PERSISTENCE_NEVER,
    preferredID = "GraphVizVisual",
    position = 2000
)
@Messages({
    "LBL_GraphViz_VISUAL=Visual",
    "CTL_StdNavModeCaption=Standard Navigation Mode",
    "CTL_FlNavModeCaption=Fading Lens Navigation Mode",
    "CTL_DmNavModeCaption=Drag Mag Navigation Mode",
    "CTL_PlNavModeCaption=Probing Lens Navigation Mode",
    "CTL_HighlightModeCaption=Highlight Mode",
    "CTL_BringAndGoModeCaption=Bring and Go Mode",
    "CTL_LinkSlidingModeCaption=Link Sliding Mode",
    "CTL_EditModeCaption=Edit Mode"
})
public final class GraphVizVisualElement extends JPanel implements MultiViewElement, ZGRApplication {

    private GraphVizDataObject obj;
    private JToolBar toolbar = new JToolBar();
    private transient MultiViewElementCallback callback;

    private GraphicsManager grMngr;
    private DOTManager dotMngr;
    private GVLoader gvLdr;
    private PieMenu mainPieMenu;

    private PieMenu subPieMenu;

    private final Listener listener;

    private final JToggleButton stdNavButton;
    private final JToggleButton flNavButton;
    private final JToggleButton dmNavButton;
    private final JToggleButton plNavButton;
    private final JToggleButton highlightButton;
    private final JToggleButton bringAndGoButton;
    private final JToggleButton linkSlidingButton;
    private final JToggleButton editButton;

    public GraphVizVisualElement(Lookup lkp) {
        obj = lkp.lookup(GraphVizDataObject.class);
        assert obj != null;

        initComponents();

        listener = new Listener();

        stdNavButton = new JToggleButton(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/stdnav24b.png")), false);
        stdNavButton.setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/stdnav24g.png")));
        stdNavButton.setToolTipText(CTL_StdNavModeCaption());
        initButton(stdNavButton);

        flNavButton = new JToggleButton(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/flnav24b.png")), false);
        flNavButton.setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/flnav24g.png")));
        flNavButton.setToolTipText(CTL_FlNavModeCaption());
        initButton(flNavButton);

        dmNavButton = new JToggleButton(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/dmnav24b.png")), false);
        dmNavButton.setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/dmnav24g.png")));
        dmNavButton.setToolTipText(CTL_DmNavModeCaption());
        initButton(dmNavButton);

        plNavButton = new JToggleButton(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/plnav24b.png")), false);
        plNavButton.setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/plnav24g.png")));
        plNavButton.setToolTipText(CTL_PlNavModeCaption());
        initButton(plNavButton);

        highlightButton = new JToggleButton(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/hl24b.png")), false);
        highlightButton.setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/hl24g.png")));
        highlightButton.setToolTipText(CTL_HighlightModeCaption());
        initButton(highlightButton);

        bringAndGoButton = new JToggleButton(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/fl24b.png")), false);
        bringAndGoButton.setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/fl24g.png")));
        bringAndGoButton.setToolTipText(CTL_BringAndGoModeCaption());
        initButton(bringAndGoButton);

        linkSlidingButton = new JToggleButton(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/ls24b.png")), false);
        linkSlidingButton.setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/ls24g.png")));
        linkSlidingButton.setToolTipText(CTL_LinkSlidingModeCaption());
        initButton(linkSlidingButton);

        editButton = new JToggleButton(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/edit24b.png")), false);
        editButton.setSelectedIcon(ImageUtilities.image2Icon(ImageUtilities.loadImage("net/claribole/zgrviewer/resources/edit24g.png")));
        editButton.setToolTipText(CTL_EditModeCaption());
        initButton(editButton);

        JToolBar.Separator separator1 = new JToolBar.Separator();
        separator1.setOrientation(JSeparator.VERTICAL);

        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(separator1);
        toolbar.add(Box.createHorizontalStrut(4));
        toolbar.add(stdNavButton);
        toolbar.add(flNavButton);
        toolbar.add(dmNavButton);
        toolbar.add(plNavButton);
        toolbar.add(highlightButton);
        toolbar.add(bringAndGoButton);
        toolbar.add(linkSlidingButton);
        toolbar.add(editButton);

        initConfig();
        JPanel panel = initGUI(true);
        updateButtons();

        this.setLayout(new BorderLayout());
        this.add(panel);

        obj.getPrimaryFile().addFileChangeListener(new FileChangeListener() {

            @Override
            public void fileFolderCreated(FileEvent fe) {
            }

            @Override
            public void fileDataCreated(FileEvent fe) {
            }

            @Override
            public void fileChanged(FileEvent fe) {
                reloadDocument();
            }

            @Override
            public void fileDeleted(FileEvent fe) {
            }

            @Override
            public void fileRenamed(FileRenameEvent fe) {
            }

            @Override
            public void fileAttributeChanged(FileAttributeEvent fe) {
            }
            
        });

        reloadDocument();
    }

    private void reloadDocument() {
        grMngr.reset();
        gvLdr.loadFile(FileUtil.toFile(obj.getPrimaryFile()), DOTManager.DOT_PROGRAM, false);
    }

    @Override
    public GraphicsManager getGraphicsManager() {
        return grMngr;
    }

    @Override
    public GVLoader getGVLoader() {
        return gvLdr;
    }

    @Override
    public void about() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void initConfig(){
        grMngr = new GraphicsManager(this);
        dotMngr = new DOTManager(grMngr);
        gvLdr = new GVLoader(this, grMngr, dotMngr);
        ConfigManager.loadConfig();
        ConfigManager.initPlugins(this);
    }

    JPanel initGUI(boolean acc){
        ConfigManager.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_INITIALIZING);
		//Utils.initLookAndFeel();
//		JMenuBar jmb = initViewMenu(acc);
//		grMngr.createFrameView(grMngr.createZVTMelements(false), acc ? View.OPENGL_VIEW : View.STD_VIEW, jmb);
        JPanel panel = grMngr.createPanelView(grMngr.createZVTMelements(false), 800, 600);
        ConfigManager.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_VIEW_CREATED);
		grMngr.parameterizeView(new ZgrvEvtHdlr(this, this.grMngr));
		ConfigManager.notifyPlugins(Plugin.NOTIFY_PLUGIN_GUI_INITIALIZED);
        return panel;
	}

//    public void displayMainPieMenu(boolean b){
//		if (b){
//			PieMenuFactory.setItemFillColor(ConfigManager.PIEMENU_FILL_COLOR);
//			PieMenuFactory.setItemBorderColor(ConfigManager.PIEMENU_BORDER_COLOR);
//			PieMenuFactory.setSelectedItemFillColor(ConfigManager.PIEMENU_INSIDE_COLOR);
//			PieMenuFactory.setSelectedItemBorderColor(null);
//			PieMenuFactory.setLabelColor(ConfigManager.PIEMENU_BORDER_COLOR);
//			PieMenuFactory.setFont(ConfigManager.PIEMENU_FONT);
//			if (Utils.osIsWindows() || Utils.osIsMacOS()){PieMenuFactory.setTranslucency(ConfigManager.PIEMENU_MAIN_ALPHA);}
//			PieMenuFactory.setSensitivityRadius(0.5);
//			PieMenuFactory.setAngle(-Math.PI/2.0);
//			PieMenuFactory.setRadius(100);
//			mainPieMenu = PieMenuFactory.createPieMenu(ZGRMessages.mainMenuLabels, ZGRMessages.mainMenuLabelOffsets, 0, grMngr.mainView, grMngr.vsm);
//			Glyph[] items = mainPieMenu.getItems();
//			items[0].setType(ZGRMessages.PM_ENTRY);
//			items[1].setType(ZGRMessages.PM_SUBMN);
//			items[2].setType(ZGRMessages.PM_ENTRY);
//			items[3].setType(ZGRMessages.PM_SUBMN);
//		}
//		else {
//			mainPieMenu.destroy(0);
//			mainPieMenu = null;
//		}
//	}
//
//    public void displaySubMenu(Glyph menuItem, boolean b){
//		if (b){
//			int index = mainPieMenu.getItemIndex(menuItem);
//			if (index != -1){
//				String label = mainPieMenu.getLabels()[index].getText();
//				PieMenuFactory.setFont(ConfigManager.PIEMENU_FONT);
//				PieMenuFactory.setItemFillColor(ConfigManager.PIEMENU_FILL_COLOR);
//				PieMenuFactory.setItemBorderColor(ConfigManager.PIEMENU_BORDER_COLOR);
//				PieMenuFactory.setSelectedItemFillColor(ConfigManager.PIEMENU_INSIDE_COLOR);
//				PieMenuFactory.setSelectedItemBorderColor(null);
//				PieMenuFactory.setSensitivityRadius(1.0);
//				if (Utils.osIsWindows() || Utils.osIsMacOS()){PieMenuFactory.setTranslucency(ConfigManager.PIEMENU_SUB_ALPHA);}
//				PieMenuFactory.setRadius(100);
//				Glyph[] items;
//				if (label == ZGRMessages.PM_FILE){
//					subPieMenu = PieMenuFactory.createPieMenu(ZGRMessages.fileMenuLabels, ZGRMessages.fileMenuLabelOffsets, 0 , grMngr.mainView, grMngr.vsm);
//					items = subPieMenu.getItems();
//					for (int i=0;i<items.length;i++){
//						items[i].setType(ZGRMessages.PM_ENTRY);
//					}
//				}
//				else if (label == ZGRMessages.PM_EXPORT){
//					subPieMenu = PieMenuFactory.createPieMenu(ZGRMessages.exportMenuLabels, ZGRMessages.exportMenuLabelOffsets, 0 , grMngr.mainView, grMngr.vsm);
//					items = subPieMenu.getItems();
//					for (int i=0;i<items.length;i++){
//						items[i].setType(ZGRMessages.PM_ENTRY);
//					}
//				}
//			}
//		}
//		else {
//			subPieMenu.destroy(0);
//			subPieMenu = null;
//		}
//	}
//
//    public void pieMenuEvent(Glyph menuItem){
//		int index = mainPieMenu.getItemIndex(menuItem);
//		String label;
//		if (index != -1){
//			label = mainPieMenu.getLabels()[index].getText();
//			if (label == ZGRMessages.PM_BACK){grMngr.moveBack();}
//			else if (label == ZGRMessages.PM_GLOBALVIEW){grMngr.getGlobalView();}
//		}
//		else {
//			index = subPieMenu.getItemIndex(menuItem);
//			if (index != -1){
//				label = subPieMenu.getLabels()[index].getText();
//				if (label == ZGRMessages.PM_OPENDOTSVG){gvLdr.open(DOTManager.DOT_PROGRAM, false);}
//				else if (label == ZGRMessages.PM_OPENNEATOSVG){gvLdr.open(DOTManager.NEATO_PROGRAM, false);}
//				else if (label == ZGRMessages.PM_OPENCIRCOSVG){gvLdr.open(DOTManager.CIRCO_PROGRAM, false);}
//				else if (label == ZGRMessages.PM_OPENTWOPISVG){gvLdr.open(DOTManager.TWOPI_PROGRAM, false);}
//				else if (label == ZGRMessages.PM_OPENSVG){gvLdr.openSVGFile();}
////				else if (label == ZGRMessages.PM_OPENOTHER){gvLdr.openOther();}
//				else if (label == ZGRMessages.PM_EXPSVG){saveSVG();}
//				else if (label == ZGRMessages.PM_EXPPNG){savePNG();}
//				else if (label == ZGRMessages.PM_EXPPRINT){print();}
//			}
//		}
//	}
//
//    void savePNG(){
//		final double[] vr = grMngr.mainView.getVisibleRegion(grMngr.mSpace.getCamera(0));
//		SwingWorker sw = new SwingWorker(){
//			public 	Object construct(){
//				new PNGExportWindow(vr[2] - vr[0], vr[1]-vr[3], grMngr);
//				return null;
//			}
//		};
//		sw.start();
//	}
//
//    void saveSVG(){
//        throw new UnsupportedOperationException("Not implemented yet.");
////		final JFileChooser fc=new JFileChooser(ConfigManager.m_LastExportDir!=null ? ConfigManager.m_LastExportDir : ConfigManager.m_PrjDir);
////		fc.setDialogTitle("Export SVG");
////		int returnVal=fc.showSaveDialog(grMngr.mainView.getFrame());
////		if (returnVal==JFileChooser.APPROVE_OPTION) {
////			final SwingWorker worker=new SwingWorker(){
////				public Object construct(){
////					exportSVG(fc.getSelectedFile());
////					return null;
////				}
////			};
////			worker.start();
////		}
//	}
//
//    /*export the entire RDF graph as SVG locally*/
//    public void exportSVG(File f) {
//		if (f!=null){
//			grMngr.mainView.setCursorIcon(java.awt.Cursor.WAIT_CURSOR);
//			ConfigManager.m_LastExportDir=f.getParentFile();
//			setStatusBarText("Exporting to SVG "+f.toString()+" ...");
//			if (f.exists()){f.delete();}
//			fr.inria.zvtm.svg.SVGWriter svgw=new fr.inria.zvtm.svg.SVGWriter();
//			Document d = svgw.exportVirtualSpace(grMngr.mSpace, new DOMImplementationImpl(), f);
////			Utils.serialize(d,f);
//			setStatusBarText("Exporting to SVG "+f.toString()+" ...done");
//			grMngr.mainView.setCursorIcon(java.awt.Cursor.CUSTOM_CURSOR);
//		}
//	}
//
//	void print(){
//		final double[] vr = grMngr.mainView.getVisibleRegion(grMngr.mSpace.getCamera(0));
//		SwingWorker sw = new SwingWorker(){
//			public 	Object construct(){
//				new PrintWindow(vr[2] - vr[0], vr[1]-vr[3], grMngr);
//				return null;
//			}
//		};
//		sw.start();
//	}
//
//	public PieMenu getMainPieMenu(){
//		return mainPieMenu;
//	}
//
//	public PieMenu getSubPieMenu(){
//		return subPieMenu;
//	}

    @Override
    public String getName() {
        return "GraphVizVisualElement";
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables


    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return toolbar;
    }

    @Override
    public Action[] getActions() {
        return new Action[0];
    }

    @Override
    public Lookup getLookup() {
        return obj.getLookup();
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
        grMngr.closeView();
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return UndoRedo.NONE;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    @Override
    public void setStatusBarText(String string) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void initButton(AbstractButton button) {
        button.addActionListener(listener);
        button.addMouseListener(listener);

        if (!("Windows".equals(UIManager.getLookAndFeel().getID())
            && (button instanceof JToggleButton))) {
            button.setBorderPainted(false);
        }

        button.setOpaque(false);
        button.setFocusPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));
    }

    private void updateButtons() {
        stdNavButton.setSelected(getGraphicsManager().getToolPalette().isStdNavMode());
        flNavButton.setSelected(getGraphicsManager().getToolPalette().isFadingLensNavMode());
        dmNavButton.setSelected(getGraphicsManager().getToolPalette().isDragMagNavMode());
        plNavButton.setSelected(getGraphicsManager().getToolPalette().isProbingLensNavMode());
        highlightButton.setSelected(getGraphicsManager().getToolPalette().isHighlightMode());
        bringAndGoButton.setSelected(getGraphicsManager().getToolPalette().isBringAndGoMode());
        linkSlidingButton.setSelected(getGraphicsManager().getToolPalette().isLinkSlidingMode());
        editButton.setSelected(getGraphicsManager().getToolPalette().isEditMode());
    }

    private class Listener extends MouseAdapter implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            if (e.getSource() == stdNavButton) {
                selectMode(ToolPalette.STD_NAV_MODE);
            } else if (e.getSource() == flNavButton) {
                selectMode(ToolPalette.FL_NAV_MODE);
            } else if (e.getSource() == dmNavButton) {
                selectMode(ToolPalette.DM_NAV_MODE);
            } else if (e.getSource() == plNavButton) {
                selectMode(ToolPalette.PL_NAV_MODE);
            } else if (e.getSource() == highlightButton) {
                selectMode(ToolPalette.HIGHLIGHT_MODE);
            } else if (e.getSource() == bringAndGoButton) {
                selectMode(ToolPalette.BRING_AND_GO_MODE);
            } else if (e.getSource() == linkSlidingButton) {
                selectMode(ToolPalette.LINK_SLIDING_MODE);
            } else if (e.getSource() == editButton) {
                selectMode(ToolPalette.EDIT_MODE);
            }
        }

        private void selectMode(short mode) {
            getGraphicsManager().getToolPalette().selectButton(mode);
            updateButtons();
        }

    }

}

