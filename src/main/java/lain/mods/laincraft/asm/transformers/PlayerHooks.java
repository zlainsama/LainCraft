package lain.mods.laincraft.asm.transformers;

public class PlayerHooks extends InstanceHook
{

    public PlayerHooks()
    {
        addMapping("net.minecraft.client.entity.EntityClientPlayerMP", "lain.mods.laincraft.player.ClientPlayer");
        addMapping("net.minecraft.client.entity.EntityOtherPlayerMP", "lain.mods.laincraft.player.ClientPlayerOther");
        addMapping("net.minecraft.entity.player.EntityPlayerMP", "lain.mods.laincraft.player.ServerPlayer");
    }

}
