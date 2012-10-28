package fr.inria.zvtm.engine;

import java.util.List;

/**
 * Instantiator for GLViewPanel
 */
class GLViewPanelFactory implements PanelFactory {

    @Override
    public ViewPanel getNewInstance(List<Camera> cameras, View v, boolean arfome) {
        return new GLViewPanel(cameras, v, arfome);
    }

}
