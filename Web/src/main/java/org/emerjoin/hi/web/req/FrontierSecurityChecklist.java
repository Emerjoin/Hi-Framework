package org.emerjoin.hi.web.req;

/**
 * @author Mario Junior.
 */
public class FrontierSecurityChecklist {

    private boolean originCheck;
    private boolean csrfTokenPresent;
    private boolean csrfTokenValid;
    private boolean csrfTokenExpired;

    public boolean isOriginCheck() {
        return originCheck;
    }

    public void setOriginCheck(boolean originCheck) {
        this.originCheck = originCheck;
    }

    public boolean isCsrfTokenPresent() {
        return csrfTokenPresent;
    }

    public void setCsrfTokenPresent(boolean csrfTokenPresent) {
        this.csrfTokenPresent = csrfTokenPresent;
    }

    public boolean isCsrfTokenValid() {
        return csrfTokenValid;
    }

    public void setCsrfTokenValid(boolean csrfTokenValid) {
        this.csrfTokenValid = csrfTokenValid;
    }

    public boolean isCsrfTokenExpired() {
        return csrfTokenExpired;
    }

    public void setCsrfTokenExpired(boolean csrfTokenExpired) {
        this.csrfTokenExpired = csrfTokenExpired;
    }

}
