package lain.mods.laincraft.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.Event;

@Cancelable
public class ClientPlayerUpdateSkinsEvent extends Event
{

    public static ClientPlayerUpdateSkinsEvent post(EntityPlayer player, String texture, String skinUrl, String cloakUrl)
    {
        ClientPlayerUpdateSkinsEvent event = new ClientPlayerUpdateSkinsEvent(player, texture, skinUrl, cloakUrl);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
    public final EntityPlayer player;
    public String texture;
    public String skinUrl;

    public String cloakUrl;

    public ClientPlayerUpdateSkinsEvent(EntityPlayer player, String texture, String skinUrl, String cloakUrl)
    {
        super();
        this.player = player;
        this.texture = texture;
        this.skinUrl = skinUrl;
        this.cloakUrl = cloakUrl;
    }

}
