package lain.mods.laincraft.permission;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.command.ICommandSender;

public class PermissionManager
{

    private final Map<String, PermissionUser> users = new HashMap<String, PermissionUser>();

    public PermissionUser getUser(ICommandSender par1)
    {
        PermissionUser user = new PermissionUser(par1, this);
        return user;
    }

    public boolean checkUpdate(PermissionUser par1, ICommandSender par2, ICommandSender par3)
    {
        return PermissionUser.Type.getType(par2) == PermissionUser.Type.getType(par3);
    }

    public void onUpdate(PermissionUser par1)
    {
    }

    public boolean attachPermission(PermissionUser par1, Permission par2)
    {
        return false;
    }

    public boolean detachPermission(PermissionUser par1, Permission par2)
    {
        return false;
    }

}
