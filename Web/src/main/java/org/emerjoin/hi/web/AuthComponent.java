package org.emerjoin.hi.web;

/**
 * @author Mário Júnior
 */
public interface AuthComponent {

    public boolean isUserInAnyOfThisRoles(String[] roles);
    public boolean doesUserHavePermission(String permissionName);

}
