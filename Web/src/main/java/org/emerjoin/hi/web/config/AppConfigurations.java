package org.emerjoin.hi.web.config;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mario Junior.
 */
public class AppConfigurations {

    private String viewsDirectory;
    private String welcomeUrl;
    private String templates[]={"index"};
    private List<String> frontiers = new ArrayList<>();
    private List<String> frontierPackages = new ArrayList<>();
    private Tunings tunings = new Tunings();
    private Map<String,Boolean> testFiles = new HashMap<>();
    private String defaultLanguage = "default";
    private String defaultTemplate = "index";
    private long frontiersTimeout = 0;

    private Map<String,String> testedViews = new HashMap<>();

    private List<String> smartCachingExtensions = new ArrayList<>();

    private DeploymentMode deploymentMode = DeploymentMode.DEVELOPMENT;

    private Logger _log = LoggerFactory.getLogger(AppConfigurations.class);



    public static enum DeploymentMode {

        DEVELOPMENT, PRODUCTION

    }

    private AppConfigurations(){

        smartCachingExtensions.add("css");
        smartCachingExtensions.add("js");

    }

    public Map<String,String> getTestedViews() {
        return testedViews;
    }


    public List<String> getSmartCachingExtensions(){

        return smartCachingExtensions;

    }

    private static AppConfigurations appConfigurations = null;

    public static void set(AppConfigurations config){

        appConfigurations = config;

    }

    public static AppConfigurations get(){

        if(appConfigurations==null)
            appConfigurations = new AppConfigurations();
        return appConfigurations;

    }



    public boolean underDevelopment(){

        return deploymentMode==DeploymentMode.DEVELOPMENT;

    }


    public String getViewsDirectory() {
        return viewsDirectory;
    }

    public void setViewsDirectory(String viewsDirectory) {
        this.viewsDirectory = viewsDirectory;
    }

    public String[] getTemplates() {
        return templates;
    }

    public void setTemplates(String[] templates) {

        this.templates = templates;
        this.defaultTemplate = templates[0];
        _log.info("Setting default template : "+this.defaultTemplate);
    }


    public String getWelcomeUrl() {

        return welcomeUrl;

    }

    public void setWelcomeUrl(String welcomeUrl) {
        this.welcomeUrl = welcomeUrl;
    }


    public List<String> getFrontiers() {

        return frontiers;

    }

    public void setFrontiers(List<String> frontiers) {

        this.frontiers = frontiers;

    }

    public Tunings getTunings() {
        return tunings;
    }

    public void setTunings(Tunings tunings) {
        this.tunings = tunings;
    }


    public Map<String, Boolean> getTestFiles() {
        return testFiles;
    }

    public void setTestFiles(Map<String, Boolean> testFiles) {
        this.testFiles = testFiles;
    }

    public String getDefaultLanguage() {
        return defaultLanguage;
    }

    public void setDefaultLanguage(String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }

    public void setDeploymentMode(DeploymentMode mode){

        this.deploymentMode = mode;

    }

    public List<String> getFrontierPackages() {
        return frontierPackages;
    }

    public void setFrontierPackages(List<String> frontierPackages) {
        this.frontierPackages = frontierPackages;
    }

    public DeploymentMode getDeploymentMode() {
        return deploymentMode;
    }

    public String getDefaultTemplate(){

        return defaultTemplate;

    }

    public long getFrontiersTimeout() {
        return frontiersTimeout;
    }

    public void setFrontiersTimeout(long frontiersTimeout) {
        this.frontiersTimeout = frontiersTimeout;
    }
}
