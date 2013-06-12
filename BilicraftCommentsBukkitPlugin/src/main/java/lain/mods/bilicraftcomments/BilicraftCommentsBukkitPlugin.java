package lain.mods.bilicraftcomments;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permissible;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class BilicraftCommentsBukkitPlugin extends JavaPlugin implements IPermissionManager
{

    public boolean hasPermission(String username, String permission)
    {
        PluginManager manager = Bukkit.getPluginManager();
        Permission perm = manager.getPermission(permission);
        if (perm == null)
        {
            perm = new Permission(permission);
            manager.addPermission(perm);
        }
        for (Permissible user : perm.getPermissibles())
            if (user.hasPermission(perm) && user instanceof Player && username.equalsIgnoreCase(((Player) user).getName()))
                return true;
        return false;
    }

    @Override
    public void onDisable()
    {
        if (BilicraftComments.manager == this)
            BilicraftComments.manager = null;
    }

    @Override
    public void onEnable()
    {
        BilicraftComments.manager = this;
    }

}
