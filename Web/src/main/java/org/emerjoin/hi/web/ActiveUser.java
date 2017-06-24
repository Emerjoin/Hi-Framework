package org.emerjoin.hi.web;

import org.emerjoin.hi.web.internal.Logging;
import org.slf4j.Logger;

import javax.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashMap;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
@SessionScoped
public class ActiveUser implements Serializable {

    private String csrfToken = "";
    private HashMap<String,Object> data = new HashMap<>();
    private Logger _log = Logging.getInstance().getLogger(ActiveUser.class);

    public ActiveUser(){

        try {

            SecureRandom secureRandom = new SecureRandom();
            byte[] token = new byte[128];
            secureRandom.nextBytes(token);

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] mdbytes = md.digest(token);

            //convert the byte to hex format method 2
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < mdbytes.length; i++) {
                String hex = Integer.toHexString(0xff & mdbytes[i]);
                if (hex.length() == 1) hexString.append('0');
                    hexString.append(hex);
            }

            csrfToken = hexString.toString().toUpperCase();

        }catch (Exception ex){

            _log.error("Failed to generate User's CSRF protection token",ex);

        }
    }

    public String getCsrfToken() {

        return csrfToken;

    }

    public HashMap<String, Object> getData() {
        return data;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }

    public Object getProperty(String name){

        return data.get(name);

    }

    public Object getProperty(String name,Object defaultValue){

        Object value = data.get(name);
        if(value==null)
            return defaultValue;

        return value;

    }

    public void setProperty(String name, Object value){

        data.put(name,value);

    }
}
