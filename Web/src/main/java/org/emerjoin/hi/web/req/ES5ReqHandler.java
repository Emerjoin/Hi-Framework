package org.emerjoin.hi.web.req;

import com.google.gson.Gson;
import org.emerjoin.hi.web.*;
import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.exceptions.HiException;
import org.emerjoin.hi.web.i18n.I18nConfiguration;
import org.emerjoin.hi.web.i18n.I18nRuntime;
import org.emerjoin.hi.web.i18n.LanguageBundle;
import org.emerjoin.hi.web.internal.ES5Library;
import org.emerjoin.hi.web.mvc.exceptions.NoSuchTemplateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Mario Junior.
 */
@HandleRequests(regexp = "(hi-[A-Za-z_0-9.-]+[.][A-Za-z]{1,10}|[A-Z-a-z_]+\\/hi-[A-Za-z_0-9.-]+[.][A-Za-z]{1,10})\\w+")
@ApplicationScoped
public class ES5ReqHandler extends ReqHandler {

    private static Map<String,String> templateControllers = new HashMap<String, String>();


    @PostConstruct
    public void init(){

        this.gson = AppContext.createGson();

    }

    public static void prepareTemplates(ServletContext context) throws HiException{

        AppConfigurations appConfigurations = AppConfigurations.get();

        for(String template : appConfigurations.getTemplates()) {

            try {

                URL templateHTML = context.getResource("/" + template + ".html");
                URL templateController = context.getResource("/" + template + ".js");

                if(!(templateHTML!=null&&templateController!=null)){
                    throw new NoSuchTemplateException(template);
                }

                String templHTML = Helper.readLines(templateHTML.openStream(),null);
                MVCReqHandler.storeTemplate(template,templHTML);

                String templtController = Helper.readLines(templateController.openStream(), null);

                if(!appConfigurations.underDevelopment())
                    templateControllers.put(template, templtController);

            }catch (MalformedURLException e){

                throw new HiException("Invalid template path <"+template+">",e);

            }catch (IOException ex){

                throw new NoSuchTemplateException(template);

            }

        }

    }


    private RequestContext requestContext = null;

    @Inject
    private ActiveUser activeUser;

    @Inject
    private AppContext appContext;

    private Gson gson = null;

    @Inject
    private ES5Library es5;

    @Inject
    private FrontEnd frontEnd;

    private static Logger LOG = LoggerFactory.getLogger(ES5ReqHandler.class);

    private void es5File(String templateContent){

        if(!AppConfigurations.get().underDevelopment()) {
            AppConfigurations.get().getTunings().emmitSmartCachingHeaders(requestContext);
        } else requestContext.getResponse().setHeader("Cache-Control", "no-cache");

        requestContext.getResponse().setHeader("Content-Type", "text/javascript");

        String toReplace = "//{{config}}";
        String hiJs = getHiJs(requestContext);
        String hiJsReplaced = hiJs.replace(toReplace, es5.getJsConfig());

        if(I18nRuntime.isReady()){
            I18nRuntime runtime =  I18nRuntime.get();
            I18nConfiguration configuration = runtime.getConfiguration();
            LanguageBundle lang = runtime.getBundle();

            if(configuration.isExportLanguageBundle()){

                LOG.debug("Exporting full language dictionary");
                //Export current language bundle
                Map<String,String> dict =  lang.export();
                LOG.debug(String.format("%d words found in dictionary",dict.size()));

                String dictJSON = gson.toJson(dict);
                String dictVar = String.format("var $i18nBundle = %s;",dictJSON);
                hiJsReplaced += dictVar;

            }else if(configuration.isMappingsEnabled()){

                String template = frontEnd.getTemplate();
                if(lang.hasDictionary(template)){

                    LOG.debug(String.format("Exporting language dictionary of template [%s]",template));
                    //Export the dictionary mapped to current template
                    Map<String,String> dictionary = lang.getDictionary(template);
                    LOG.debug(String.format("%d words found in dictionary",dictionary.size()));

                    String dictJSON = gson.toJson(dictionary);
                    String dictVar = String.format("var $i18nTemplateBundle = %s;",dictJSON);
                    hiJsReplaced += dictVar;

                }else{

                    LOG.warn(String.format("No language dictionary mapped to template [%s]",template));

                }

            }else{

                LOG.debug("I18n Mappings are disabled and full language exportation is also disabled");

            }
        }

        Helper.echo(hiJsReplaced + templateContent + es5.getComponentsJS(), requestContext);

    }

    private void es5TestFiles() throws ServletException, IOException, HiException{


        requestContext.getResponse().setHeader("Content-Type","text/javascript");

        String toReplace = "//{{config}}";
        String hiForTests = es5.getStaticHiJS() + es5.getTestsJS();

        URL runResource = requestContext.getServletContext().getResource("/webroot/tests/run.js");

        if(runResource!=null){
            String runScript = Helper.readLines(runResource.openStream(),null);
            hiForTests=hiForTests.replace(toReplace,runScript);
        }

        Helper.echo(hiForTests+prepareTestFiles(),requestContext);

    }

    private String prepareTestFiles() throws IOException, ServletException{

        String testFilesJS = "";
        Set<String> testFiles = AppConfigurations.get().getTestedViews().keySet();
        for(String testFile: testFiles){

            URL resource = requestContext.getServletContext().getResource(testFile);
            String url = AppConfigurations.get().getTestedViews().get(testFile);

            int slashIndex = url.indexOf('/');
            String controller = url.substring(0,slashIndex);
            String action = url.substring(slashIndex+1,url.length());

            String append = "";
            String prepend = "";

            if(resource!=null){

                prepend = "\nHi.$nav.setNextControllerInfo(\""+controller+"\",\""+action+"\");";
                append = "\nHi.$ui.js.setLoadedController(\""+controller+"\",\""+action+"\");";

                String viewControllerContent = Helper.readLines(resource.openStream(),null);
                testFilesJS=testFilesJS+prepend+"\n"+viewControllerContent+append;

            }

        }

        return testFilesJS;


    }


    private String getHiJs(RequestContext requestContext){

        String content = null;

        if(es5.getStaticHiJS() !=null){

            content = es5.getAngularJS()+ es5.getStaticHiJS() +es5.getFrontierJS();

        }else

            content = "";

        return content;

    }

    private String fetchTemplateController(String name) throws HiException{

        try {

            URL templateURL = requestContext.getServletContext().getResource("/" + name + ".js");

            if (templateURL == null) {
                throw new NoSuchTemplateException(name);
            }

            String html = Helper.readLines(templateURL.openStream(), null);
            return html;

        }catch (MalformedURLException ex){

            throw new HiException("Invalid template name <"+name+">",ex);

        }catch (IOException ex){

            throw new HiException(String.format("Failed to fetch with name %s",name),ex);

        }

    }

    private String getTemplateController(String name) throws HiException{

        if(AppConfigurations.get().underDevelopment())
            return fetchTemplateController(name);

        if(!templateControllers.containsKey(name))
            throw new NoSuchTemplateException(name);


        return templateControllers.get(name);

    }


    @Override
    public boolean handle(RequestContext requestContext) throws ServletException, IOException {

        try {

            String language = frontEnd.getLanguage();
            if(I18nRuntime.isReady())
                I18nRuntime.get().setLanguage(language);


            this.requestContext = requestContext;

            String requestURL = requestContext.getRouteUrl();
            int indexOfLastSlash = requestURL.lastIndexOf('/');

            String templateName = (String) activeUser.getProperty(FrontEnd.TEMPLATE_SESSION_VARIABLE,
                    AppConfigurations.get().getDefaultTemplate());
            String templateContent = getTemplateController(templateName);

            if (indexOfLastSlash != -1)
                requestURL = requestURL.substring(indexOfLastSlash + 1, requestURL.length());

            String es5File = "hi-es5.js";
            if (AppConfigurations.get().getDeploymentMode() != AppConfigurations.DeploymentMode.DEVELOPMENT)
                es5File = "hi-es5-" + templateName + appContext.getAssetVersionToken() + ".js";


            if (requestURL.equals(es5File)) {
                es5File(templateContent);
            } else if (requestURL.equals("hi-angular.js")) {
                requestContext.getResponse().setHeader("Content-Type", "text/javascript");
                Helper.echo(es5.getAngularJS(), requestContext);
            } else if (requestURL.equals("hi-es5-tests.js")) {
                es5TestFiles();
            } else {
                return false;
            }

        }finally {

            if(I18nRuntime.isReady())
                I18nRuntime.get().unsetLanguage();

        }

        return true;

    }




}
