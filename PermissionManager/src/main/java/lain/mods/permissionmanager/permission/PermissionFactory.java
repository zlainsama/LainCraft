package lain.mods.permissionmanager.permission;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

public class PermissionFactory
{

    public static final Permission ALLPERMISSION = new PermissionBase(Permission.MatcherType.ALL.toString());
    public static final Permission ANYPERMISSION = new PermissionBase(Permission.MatcherType.ANY.toString());

    private static final Map<String, Permission> permissions = new HashMap<String, Permission>();
    private static final Map<String, Constructor> constructors = new HashMap<String, Constructor>();
    private static final Constructor defaultConstructor;

    static
    {
        try
        {
            defaultConstructor = PermissionBase.class.getDeclaredConstructor(String.class);
            defaultConstructor.setAccessible(true);
        }
        catch (Exception e)
        {
            throw new Error(e);
        }
        registerPermissionBase("command", CommandPermission.class);
        registerPermissionBase("group", GroupPermission.class);
        permissions.put("*", ALLPERMISSION);
        permissions.put("?", ANYPERMISSION);
    }

    public static <T extends Permission> T build(String par1)
    {
        try
        {
            if (permissions.containsKey(par1))
                return (T) permissions.get(par1);
            T permission = build0(par1);
            permissions.put(par1, permission);
            return permission;
        }
        catch (Exception ignored)
        {
            return null;
        }
    }

    private static <T extends Permission> T build0(String par1) throws Exception
    {
        int lastLength = -1;
        Constructor result = null;
        for (String k : new TreeSet<String>(constructors.keySet()))
        {
            if (par1.startsWith(k + ".") && k.length() > lastLength)
            {
                result = constructors.get(k);
                lastLength = k.length();
            }
        }
        return (T) (result == null ? defaultConstructor.newInstance(par1) : result.newInstance(par1.substring(lastLength + 1)));
    }

    public static void registerPermissionBase(String par1, Class par2)
    {
        try
        {
            Constructor constructor = par2.getDeclaredConstructor(String.class);
            constructor.setAccessible(true);
            constructors.put(par1, constructor);
        }
        catch (Exception ignored)
        {
        }
    }

}
