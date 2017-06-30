package org.emerjoin.hi.web.internal;

import org.emerjoin.hi.web.BootstrapUtils;
import org.emerjoin.hi.web.Helper;
import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.exceptions.HiException;
import org.emerjoin.hi.web.frontier.FrontiersGenerator;
import org.emerjoin.hi.web.frontier.model.FrontierBeansCrawler;
import org.emerjoin.hi.web.frontier.model.FrontierClass;
import org.emerjoin.hi.web.meta.Frontier;
import org.emerjoin.hi.web.meta.WebComponent;
import org.emerjoin.hi.web.mvc.exceptions.MissingResourcesLibException;
import org.emerjoin.hi.web.req.FrontiersReqHandler;
import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Index;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for serving ES5 scripts
 * to the internal components of the framework.
 * @author Mário Júnior
 */
@ApplicationScoped
public class ES5Library {



    private String testsJS = "";
    private String staticHiJS = null;
    private String hiLoaderJS = null;
    private String genericFrontierJS = null;
    private String jsConfig ="";
    private String frontierJS ="";
    private String angularJS ="";
    private String componentsJS ="";


    private ServletContext servletContext = null;
    private static Logger _log = Logging.getInstance().getLogger(ES5Library.class);


    public void init(ServletContext context)throws HiException {

        this.servletContext = context;
        loadMainLibraryJS();
        loadRunJS();
        loadAppLoaderJS();
        loadGenericFrontierJS();
        generateFrontiersJS();
        findAndLoadComponents();

    }


    private void loadMainLibraryJS() throws MissingResourcesLibException {

        try {

            _log.debug("Reading main client-side code file...");
            URL res = servletContext.getResource("/hi.min.js");

            if(AppConfigurations.get().underDevelopment())
                res = servletContext.getResource("/hi.js");

            if(res!=null){

                InputStream inputStream = res.openStream();
                String scriptContent = Helper.readLines(inputStream,null);
                staticHiJS = scriptContent;
            }

            res = servletContext.getResource("/hi-tests.js");
            if(res!=null){

                InputStream inputStream = res.openStream();
                String scriptContent = Helper.readLines(inputStream,null);
                testsJS = scriptContent;

            }

            res = servletContext.getResource("/angular.min.js");
            if(res!=null){

                InputStream inputStream = res.openStream();
                String scriptContent = Helper.readLines(inputStream,null);
                angularJS = scriptContent;

            }

        }catch (Exception ex){

            throw new MissingResourcesLibException(ex);

        }


    }

    private void loadAppLoaderJS() throws MissingResourcesLibException {

        try {

            _log.debug("Reading client-side AJAX loader code file...");
            URL res = servletContext.getResource("/loader.js");
            if(res!=null){

                InputStream inputStream = res.openStream();
                String scriptContent = Helper.readLines(inputStream,null);
                hiLoaderJS = scriptContent;
            }

        }catch (Exception ex){

            throw new MissingResourcesLibException(ex);

        }

    }

    private void loadRunJS() throws MissingResourcesLibException {

        try {

            URL res = servletContext.getResource("/run.js");
            if(res!=null){
                _log.debug("Reading javascript configurations code file...");
                InputStream inputStream = res.openStream();
                String scriptContent = Helper.readLines(inputStream,null);
                jsConfig = scriptContent;
            }

        }catch (Exception ex){

            throw new MissingResourcesLibException(ex);

        }

    }

    private void loadGenericFrontierJS() throws MissingResourcesLibException {

        try {

            _log.debug("Reading generic client-side (frontiers) code file...");
            URL res = servletContext.getResource("/frontier.min.js");
            if(AppConfigurations.get().underDevelopment())
                res = servletContext.getResource("/frontier.js");

            if(res!=null){

                InputStream inputStream = res.openStream();
                String scriptContent = Helper.readLines(inputStream,null);
                genericFrontierJS = scriptContent;


            }

        }catch (Exception ex){

            throw new MissingResourcesLibException(ex);

        }


    }

    private void findAndLoadComponents() throws HiException {

        Set<Index> indexSet = BootstrapUtils.getIndexes(servletContext);
        if(indexSet==null)
            return;

        for(Index index : indexSet){

            List<AnnotationInstance> instances = index.
                    getAnnotations(DotName.createSimple(WebComponent.class.getCanonicalName()));

            for(AnnotationInstance an: instances){

                Class componentClass = null;
                try {

                    _log.info("Loading web component : "+an.target().asClass().name().toString());
                    componentClass = Class.forName(an.target().asClass().toString());

                }catch (ClassNotFoundException ex){

                    _log.warn("Component class is missing : "+an.target().asClass().toString());
                    continue;

                }

                String scriptName = componentClass.getSimpleName().toLowerCase();
                String minifiedScriptName = componentClass.getSimpleName().toLowerCase()+".min";
                loadComponentJS(componentClass,scriptName,minifiedScriptName);

            }
        }
    }

    private void loadComponentJS(Class componentClass, String scriptName,
                                 String minifiedScriptName) throws HiException{

        URL componentScript =  null;

        try {

            if (AppConfigurations.get().underDevelopment())
                componentScript = servletContext.getResource("/" + scriptName + ".js");
            else
                componentScript = servletContext.getResource("/" + minifiedScriptName + ".js");

        }catch (MalformedURLException ex){

            throw new HiException("Invalid component name : "+componentClass.getSimpleName(),ex);

        }

        if(componentScript==null) {

            _log.warn("Script for component "+componentClass.getSimpleName()+" could not be located using names : "
                    +scriptName+", "+minifiedScriptName);

            return;
        }

        try {

            componentsJS +=Helper.readLines(componentScript.openStream(), null);
            _log.info(componentClass.getSimpleName()+" Web component loaded");

        }catch (Exception ex){

            throw new HiException("Could not read component script : "+componentClass.getSimpleName(),ex);

        }

    }

    private void generateFrontiersJS() throws HiException {

        List beansList = new ArrayList();
        _log.info("Looking for frontiers...");

        Set<Index> indexSet = BootstrapUtils.getIndexes(servletContext);
        if(indexSet==null)
            return;

        for(Index index: indexSet){

            List<AnnotationInstance> instanceList = index.getAnnotations(DotName.createSimple(Frontier.class.getCanonicalName()));
            for(AnnotationInstance an: instanceList){

                Class fClazz = null;

                try {

                    fClazz = Class.forName(an.target().asClass().name().toString());
                    _log.info("Frontier class detected : " + fClazz.getCanonicalName());
                    beansList.add(fClazz);

                }catch (Exception ex){

                    _log.error("Error while attempting to register the frontier class",ex);
                    continue;

                }
            }
        }

        generateFrontiers(beansList);

    }

    private void generateFrontiers(List beansList) throws HiException{

        try {

            FrontierClass[] beanClasses = FrontierBeansCrawler.getInstance().crawl(beansList);


            _log.info("Generating client-side code for frontiers...");
            FrontiersGenerator frontiersGenerator = new FrontiersGenerator();

            for (FrontierClass beanClass : beanClasses) {

                FrontiersReqHandler.addFrontier(beanClass);
                String frontier_script = frontiersGenerator.generate(beanClass);
                frontierJS += "\n" + frontier_script;

            }

        }catch (ServletException ex){

            throw new HiException("Failed to crawl frontier beans",ex);

        }

    }


    public String getTestsJS() {
        return testsJS;
    }

    public String getStaticHiJS() {
        return staticHiJS;
    }

    public String getHiLoaderJS() {
        return hiLoaderJS;
    }

    public String getGenericFrontierJS() {
        return genericFrontierJS;
    }

    public String getJsConfig() {
        return jsConfig;
    }

    public String getFrontierJS() {
        return frontierJS;
    }

    public String getAngularJS() {
        return angularJS;
    }

    public String getComponentsJS() {
        return componentsJS;
    }
}
