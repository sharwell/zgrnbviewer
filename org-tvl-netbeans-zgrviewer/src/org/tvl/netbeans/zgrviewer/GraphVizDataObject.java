/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tvl.netbeans.zgrviewer;

import java.io.IOException;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;

@NbBundle.Messages({
    "GraphVizResolver=GraphViz Files",
    "CTL_SourceTabCaption=&Source"
})
@MIMEResolver.ExtensionRegistration(
    displayName = "#GraphVizResolver",
    extension="dot",
    mimeType = "text/x-graphviz",
    showInFileChooser = "#GraphVizResolver")
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
        displayName = "#CTL_SourceTabCaption",
        iconBase = "",
        mimeType = "text/x-graphviz",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "GraphViz",
        position = 1000
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }

}
