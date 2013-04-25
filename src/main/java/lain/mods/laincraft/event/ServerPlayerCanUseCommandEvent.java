package lain.mods.laincraft.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;

public class ServerPlayerCanUseCommandEvent extends Event
{

    public static ServerPlayerCanUseCommandEvent post(EntityPlayer player, boolean allow, int requiredLevel, String commandName)
    {
        ServerPlayerCanUseCommandEvent event = new ServerPlayerCanUseCommandEvent(player, allow, requiredLevel, commandName);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public final EntityPlayer player;
    public boolean allow;
    public final int requiredLevel;

    public final String commandName;

    public ServerPlayerCanUseCommandEvent(EntityPlayer player, boolean allow, int requiredLevel, String commandName)
    {
        super();
        this.player = player;
        this.allow = allow;
        this.requiredLevel = requiredLevel;
        this.commandName = commandName;
    }

}
