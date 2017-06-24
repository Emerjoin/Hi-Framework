package org.emerjoin.hi.web.config;

import org.emerjoin.hi.web.AppContext;
import org.emerjoin.hi.web.RequestContext;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import java.util.*;

/**
 * Created by Mario Junior.
 */
public class Tunings {

    private Map<String,CachedWebrootDirectory> fixedCachingPaths = Collections.synchronizedMap(new HashMap<>());
    private List<String> smartCachingPaths = Collections.synchronizedList(new ArrayList<>());
    private List<String> noCachingHintPaths = Collections.synchronizedList(new ArrayList<>());
    private Map<String,String> smartlyCachedResources = Collections.synchronizedMap(new HashMap<>());
    private Map<String,CachingDecision> decisionsMap =  Collections.synchronizedMap(new HashMap<>());

    private static Logger _log = LoggerFactory.getLogger(Tunings.class);

    public class CachedWebrootDirectory {

        public String name;
        public long time;

        public CachedWebrootDirectory(String name, long time){

            this.name = name;
            this.time = time;

        }


    }

    public enum CachingMode {

        SmartCaching, FixedCaching,NoCaching

    }

    public class CachingDecision{

        private CachingMode mode;
        private long time;
        private String resourcePath;

        public CachingMode getMode(){

            return mode;

        }

        public long getTime(){

            return time;

        }

        public String getResourcePath(){

            return resourcePath;

        }

    }


    public Tunings(){

        //this.smartCachingPaths.add("hi-es5.js");

    }

    public void enableFixedCaching(String folderPath, long time){

        fixedCachingPaths.put(folderPath, new CachedWebrootDirectory(folderPath,time));

    }

    public void addNoCachingHint(String path){

        this.noCachingHintPaths.add(path);

    }

    public void enableSmartCaching(String folderPath){

        smartCachingPaths.add(folderPath);

    }

    public List<String> getSmartCachingPaths(){

        return smartCachingPaths;

    }

    public String applySmartCaching(String markup, boolean partialDocument){

        if(AppConfigurations.get().getDeploymentMode()== AppConfigurations.DeploymentMode.DEVELOPMENT)
            return markup;
        AppContext appContext = CDI.current().select(AppContext.class).get();

        Document document = null;
        if(partialDocument)
            document = Jsoup.parseBodyFragment(markup);
        else
            document = Jsoup.parse(markup.replace("hi-es5.js",getCachedPath("hi-es5.js",appContext)));

        String imgFindFormat = "img[src^=webroot/%s]";
        String imgAloadFindFormat = "img[aload^=webroot/%s]";
        String imgErrFindFormat = "img[err^=webroot/%s]";
        String scriptFindFormat = "script[src^=webroot/%s]";
        String styleFindFormat = "style[href^=webroot/%s]";
        String linkFindFormat = "a[href^=webroot/%s]";
        String styleLinkFindFormat = "link[href^=webroot/%s]";

        String q = "";

        for(String cachedPath : smartCachingPaths){
            //Find image elements
            q = String.format(imgFindFormat,cachedPath);
            Elements elements  =  document.select(q);
            elements.forEach((el) -> cacheAttribute("src",el,appContext));
            //Find image aload elements
            q = String.format(imgAloadFindFormat,cachedPath);
            elements  =  document.select(q);
            elements.forEach((el) -> cacheAttribute("aload",el,appContext));
            //Find image err elements
            q = String.format(imgAloadFindFormat,cachedPath);
            elements  =  document.select(q);
            elements.forEach((el) -> cacheAttribute("err",el,appContext));
            //Find script elements
            q = String.format(scriptFindFormat,cachedPath);
            elements = document.select(q);
            elements.forEach((el) -> cacheAttribute("src",el,appContext));
            //Find style elements
            q = String.format(styleFindFormat,cachedPath);
            elements = document.select(q);
            elements.forEach((el) -> cacheAttribute("href",el,appContext));
            //Find style link elements
            q = String.format(styleLinkFindFormat,cachedPath);
            elements = document.select(q);
            elements.forEach((el) -> cacheAttribute("href",el,appContext));
            //Find link elements
            q = String.format(linkFindFormat,cachedPath);
            elements = document.select(q);
            elements.forEach((el) -> cacheAttribute("href",el,appContext));
        }

        if(partialDocument)
            return document.body().html();
        return document.outerHtml();
    }

    private void cacheAttribute(String attr,Element element, AppContext appContext){

        String resourcePath = element.attr(attr);
        if(resourcePath==null||resourcePath.isEmpty())
            return;

        String cachedResourcePath = getCachedPath(resourcePath,appContext);
        if(cachedResourcePath==null)
            return;

        element.attr(attr,cachedResourcePath);
        smartlyCachedResources.put(cachedResourcePath,resourcePath);

    }

    private String getCachedPath(String currentPath, AppContext appContext){

        int lastDotIndex = currentPath.lastIndexOf('.');
        if(lastDotIndex==-1)
            return null;

        return currentPath.substring(0,lastDotIndex)+appContext.getAssetVersionToken()
                +currentPath.substring(lastDotIndex,currentPath.length());

    }


    public void emmitSmartCachingHeaders(RequestContext requestContext){

        requestContext.getResponse().setHeader("Pragma", "");
        requestContext.getResponse().setHeader("Cache-Control", "public, max-age=31536000");

    }

    public void emmitFixedCachingHeaders(CachingDecision decision, RequestContext requestContext){

        requestContext.getResponse().setHeader("Pragma", "");
        requestContext.getResponse().setHeader("Cache-Control", "public, max-age=" + decision.time);

    }

    public CachingDecision decision(String asset){

        CachingDecision decision = decisionsMap.get(asset);
        if(decision!=null)
            return decision;

        decision = new CachingDecision();
        decision.mode = CachingMode.NoCaching;

        boolean smartlyCached = isASmartCachedURL(asset);

        //Smartly cached resource
        if(smartlyCached){
            decision.mode = CachingMode.SmartCaching;
            decision.resourcePath = smartlyCachedResources.get(asset);
            decisionsMap.put(asset,decision);
            return decision;
        }

        //Check if its a fixed caching resource
        long fixedCachingTime = getFixedCachingTime(asset);
        if(fixedCachingTime<1) {
            decisionsMap.put(asset,decision);
            return decision;
        }

        decision.mode = CachingMode.FixedCaching;
        decision.time = fixedCachingTime;
        decision.resourcePath = asset;
        decisionsMap.put(asset,decision);
        return decision;
    }

    private long getFixedCachingTime(String assetURL){
        long time = 0;
        String primaryPattern = assetURL.replace("webroot/","").substring(0,assetURL.indexOf("/"))+"*";
        String secondaryPattern = assetURL.substring(0,assetURL.lastIndexOf("/")+1).replace("webroot/","");

        //No caching primary match
        if(noCachingHintPaths.contains(primaryPattern))
            return -1;
        //No caching secondary match
        if(noCachingHintPaths.contains(secondaryPattern))
            return -1;
        String fixedCachingPath = assetURL.replace("webroot/","");
        fixedCachingPath = fixedCachingPath.substring(0,fixedCachingPath.lastIndexOf("/")+1);

        time = getTime4Path(fixedCachingPath);
        if(time>0)
            return time;

        while (fixedCachingPath.indexOf('/')!=-1){

            String wildcard = fixedCachingPath+"*";
            time = getTime4Path(wildcard);
            if(time>0)
                return time;

            int lastForwardSlashIndex = fixedCachingPath.lastIndexOf('/');
            if(lastForwardSlashIndex==-1)
                return 0;

            fixedCachingPath = fixedCachingPath.substring(0,lastForwardSlashIndex);
            fixedCachingPath = fixedCachingPath.substring(0,fixedCachingPath.lastIndexOf("/")+1);
        }

        return time;
    }

    private long getTime4Path(String path){

        if(fixedCachingPaths.containsKey(path)) {
            long time = fixedCachingPaths.get(path).time;;
            return time;
        }

        return 0;


    }


    private boolean isASmartCachedURL(String assetUrl){

        boolean cached = smartlyCachedResources.containsKey(assetUrl);
        return cached;

    }

    public String getCleanAssetURL(String smartCacheURL){

        AppContext appContext = CDI.current().select(AppContext.class).get();
        String cachingVersion = appContext.getAssetVersionToken();
        return smartCacheURL.replace(cachingVersion,"");

    }

}
