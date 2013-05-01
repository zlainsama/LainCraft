package lain.mods.laincraft.permission;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lain.mods.laincraft.LainCraft;
import lain.mods.laincraft.util.UnicodeInputStreamReader;
import net.minecraft.command.ICommandSender;

public class PermissionManager
{

    private final File baseDir;

    private final Map<String, PermissionUser> users = new HashMap<String, PermissionUser>();

    public PermissionManager(File par1)
    {
        baseDir = par1;
        if (!par1.exists())
            par1.mkdirs();
    }

    public boolean checkAttachPermission(PermissionUser par1, Permission par2)
    {
        if (par2 == null)
            return false;
        if (par2.isMatcher() && par2.getMatcherType() != Permission.MatcherType.ALL)
            return false;
        return true;
    }

    public boolean checkDetachPermission(PermissionUser par1, Permission par2)
    {
        return true;
    }

    public boolean checkUpdate(PermissionUser par1, ICommandSender par2, ICommandSender par3)
    {
        try
        {
            return PermissionUser.Type.getType(par2) == PermissionUser.Type.getType(par3) && par2.getCommandSenderName().equals(par3.getCommandSenderName());
        }
        catch (NullPointerException ignored)
        {
            return false;
        }
    }

    public PermissionUser getUser(ICommandSender par1)
    {
        PermissionUser user = new PermissionUser(par1, this);
        String k = user.getUserType() + "." + user.getUserName();
        if (users.containsKey(k))
            users.get(k).update(par1);
        else
        {
            users.put(k, user);
            loadUser(user);
        }
        return users.get(k);
    }

    public void loadUser(PermissionUser par1)
    {
        switch (par1.getUserType())
        {
            case ServerPlayer:
            case FakePlayer:
            case CommandBlock:
            case Group:
                if (LainCraft.disablePermissionManager)
                    break;
                File f1 = new File(baseDir, par1.getUserType().toString());
                if (!f1.exists())
                    f1.mkdirs();
                File f2 = new File(f1, par1.getUserName());
                BufferedReader buf = null;
                try
                {
                    buf = new BufferedReader(new UnicodeInputStreamReader(new FileInputStream(f2), "UTF-8"));
                    String line = null;
                    while ((line = buf.readLine()) != null)
                    {
                        line = line.trim();
                        if (line.isEmpty() || line.startsWith("#"))
                            continue;
                        par1.attachPermission(PermissionFactory.build(line));
                    }
                }
                catch (FileNotFoundException ignored)
                {
                }
                catch (Exception e)
                {
                    throw new Error(e);
                }
                finally
                {
                    if (buf != null)
                        try
                        {
                            buf.close();
                        }
                        catch (Exception ignored)
                        {
                        }
                }
                break;
            default:
                break;
        }
        if (par1.isOperator())
            par1.attachPermission(PermissionFactory.ALLPERMISSION);
    }

    public void reloadUser(PermissionUser par1)
    {
        Set<Permission> copy = new HashSet<Permission>(par1.getPermissions());
        for (Permission permission : copy)
            if (permission instanceof PermissionGroup)
                reloadUser(getUser((PermissionGroup) permission));
        for (Permission permission : copy)
            par1.detachPermission(permission);
        loadUser(par1);
    }

}
