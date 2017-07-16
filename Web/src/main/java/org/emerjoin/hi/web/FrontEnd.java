package org.emerjoin.hi.web;

import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.i18n.I18nRuntime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

//TODO: JavaDoc
@RequestScoped
public class FrontEnd {


    public static String TEMPLATE_SESSION_VARIABLE ="$template_";
    public static String LANGUAGE_SESSION_VARIABLE="$language_";

    private Map<String,Map> laterInvocations = new HashMap<>();

    @Inject
    private HttpServletRequest httpServletRequest;

    @Inject
    private ActiveUser activeUser;

    private String template;
    private String language;
    private Map<String,Object> templateData = new HashMap<>();


    private Logger log = LoggerFactory.getLogger(FrontEnd.class);


    @PostConstruct
    private void onReady(){

        this.language = AppConfigurations.get().getDefaultLanguage();

        Object templateObject = activeUser.getProperty(TEMPLATE_SESSION_VARIABLE);
        if(templateObject==null){
            template=AppConfigurations.get().getDefaultTemplate();
            activeUser.setProperty(TEMPLATE_SESSION_VARIABLE,template);

        }else template = templateObject.toString();


        Object langObject = activeUser.getProperty(LANGUAGE_SESSION_VARIABLE);
        if(langObject==null){
            language = AppConfigurations.get().getDefaultLanguage();
            activeUser.setProperty(LANGUAGE_SESSION_VARIABLE,language);

        }else language = langObject.toString();


    }



    public void refresh(){

        invokeAfter("reload", Collections.emptyMap());

    }

    public void refresh(String url){

        Map<String,Object> map = new HashMap<>();
        map.put("url",url);
        invokeAfter("reload", map);

    }

    public void invokeAfter(String actionName, Map params) {

        laterInvocations.put("$"+actionName,params);

    }

    public void ajaxRedirect(String url){

        Map map = new HashMap<>();
        map.put("url",url);
        invokeAfter("redirect",map);

    }


    public void setTemplateData(Map<String,Object> templateData) {
        this.templateData = templateData;
    }

    public void setLanguage(String name){
        this.language = name;
        activeUser.setProperty(LANGUAGE_SESSION_VARIABLE,this.language);
        //Set reloadLanguage command if this method is invoked on an ajax request
        if(isRequestAjax()||isFrontierRequest())
            invokeAfter("reloadLanguage", Collections.emptyMap());
        else {

            if(I18nRuntime.isReady()){
                I18nRuntime.get().setLanguage(name);
            }

        }
    }

    public void setTemplate(String template) {

        String currentTemplate = activeUser.getProperty(TEMPLATE_SESSION_VARIABLE).toString();

        if(template.equals(currentTemplate)) {
            log.warn(String.format("Trying to set the same template name : %s",template));
            return;
        }

        log.debug(String.format("Changing template %s => %s",currentTemplate,template));
        this.template = template;
        activeUser.setProperty(TEMPLATE_SESSION_VARIABLE,this.template);

        //Reload command if this method is invoked on an ajax request
        if(isRequestAjax()||isFrontierRequest())
            refresh(httpServletRequest.getRequestURI());


    }

    public boolean wasTemplateDataSet(){

        return templateData.size()>0;

    }

    public String getLanguage() {
        return language;
    }

    public String getTemplate() {

        return template;

    }


    public Map<String,Map> getLaterInvocations() {
        return laterInvocations;
    }
    public Map<String,Object> getTemplateData() {
        return templateData;
    }

    public boolean gotLaterInvocations() {

        return laterInvocations.size()>0;

    }

    public boolean isRequestAjax(){

        String headerValue = httpServletRequest.getHeader(RequestContext.AJAX_HEADER_KEY);
        return headerValue!=null&&headerValue.equalsIgnoreCase("1");

    }

    public boolean isFrontierRequest(){

        return httpServletRequest.getRequestURL().indexOf("jbind:")!=-1;

    }

    public String getLangDictionary(){
        String dictPath = "/i18n/"+language+".json";
        String dict = "{}";

        try {

            URL resource = httpServletRequest.getServletContext().getResource(dictPath);
            if(resource==null)
                return dict;
            dict = Helper.readLines(resource.openStream(),null);
            return dict;

        }catch (Throwable ex){

            return dict;

        }

    }




}
