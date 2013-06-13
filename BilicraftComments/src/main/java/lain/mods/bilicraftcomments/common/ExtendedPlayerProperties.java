package lain.mods.bilicraftcomments.common;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;

public class ExtendedPlayerProperties implements IExtendedEntityProperties
{

    public static class EventHandler
    {
        @ForgeSubscribe
        public void onEntityConstructing(EntityEvent.EntityConstructing event)
        {
            if (event.entity instanceof EntityPlayerMP)
                event.entity.registerExtendedProperties(identifier, new ExtendedPlayerProperties());
        }
    }

    public static final String identifier = "BcC:ExtendedPlayerProperties";

    public static ExtendedPlayerProperties getProperties(Entity entity)
    {
        IExtendedEntityProperties prop = entity.getExtendedProperties(identifier);
        if (prop instanceof ExtendedPlayerProperties)
            return (ExtendedPlayerProperties) prop;
        return null;
    }

    public static void load()
    {
        MinecraftForge.EVENT_BUS.register(new EventHandler());
    }

    public TimeMarker timer = new TimeMarker();

    @Override
    public void init(Entity arg0, World arg1)
    {
    }

    @Override
    public void loadNBTData(NBTTagCompound arg0)
    {
        try
        {
            if (arg0.hasKey(identifier))
                timer.markTime(arg0.getCompoundTag(identifier).getLong("lastMark"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public void saveNBTData(NBTTagCompound arg0)
    {
        try
        {
            if (!arg0.hasKey(identifier))
                arg0.setTag(identifier, new NBTTagCompound(identifier));
            arg0.getCompoundTag(identifier).setLong("lastMark", timer.getLastMark());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

}
