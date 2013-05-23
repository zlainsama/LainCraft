package lain.mods.permissionmanager.permission;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import net.minecraft.util.ChunkCoordinates;

public class GroupPermission extends PermissionBase implements PermissionGroup
{

    private final Map<String, Set<Permission>> attachedPermissions = new HashMap<String, Set<Permission>>();

    protected GroupPermission(String par1)
    {
        super(par1);
    }

    @Override
    public boolean canCommandSenderUseCommand(int arg0, String arg1)
    {
        return false;
    }

    @Override
    public String getCommandSenderName()
    {
        return getName();
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates()
    {
        return new ChunkCoordinates(0, 0, 0);
    }

    @Override
    public boolean matcherPass(MatcherType type)
    {
        if (type == MatcherType.ALL)
            return true;
        return false;
    }

    @Override
    public void onAttach(PermissionUser user)
    {
        PermissionUser group = user.getManager().getUser(this);
        String k = user.toString();
        Set<Permission> s = attachedPermissions.get(k);
        if (s == null)
        {
            s = new HashSet<Permission>();
            attachedPermissions.put(k, s);
        }
        for (Permission permission : group.getPermissions())
            if (user.attachPermission(permission))
                s.add(permission);
    }

    @Override
    public void onDetach(PermissionUser user)
    {
        String k = user.toString();
        Set<Permission> s = attachedPermissions.get(k);
        if (s != null)
        {
            for (Permission permission : s)
                user.detachPermission(permission);
            s.clear();
            attachedPermissions.remove(k);
        }
    }

    @Override
    public void sendChatToPlayer(String arg0)
    {
    }

    @Override
    public String translateString(String arg0, Object... arg1)
    {
        return "";
    }

}
