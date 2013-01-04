/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tvl.netbeans.zgrviewer;

import java.io.IOException;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.ExtensionList;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.loaders.UniFileLoader;
import org.openide.util.NbBundle;

/**
 *
 * @author Sam Harwell
 */
@NbBundle.Messages({
    "GraphVizLoader_Name=GraphViz Source Object"
})
@DataObject.Registration(
    displayName="#GraphVizLoader_Name",
    mimeType="text/x-graphviz",
    position=99999)
public class GraphVizDataLoader extends UniFileLoader {

    public GraphVizDataLoader() {
        super("org.antlr.works.editor.grammar.GraphVizDataObject");
    }

    @Override
    protected void initialize() {
        super.initialize();
        ExtensionList extensions = new ExtensionList();
        extensions.addExtension(".dot");
        extensions.addMimeType("text/x-graphviz");
        setExtensions(extensions);
    }

    @Override
    protected String actionsContext() {
        return "Loaders/text/x-graphviz/Actions/";
    }

    @Override
    protected String defaultDisplayName() {
        return Bundle.GraphVizLoader_Name();
    }

    @Override
    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException, IOException {
        if (getExtensions().isRegistered(primaryFile)) {
            return new GraphVizDataObject(primaryFile, this);
        }

        return null;
    }

    @Override
    protected FileObject findPrimaryFile(FileObject fo) {
        // never recognize folders
        if (fo.isFolder()) {
            return null;
        }

        return super.findPrimaryFile(fo);
    }

    @Override
    protected Entry createPrimaryEntry(MultiDataObject obj, FileObject primaryFile) {
        return super.createPrimaryEntry(obj, primaryFile);
    }

}
