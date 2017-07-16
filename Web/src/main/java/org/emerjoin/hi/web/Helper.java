package org.emerjoin.hi.web;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.util.Scanner;

/**
 * Created by Mario Junior.
 */
public class Helper {

    private static Logger log = LoggerFactory.getLogger(Helper.class);

    public static String readLines(InputStream inputStream, RequestContext requestContext){

        if(inputStream==null)
            return null;

        String text = "";

        try {

            Scanner scanner = new Scanner(inputStream,"utf8");
            while (scanner.hasNextLine())
                text +=scanner.nextLine()+"\n";

            if(requestContext!=null){
                HttpServletResponse response = requestContext.getResponse();
                response.setContentType("charset=UTF8");
                requestContext.echo(text);
            }

        }catch (Throwable ex){

            log.error("Failed to read lines",ex);

        }finally {
            try{
                inputStream.close();
            }catch (Throwable ex){
                log.error("Failed to close InputStream",ex);
            }
        }

        return text;


    }

    public static void echo(String text,RequestContext requestContext){

        try {

            PrintWriter printWriter = requestContext.getResponse().getWriter();
            printWriter.write(text);
            printWriter.flush();

        }catch (Throwable ex){
            log.error("Failed to write text content",ex);
        }

    }

    public static void echoln(String text,RequestContext requestContext){

        try {

            PrintWriter printWriter = requestContext.getResponse().getWriter();
            printWriter.write(text+"\n");
            printWriter.flush();

        }catch (Throwable ex){
            log.error("Failed to write text content",ex);
        }

    }

    public static String md5(String text){

        try {

            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(text.getBytes());

            byte byteData[] = md.digest();

            //convert the byte to hex format method 1
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteData.length; i++)
                sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));

            //convert the byte to hex format method 2
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < byteData.length; i++) {
                String hex = Integer.toHexString(0xff & byteData[i]);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();


        }catch (Throwable ex){

            log.error("Md5 hash generation failed",ex);
            return text;

        }

    }






}
