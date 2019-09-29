package org.emerjoin.hi.web.security;

import io.jsonwebtoken.*;
import org.emerjoin.hi.web.config.AppConfigurations;
import org.emerjoin.hi.web.config.Frontiers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Date;
import java.util.UUID;

/**
 * @author Mario Junior.
 */
public class CsrfTokenUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(CsrfTokenUtil.class);

    private String makeSecureRandom(){
        AppConfigurations appConfigurations = AppConfigurations.get();
        Frontiers frontiers = appConfigurations.getFrontiersConfig();
        Frontiers.Security frontiersSecurity = frontiers.getSecurity();
        int secureRandomSize = frontiersSecurity.getCrossSiteRequestForgery().getToken().getSecureRandomSize();
        SecureRandom secureRandom = new SecureRandom();
        byte[] randomBuffer = new byte[secureRandomSize];
        secureRandom.nextBytes(randomBuffer);
        return DatatypeConverter.printHexBinary(randomBuffer);
    }

    public String makeJwtToken(){
        AppConfigurations appConfigurations = AppConfigurations.get();
        Frontiers frontiers = appConfigurations.getFrontiersConfig();
        Frontiers.Security frontiersSecurity = frontiers.getSecurity();
        Frontiers.Security.CrossSiteRequestForgery.Token tokenConfig = frontiersSecurity
                .getCrossSiteRequestForgery()
                .getToken();

        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.valueOf(tokenConfig
                .getJwtAlgorithm());

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        byte[] secretBytes = DatatypeConverter.parseBase64Binary(tokenConfig.getJwtPassphrase());
        Key signingKey = new SecretKeySpec(secretBytes, signatureAlgorithm.getJcaName());

        //Let's set the JWT Claims
        JwtBuilder builder = Jwts.builder().setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setSubject(makeSecureRandom())
                .setIssuer("")
                .signWith(signatureAlgorithm, signingKey);

        return builder.compact();
    }

    public boolean checkJwtToken(String jwt){
        AppConfigurations appConfigurations = AppConfigurations.get();
        Frontiers frontiers = appConfigurations.getFrontiersConfig();
        Frontiers.Security frontiersSecurity = frontiers.getSecurity();
        Frontiers.Security.CrossSiteRequestForgery.Token tokenConfig = frontiersSecurity
                .getCrossSiteRequestForgery()
                .getToken();
        byte[] secretBytes = DatatypeConverter.parseBase64Binary(tokenConfig
                .getJwtPassphrase());
        try {
            Jwts.parser().setSigningKey(secretBytes).parseClaimsJws(jwt)
                    .getBody()
                    .getSubject();
            return true;
        }catch (SignatureException ex){
            return false;
        }catch (JwtException ex){
            LOGGER.error("Error validating JSON Web Token: "+jwt, ex);
            return false;
        }
    }

}
