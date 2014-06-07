package com.aegisql.testing_tools;

import com.aegisql.access.AccessPattern;

public class AccessPatterns {

public static AccessPattern ANY_MIKE_ANY       = new AccessPattern("%","mike","%","%","ACCESSOR_ID","1001");
public static AccessPattern ANY_NIKITA_ANY     = new AccessPattern("%","nikita","%","%","ACCESSOR_ID","1000");
public static AccessPattern USER_ANY_ANY       = new AccessPattern("USER","%","%","%","ACCESSOR_ID",null);
public static AccessPattern USER_NIKITA_ANY       = new AccessPattern("USER","nikita","%","%","ACCESSOR_ID","1000");
public static AccessPattern POWER_USER_ANY_ANY = new AccessPattern("POWER_USER","%","%","%","ACCESSOR_ID",null);
public static AccessPattern ADMIN_ANY_ANY      = new AccessPattern("ADMIN","%","%","%","%",null);
public static AccessPattern GUEST_ANY_ANY      = new AccessPattern("GUEST","%","%","%","%",null);

}
