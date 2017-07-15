package org.emerjoin.hi.web.boot;

import org.emerjoin.hi.web.BootstrapUtils;
import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.config.ConfigProvider;
import org.emerjoin.hi.web.i18n.I18nXmlConfig;
import org.emerjoin.hi.web.exceptions.HiException;
import org.emerjoin.hi.web.i18n.I18nStarter;
import org.emerjoin.hi.web.i18n.I18nConfiguration;
import org.emerjoin.hi.web.internal.ES5Library;
import org.emerjoin.hi.web.internal.Logging;
import org.emerjoin.hi.web.internal.Router;
import org.emerjoin.hi.web.meta.Tested;
import org.emerjoin.hi.web.mvc.Controller;
import org.emerjoin.hi.web.mvc.ControllersMapper;
import org.emerjoin.hi.web.req.MVCReqHandler;
import org.jboss.jandex.*;
import org.slf4j.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.net.URL;
import java.util.*;

/**
 * This class is responsible for performing initialization of a Hi-Application upon deployment.
 * @author Mário Júnior
 */
@ApplicationScoped
public class BootAgent {

    private ServletContext servletContext = null;
    private ServletConfig servletConfig = null;
    private Logger _log = Logging.getInstance().getLogger();
    private String deployId ="";

    @Inject
    private ES5Library scriptLibrary;

    @Inject
    private Router router;

    @Inject
    private ConfigProvider configProvider;


    public BootAgent(){}

    private void initBootExtensions() throws HiException {

        Set<Index> indexSet = BootstrapUtils.getIndexes(servletContext);
        if(indexSet==null)
            return;

        Iterable<BootExtension> bootExtensions = BootManager.getExtensions();
        _log.info("Initializing boot extensions...");

        for(BootExtension extension : bootExtensions) {

            try {

                _log.info(String.format("Initializing boot extension : %s",extension.getClass().getCanonicalName()));
                extension.boot(indexSet,servletContext);

            }catch (Exception ex){

                throw new HiException("Failed to initialize boot extension : "+extension.getClass().getCanonicalName(),ex);

            }

        }

        _log.info("Finalized initialization of boot extensions.");

    }



    private void findControllersAndMap() throws HiException{

        Set<Index> indexSet = BootstrapUtils.getIndexes(servletContext);
        if(indexSet==null)
            return;

        for(Index index : indexSet){

            Collection<ClassInfo> classInfos =
                    index.getAllKnownSubclasses(DotName.createSimple(Controller.class.getCanonicalName()));


            for(ClassInfo classInfo : classInfos){

                DotName dotName = classInfo.asClass().name();


                try {

                    _log.info("Mapping controller class : "+dotName.toString());
                    Class controllerClazz = Class.forName(dotName.toString());

                    ControllersMapper.map(controllerClazz);

                }catch (ClassNotFoundException ex){

                    _log.error("Error mapping controller class",ex);
                    continue;

                }

            }

        }

    }


    private void findTestedActions(){

        Set<Index> indexSet = BootstrapUtils.getIndexes(servletContext);

        for(Index index : indexSet)
            findTestedAControllerActions(index);


    }

    private void findTestedAControllerActions(Index index){

        List<AnnotationInstance> instances = index.getAnnotations(DotName.createSimple(Tested.class.getCanonicalName()));
        for(AnnotationInstance an : instances){

            MethodInfo methodInfo =  an.target().asMethod();
            String actionURL = MVCReqHandler.getActionMethodFromURLPart(methodInfo.name());


            String canonicalName = methodInfo.declaringClass().name().toString();
            String simpleName = canonicalName.substring(canonicalName.lastIndexOf('.')+1,canonicalName.length());
            String controllerURL = MVCReqHandler.getURLController(simpleName);

            _log.info("Tested controller action detected : "+controllerURL+"/"+actionURL);

            String testedViewPath = "/views/"+controllerURL+"/"+actionURL+".js";
            AppConfigurations.get().getTestedViews().put(testedViewPath,controllerURL+"/"+actionURL);
            String viewTestPath1 = "/webroot/tests/views/"+controllerURL+"/"+actionURL+"Test.js";

            AppConfigurations.get().getTestFiles().put(viewTestPath1,true);

        }

    }

    private void loadConfigs() throws HiException{

        String disable_servlet_context_scanning = servletContext.getInitParameter("DISABLE_SERVLET_CONTEXT_SCANNING");
        if(disable_servlet_context_scanning!=null
                &&(disable_servlet_context_scanning.equals("1")||disable_servlet_context_scanning.toUpperCase().equals("TRUE")))
            BootstrapUtils.DISABLE_SERVLET_CONTEXT_SCANNING = true;


        Set<Index> indexSet = BootstrapUtils.getIndexes(servletContext);
        configProvider.load(servletContext,servletConfig,indexSet);

    }


    private void makeDeployId(){

        deployId = String.valueOf(Calendar.getInstance().getTimeInMillis());

    }



    public void init(ServletContext context, ServletConfig config) throws HiException{

        this.servletContext = context;
        this.servletConfig = config;

        makeDeployId(); //Set deploy Id
        loadConfigs(); //Load App configurations
        initI18n(); //Start I18n
        findControllersAndMap(); //Find all the controller and map them
        findTestedActions(); //Find all the tested controllers actions
        scriptLibrary.init(servletContext);//Load scripts and generate frontiers
        router.init(servletContext,servletConfig); //Register requests handlers
        initBootExtensions(); //Load and execute boot extensions
    }


    private void initI18n(){

        Optional<I18nConfiguration> configuration = I18nXmlConfig.getConfiguration();
        if(!configuration.isPresent()) {
            _log.info("I18n not enabled. Skipping");
            return;
        }

        _log.info("I18n enabled. Initializing...");
        Set<URL> libraries = BootstrapUtils.getLibraries(servletContext);
        I18nStarter i18NStarter = new I18nStarter(configuration.get(),libraries,servletContext);
        i18NStarter.start();


    }

    public String getDeployId(){

        return deployId;

    }


}
