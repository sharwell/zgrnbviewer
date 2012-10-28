package fr.inria.zvtm.engine;

import java.util.List;

/**
 * Instantiator for StdViewPanel
 */
class StdViewPanelFactory implements PanelFactory {

    @Override
    public ViewPanel getNewInstance(List<Camera> cameras, View v, boolean arfome) {
        return new StdViewPanel(cameras, v, arfome);
    }

}
