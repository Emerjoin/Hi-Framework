package org.emerjoin.hi.web.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Mário Júnior
 */
public class DictionaryBuilder {

    private Map<String,String> dictionary = new HashMap<>();

    protected DictionaryBuilder(){



    }

    public void put(InputStream dictionaryFile, I18nConfiguration configuration){

        Properties p = new Properties();

        try {

            if (configuration.isEncodingUTF8())
                p.load(new InputStreamReader(dictionaryFile, Charset.forName("UTF-8")));
            else
                p.load(dictionaryFile);

        }catch (IOException ex){

            throw new I18nException("Failed to load a Language dictionary file",ex);

        }

        p.forEach((k,v) -> dictionary.put(k.toString(),v.toString()));

    }



    public Map<String,String> build(){

        return dictionary;

    }


}
