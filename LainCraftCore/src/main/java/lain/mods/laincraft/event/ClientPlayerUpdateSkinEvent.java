package lain.mods.laincraft.event;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Cancelable;
import net.minecraftforge.event.Event;

@Cancelable
public class ClientPlayerUpdateSkinEvent extends Event
{

    public static ClientPlayerUpdateSkinEvent post(EntityPlayer player, String texture, String skinUrl, String cloakUrl)
    {
        ClientPlayerUpdateSkinEvent event = new ClientPlayerUpdateSkinEvent(player, texture, skinUrl, cloakUrl);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }

    public final EntityPlayer player;
    public String texture;
    public String skinUrl;

    public String cloakUrl;

    public ClientPlayerUpdateSkinEvent(EntityPlayer player, String texture, String skinUrl, String cloakUrl)
    {
        super();
        this.player = player;
        this.texture = texture;
        this.skinUrl = skinUrl;
        this.cloakUrl = cloakUrl;
    }

}
