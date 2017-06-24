package org.emerjoin.hi.web.config.xml;

import com.sun.org.apache.xerces.internal.jaxp.validation.XMLSchemaFactory;
import org.emerjoin.hi.web.config.*;
import org.emerjoin.hi.web.exceptions.HiException;
import org.emerjoin.hi.web.internal.Logging;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.slf4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Mario Junior.
 */
@ApplicationScoped
public class XMLConfigProvider implements ConfigProvider {

    private String docsPath = null;
    private Logger _log = Logging.getInstance().getLogger(XMLConfigProvider.class);

    private Configurator getConfigurator(Class<? extends Configurator> clazz) throws HiException {

        try{

            Constructor constructor = clazz.getDeclaredConstructor(null);
            Object configurator = constructor.newInstance();
            return (Configurator) configurator;

        }catch (Exception ex){

            throw new BadConfiguratorException("Failed to initialize the configurator "+clazz.getCanonicalName(),ex);

        }

    }


    private Set<Class<?>> getConfigurators(Set<Index> indexSet){

        Set<Class<?>> configSections = new HashSet<>();

        if(indexSet!=null) {

            for (Index index : indexSet) {

                List<AnnotationInstance> instances =
                        index.getAnnotations(DotName.createSimple(ConfigSection.class.getCanonicalName()));

                for (AnnotationInstance an : instances) {

                    String className = an.target().asClass().name().toString();
                    _log.info(String.format("Loading config section class : %s",className));

                    try {

                        Class<?> clazz  = this.getClass().getClassLoader().loadClass(className);
                        configSections.add(clazz);

                    } catch (Throwable ex) {

                        _log.error("Failed to load config section",ex);
                        continue;

                    }
                }
            }
        }

        return configSections;

    }

    public void load(ServletContext servletContext, ServletConfig config, Set<Index> indexSet) throws HiException{

        Set<Class<?>> configurators  = getConfigurators(indexSet);
        Document document = loadDocument(servletContext);
        Element docElement = document.getDocumentElement();

        for(Class clazz : configurators){

            if(clazz.asSubclass(Configurator.class)==null)
                throw new BadConfiguratorException("Class "+clazz.getCanonicalName()+" does not implement the Configurator interface");

            Configurator configurator = getConfigurator(clazz);
            ConfigSection section = (ConfigSection) clazz.getDeclaredAnnotation(ConfigSection.class);

            HashMap<String,Element> elements = new HashMap<>();

            for(String tag: section.tags()){

                NodeList nodeList = docElement.getElementsByTagName(tag);
                if(nodeList.getLength()==0)
                    continue;

                elements.put(tag,(Element) nodeList.item(0));

            }

            if(elements.size()==0) {
                _log.info("Configurator " + clazz.getCanonicalName() + " was skipped. No match for the defined tags");
                continue;
            }


            _log.info("Loading configurator " + clazz.getCanonicalName() + "...");
            configurator.doConfig(AppConfigurations.get(),elements,docElement);

        }

    }


    @Override
    public String getDocsPath() {

        return docsPath;

    }

    @Override
    public AppConfigurations getAppConfigs() {

        return AppConfigurations.get();

    }



    private Document loadDocument(ServletContext servletContext) throws HiException{

        org.w3c.dom.Document document = null;

        URL xsdPath = null;

        try {

            xsdPath = servletContext.getResource("/hi-config.xsd");

        }catch (Exception ex){

            throw new HiException("Failed to load configurations XSD file",ex);

        }

        if(xsdPath==null)
            throw new BadConfigException("Hi XML namespace XSD file could not be loaded. Make sure the Hi Resources library is in your classpath.");


        URL hiXML = null;
        BadConfigException badConfigException = new BadConfigException("Hi configuration file could not be found in path /WEB-INF/hi.xml. Application can't start successfully without this file");

        try {

            hiXML = servletContext.getResource("/WEB-INF/hi.xml");

        }catch (MalformedURLException ex){}

        if(hiXML==null)
            throw  badConfigException;

        InputStream xml = null;

        try {

            xml = hiXML.openStream();

        }catch (IOException ex){

            throw new BadConfigException("Failed to read Hi configuration file in path /WEB-INF/hi.xml");

        }

        return readDocument(xml,xsdPath);

    }

    private Document readDocument(InputStream xml,URL xsdPath) throws HiException{

        Document document = null;

        try {


            byte[] buffer = new byte[xml.available()];
            xml.read(buffer);

            ByteArrayInputStream byteArray1 = new ByteArrayInputStream(buffer);
            ByteArrayInputStream byteArray2 = new ByteArrayInputStream(buffer);


            XMLSchemaFactory factory =
                    new XMLSchemaFactory();
            Source schemaFile = new StreamSource(xsdPath.openStream());
            Source xmlSource = new StreamSource(byteArray1);
            Schema schema = factory.newSchema(schemaFile);
            Validator validator = schema.newValidator();
            validator.validate(xmlSource);

            document = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(byteArray2);

        }catch (IOException |SAXException |ParserConfigurationException ex){

            throw new BadConfigException("Invalid Hi configuration file. The file is not formatted as expected. Check the documentation",ex);

        }

        return document;

    }

}
