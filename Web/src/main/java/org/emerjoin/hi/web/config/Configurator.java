package org.emerjoin.hi.web.config;


import org.w3c.dom.Element;

import java.util.Map;

/**
 *
 * @author Mário Júnior
 */
public interface Configurator {

    /**
     *
     * @param configs
     * @param elements a <code>Map</code> containing elements with the desired tags
     * @param document the root Element of the configuration XML.
     * @throws BadConfigException something is wrong with the configuration
     */
    public void doConfig(AppConfigurations configs, Map<String, Element> elements, Element document) throws BadConfigException;

}
