package lain.mods.permissionmanager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import lain.mods.laincraft.utils.io.UnicodeInputStreamReader;
import lain.mods.permissionmanager.permission.Permission;
import lain.mods.permissionmanager.permission.PermissionHolder;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;

public class PlayerPermissionHolder extends PermissionHolder implements IExtendedEntityProperties
{

    EntityPlayerMP player;
    File dir;
    File file;
    String name;
    boolean loaded;

    public PlayerPermissionHolder(EntityPlayerMP par1, File par2)
    {
        if (par1 == null)
            throw new RuntimeException("player cant be null");
        player = par1;
        dir = par2;
    }

    @Override
    public boolean hasPermission(Permission permission)
    {
        load();
        return super.hasPermission(permission);
    }

    @Override
    public void init(Entity arg0, World arg1)
    {
    }

    public void load()
    {
        if (loaded)
            return;
        file = new File(dir, name());
        try
        {
            if (!file.exists())
                file.createNewFile();
        }
        catch (IOException e)
        {
            System.err.println("error creating empty permissions file for " + name() + " : " + e.toString());
        }
        if (PermissionManager.isOperator(name()))
            addPermission("*");
        else
        {
            BufferedReader buf = null;
            try
            {
                buf = new BufferedReader(new UnicodeInputStreamReader(new FileInputStream(file), "UTF-8"));
                String line = null;
                while ((line = buf.readLine()) != null)
                {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#"))
                        continue;
                    addPermission(line);
                }
            }
            catch (IOException e)
            {
                System.err.println("error loading permissions for " + name() + " : " + e.toString());
            }
        }
        loaded = true;
    }

    @Override
    public void loadNBTData(NBTTagCompound arg0)
    {
    }

    public String name()
    {
        if (name == null && player.username != null)
        {
            name = StringUtils.stripControlCodes(player.username);
        }
        return name;
    }

    @Override
    public void saveNBTData(NBTTagCompound arg0)
    {
    }

}
