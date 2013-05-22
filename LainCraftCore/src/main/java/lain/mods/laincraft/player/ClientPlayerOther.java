package lain.mods.laincraft.player;

import lain.mods.laincraft.event.ClientPlayerUpdateSkinEvent;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.util.StringUtils;
import net.minecraft.world.World;

public class ClientPlayerOther extends EntityOtherPlayerMP
{

    protected String cloak;

    public ClientPlayerOther(World par1World, String par2Str)
    {
        super(par1World, par2Str);
        UpdateSkins();
    }

    @Override
    public void updateCloak()
    {
        cloakUrl = cloak;
    }

    public void UpdateSkins()
    {
        skinUrl = "http://skins.minecraft.net/MinecraftSkins/" + StringUtils.stripControlCodes(username) + ".png";
        cloak = "http://skins.minecraft.net/MinecraftCloaks/" + StringUtils.stripControlCodes(username) + ".png";
        ClientPlayerUpdateSkinEvent event = ClientPlayerUpdateSkinEvent.post(this, texture, skinUrl, cloak);
        if (!event.isCanceled())
        {
            if (event.texture != null)
                texture = event.texture;
            skinUrl = event.skinUrl;
            cloak = event.cloakUrl;
        }
    }

}
