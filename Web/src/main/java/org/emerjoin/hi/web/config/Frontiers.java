package org.emerjoin.hi.web.config;

/**
 * @author Mario Junior.
 */
public class Frontiers {

    public class Security {

        public class CrossSiteRequestForgery {

            public class Token {

                private String jwtAlgorithm = "H256";
                private String jwtPassphrase = "10045084-f522-4faa-afa4-bdbc955faa47";
                private int secureRandomSize = 64;

                public String getJwtAlgorithm() {
                    return jwtAlgorithm;
                }

                public void setJwtAlgorithm(String jwtAlgorithm) {
                    this.jwtAlgorithm = jwtAlgorithm;
                }

                public String getJwtPassphrase() {
                    return jwtPassphrase;
                }

                public void setJwtPassphrase(String jwtPassphrase) {
                    this.jwtPassphrase = jwtPassphrase;
                }

                public int getSecureRandomSize() {
                    return secureRandomSize;
                }

                public void setSecureRandomSize(int secureRandomSize) {
                    this.secureRandomSize = secureRandomSize;
                }
            }
            public class Cookie {

                private boolean httpOnly = true;
                private boolean secure = false;
                public static final String NAME = "_CSRF_";

                public boolean isHttpOnly() {
                    return httpOnly;
                }

                public void setHttpOnly(boolean httpOnly) {
                    this.httpOnly = httpOnly;
                }

                public boolean isSecure() {
                    return secure;
                }

                public void setSecure(boolean secure) {
                    this.secure = secure;
                }
            }

            private Token token = new Token();
            private Cookie cookie = new Cookie();

            public Token getToken() {
                return token;
            }

            public void setToken(Token token) {
                this.token = token;
            }

            public Cookie getCookie() {
                return cookie;
            }

            public void setCookie(Cookie cookie) {
                this.cookie = cookie;
            }
        }

        private CrossSiteRequestForgery crossSiteRequestForgery = new CrossSiteRequestForgery();

        public CrossSiteRequestForgery getCrossSiteRequestForgery() {
            return crossSiteRequestForgery;
        }

        public void setCrossSiteRequestForgery(CrossSiteRequestForgery crossSiteRequestForgery) {
            this.crossSiteRequestForgery = crossSiteRequestForgery;
        }

    }

    private Security security = new Security();
    private long defaultTimeout = 1500;

    public Security getSecurity() {
        return security;
    }

    public void setSecurity(Security security) {
        this.security = security;
    }

    public long getDefaultTimeout() {
        return defaultTimeout;
    }

    public void setDefaultTimeout(long defaultTimeout) {
        this.defaultTimeout = defaultTimeout;
    }
}
