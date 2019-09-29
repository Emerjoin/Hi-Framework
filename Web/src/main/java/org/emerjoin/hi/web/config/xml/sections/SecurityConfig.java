package org.emerjoin.hi.web.config.xml.sections;

import org.emerjoin.hi.web.config.*;
import org.emerjoin.hi.web.config.xml.ConfigSection;
import org.emerjoin.xmleasy.XMLEasy;
import org.w3c.dom.Element;

import java.util.Map;
import java.util.Optional;

/**
 * @author Mário Júnior
 */
@ConfigSection(tags = "security")
public class SecurityConfig implements Configurator {

    @Override
    public void doConfig(AppConfigurations configs, Map<String, Element> elements, Element document) throws BadConfigException {
        Security securityConfig = configs.getSecurityConfig();
        Security.ContentSecurityPolicy contentPolicy = securityConfig.getContentPolicy();
        Element xmlElement = elements.get("security");
        XMLEasy xmlEasy = XMLEasy.it(xmlElement);
        xmlEasy.ifChild("content-security-policy").then(contentPolicyXml -> {
            contentPolicyXml.getContentIfPresent("reporting-url",contentPolicy::setReportingUrl);
            contentPolicyXml.ifChild("deny-iframe-embedding",element -> {
                contentPolicy.setDenyIframeEmbeding(Boolean.parseBoolean(element.
                        getContent()));
            });
            contentPolicyXml.getContentIfPresent("block-mixed-content",
                    val -> contentPolicy.setBlockMixedContent(Boolean.
                            parseBoolean(val)));
            contentPolicyXml.ifChild("policy-allow", allowXmlElement -> {
                allowXmlElement.ifChild("navigation", element -> {
                    element.optionalAttribute("to")
                            .ifPresent(contentPolicy::setNavigateToDirective);
                    element.getContentIfPresent("form-action",contentPolicy::
                            setFormActionDirective);
                });
                allowXmlElement.ifChild("content", element -> {
                    Optional<String> defaultContentSource = element.optionalAttribute("from");
                    defaultContentSource.ifPresent(contentSource -> {
                        contentPolicy.setDefaultContentSource(defaultContentSource.get());
                    });
                    element.ifChild("images",it -> contentPolicy.setImageContentSource(it.attribute("from")));
                    element.ifChild("scripts",it -> contentPolicy.setScriptContentSource(it.attribute("from")));
                    element.ifChild("styles",it -> contentPolicy.setStyleContentSource(it.attribute("from")));
                    element.ifChild("media",it -> contentPolicy.setMediaContentSource(it.attribute("from")));
                    element.ifChild("objects",it -> contentPolicy.setObjectContentSource(it.attribute("from")));
                });
            });

        }).eval();



    }

}
