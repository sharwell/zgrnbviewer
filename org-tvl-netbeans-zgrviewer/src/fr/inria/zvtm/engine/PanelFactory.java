/*
 * AUTHOR : Emmanuel Pietriga (emmanuel.pietriga@inria.fr)
 *
 * (c) COPYRIGHT INRIA (Institut National de Recherche en Informatique et en
 * Automatique), 2011. Licensed under the GNU LGPL. For full terms see the file
 * COPYING.
 *
 * $Id: $
 */
package fr.inria.zvtm.engine;

import java.util.List;

public interface PanelFactory {

    public ViewPanel getNewInstance(List<Camera> cameras, View v, boolean arfome);

}
