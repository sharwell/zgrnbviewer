/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tvl.netbeans.zgrviewer;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;

public class GraphVizDataObject extends MultiDataObject {

    public GraphVizDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/x-graphviz", true);
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(
        displayName = "#LBL_GraphViz_EDITOR",
        iconBase = "",
        mimeType = "text/x-graphviz",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "GraphViz",
        position = 1000
    )
    @NbBundle.Messages({
        "LBL_GraphViz_EDITOR=Editor"
    })
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

}
