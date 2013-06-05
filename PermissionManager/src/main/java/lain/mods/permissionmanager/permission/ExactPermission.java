package lain.mods.permissionmanager.permission;

import java.util.Collections;
import java.util.Set;

public class ExactPermission extends Permission
{

    public ExactPermission(String par1)
    {
        super(par1);
    }

    @Override
    public Set<Permission> wildcards()
    {
        return Collections.emptySet();
    }

}
