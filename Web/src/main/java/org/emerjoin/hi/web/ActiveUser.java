package org.emerjoin.hi.web;

import org.emerjoin.hi.web.security.CsrfTokenUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.enterprise.context.SessionScoped;
import javax.xml.bind.DatatypeConverter;
import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

/**
 * @author Mário Júnior
 */
//TODO: JavaDoc
@SessionScoped
public class ActiveUser implements Serializable {

    private String csrfToken = "";
    private HashMap<String,Object> data = new HashMap<>();
    private static final Logger _log = LoggerFactory.getLogger(ActiveUser.class);
    private static final CsrfTokenUtil csrfTokeUtil = new CsrfTokenUtil();

    @PostConstruct
    public void init(){
        _log.debug("Generating User CSRF token...");
        this.csrfToken = csrfTokeUtil.makeJwtToken();
    }

    public String expireCsrfToken(){
        _log.debug("Expiring current CSRF token...");
        return this.csrfToken = csrfTokeUtil
                .makeJwtToken();
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
