package org.emerjoin.hi.web.config.xml.sections;

import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.config.BadConfigException;
import org.emerjoin.hi.web.config.Configurator;
import org.emerjoin.hi.web.config.xml.ConfigSection;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.util.Map;

/**
 * @author Mário Júnior
 */
@ConfigSection(tags = "web-tuning")
public class WebTuningConfig implements Configurator{



    private void readNoCachingHints(Element noCacheElement, AppConfigurations appConfigurations){

        NodeList noCacheHintsList = noCacheElement.getElementsByTagName("no-cache-hint");
        if(noCacheHintsList.getLength()==0)
            return;

        for(int i=0;i<noCacheHintsList.getLength();i++){
            Element noCacheHintElement = (Element) noCacheHintsList.item(i);
            appConfigurations.getTunings().addNoCachingHint(noCacheHintElement.getAttribute("folder-path"));
        }

    }

    private void readStaticFoldersCachingConfigs(Element staticFoldersCachingElement, AppConfigurations appConfigurations){
        NodeList foldersHttpCacheList = staticFoldersCachingElement.getElementsByTagName("cache");
        if(foldersHttpCacheList.getLength()==0)
            return;

        for(int i=0; i<foldersHttpCacheList.getLength();i++){
            Element folderHttpCache = (Element) foldersHttpCacheList.item(i);
            int age = Integer.parseInt(folderHttpCache.getAttribute("age"));
            String ageUnit = folderHttpCache.getAttribute("age-unit");
            String folderName = folderHttpCache.getAttribute("folder-path");

            long hour_milliseconds = 3600000;
            long day_milliseconds = hour_milliseconds*24;
            long week_milliseconds = day_milliseconds*7;
            long month_milliseconds = week_milliseconds*4;
            long year_milliseconds = month_milliseconds*12;
            long millis_times_factor = 0;

            switch (ageUnit){
                case "HOURS": millis_times_factor = hour_milliseconds;
                    break;
                case "DAYS": millis_times_factor = day_milliseconds;
                    break;
                case "WEEKS": millis_times_factor = week_milliseconds;
                    break;
                case  "MONTHS": millis_times_factor = month_milliseconds;
                    break;
                case  "YEARS" : millis_times_factor = year_milliseconds;
                    break;
                default: millis_times_factor = 0;
            }

            long cache_time = millis_times_factor*age;
            if(cache_time==0)
                continue;
            appConfigurations.getTunings().enableFixedCaching(folderName,cache_time);
        }
    }

    private void readSmartTunningConfigs(Element smartAssetsCachingElement, AppConfigurations appConfigurations){

        NodeList smartCachedNodeList =  smartAssetsCachingElement.getElementsByTagName("if-path-starts-with");
        if(smartCachedNodeList.getLength()==0)
            return;

        for(int i=0;i<smartCachedNodeList.getLength();i++){
            Element element = (Element) smartCachedNodeList.item(i);
            String assetURI = element.getTextContent();
            appConfigurations.getTunings().enableSmartCaching(assetURI);
        }

    }

    @Override
    public void doConfig(AppConfigurations configs, Map<String, Element> elements, Element document) throws BadConfigException {
            Element tunningElement = elements.get("web-tuning");
            NodeList webrootNodeList = tunningElement.getElementsByTagName("webroot");
            Element webrootElement = null;

            if(webrootNodeList.getLength()>0)
                webrootElement = (Element) webrootNodeList.item(0);
            else
                return;

            NodeList staticFoldersCachingNodesList = webrootElement.getElementsByTagName("folders-fixed-caching");
            if(staticFoldersCachingNodesList.getLength()>0){
                Element staticFoldersCachingElement = (Element) staticFoldersCachingNodesList.item(0);
                readStaticFoldersCachingConfigs(staticFoldersCachingElement,configs);
            }

            NodeList smartAssetsCachingNodesList = webrootElement.getElementsByTagName("folders-smart-caching");
            if(smartAssetsCachingNodesList.getLength()>0){
                Element smartAssetsCachingElement = (Element) smartAssetsCachingNodesList.item(0);
                readSmartTunningConfigs(smartAssetsCachingElement,configs);
            }

            NodeList noCachingControlNodesList = webrootElement.getElementsByTagName("no-caching-control");
            if(noCachingControlNodesList.getLength()>0){
                Element noCachingControlEment = (Element) noCachingControlNodesList.item(0);
                readNoCachingHints(noCachingControlEment,configs);
            }

    }




}
