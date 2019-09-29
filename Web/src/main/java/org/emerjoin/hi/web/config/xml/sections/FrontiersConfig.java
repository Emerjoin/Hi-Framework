package org.emerjoin.hi.web.config.xml.sections;

import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.config.BadConfigException;
import org.emerjoin.hi.web.config.Configurator;
import org.emerjoin.hi.web.config.Frontiers;
import org.emerjoin.hi.web.config.xml.ConfigSection;
import org.emerjoin.xmleasy.XMLEasy;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Map;

/**
 * @author Mário Júnior
 */
@ConfigSection(tags = "frontiers")
public class FrontiersConfig implements Configurator {

    @Override
    public void doConfig(AppConfigurations configs, Map<String, Element> elements, Element document) throws BadConfigException {
        Frontiers frontiersConfig = configs.getFrontiersConfig();
        Element xmlElement = elements.get("frontiers");

        XMLEasy xmlEasy = XMLEasy.it(xmlElement);
        xmlEasy.ifChild("default-timeout").then(defaultTimeoutXml -> {
            frontiersConfig.setDefaultTimeout(Long.parseLong(defaultTimeoutXml.getContent()));
        }).eval();

        xmlEasy.ifChild("security").then(securityXml -> {
            Frontiers.Security.CrossSiteRequestForgery crossSiteRequestForgery = frontiersConfig.getSecurity().getCrossSiteRequestForgery();
            securityXml.ifChild("cross-site-request-forgery").then(crossSiteRequestForgeryXml -> {
                Frontiers.Security.CrossSiteRequestForgery.Token token = crossSiteRequestForgery.getToken();
                XMLEasy csrfTokenXml = crossSiteRequestForgeryXml.child("token");
                token.setJwtAlgorithm(csrfTokenXml.child("jwt-algorithm").getContent());
                token.setJwtPassphrase(csrfTokenXml.child("jwt-passphrase").getContent());
                token.setSecureRandomSize(Integer.parseInt(csrfTokenXml.child("secure-random-size")
                        .getContent()));

                Frontiers.Security.CrossSiteRequestForgery.Cookie cookie = crossSiteRequestForgery.getCookie();
                XMLEasy csrfCookie = crossSiteRequestForgeryXml.child("cookie");
                cookie.setHttpOnly(Boolean.parseBoolean(csrfCookie.child("http-only").getContent()));
                cookie.setHttpOnly(Boolean.parseBoolean(csrfCookie.child("secure").getContent()));
            }).eval();
        }).eval();

    }

}
