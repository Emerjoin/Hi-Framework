package org.emerjoin.hi.web.config.xml.sections;

import org.emerjoin.hi.web.config.*;
import org.emerjoin.hi.web.config.xml.ConfigSection;
import org.emerjoin.xmleasy.XMLEasy;
import org.w3c.dom.Element;

import java.util.Map;

/**
 * @author Mario Junior.
 */
@ConfigSection(tags = "events")
public class EventsConfig implements Configurator {
    
    @Override
    public void doConfig(AppConfigurations configs, Map<String, Element> elements, Element document) throws BadConfigException {
        Element xmlElement = elements.get("events");
        XMLEasy xmlEasy = XMLEasy.it(xmlElement);
        String interval = xmlEasy.child("reconnect-interval").getContent();
        Events eventsConfig = configs.getEventsConfig();
        eventsConfig.setReconnectInterval(Long.parseLong(
                interval));
    }

}
