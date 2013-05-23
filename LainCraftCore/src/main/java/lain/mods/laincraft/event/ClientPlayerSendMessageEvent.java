package lain.mods.laincraft.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.Event;

@Cancelable
public class ClientPlayerSendMessageEvent extends Event
{

    public static ClientPlayerSendMessageEvent post(EntityPlayer player, String message)
    {
        ClientPlayerSendMessageEvent event = new ClientPlayerSendMessageEvent(player, message);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public final EntityPlayer player;

    public String message;

    public ClientPlayerSendMessageEvent(EntityPlayer player, String message)
    {
        super();
        this.player = player;
        this.message = message;
    }

}
