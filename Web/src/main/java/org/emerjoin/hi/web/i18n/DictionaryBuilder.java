package org.emerjoin.hi.web.i18n;

import org.emerjoin.ioutils.Mappings;

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

        Map<String,String> map = new HashMap<>();

        try {

            map = Mappings.load(dictionaryFile);
            dictionary.putAll(map);

        }catch (Exception ex){

            throw new I18nException("Failed to load a Language dictionary file",ex);

        }

    }



    public Map<String,String> build(){

        return dictionary;

    }


}
