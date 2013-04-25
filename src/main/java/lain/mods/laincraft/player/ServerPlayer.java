package lain.mods.laincraft.player;

import lain.mods.laincraft.LainCraft;
import lain.mods.laincraft.event.ServerPlayerCanUseCommandEvent;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class ServerPlayer extends EntityPlayerMP
{

    public ServerPlayer(MinecraftServer par1MinecraftServer, World par2World, String par3Str, ItemInWorldManager par4ItemInWorldManager)
    {
        super(par1MinecraftServer, par2World, par3Str, par4ItemInWorldManager);
    }

    private int AbsorbDamage(EntityLiving ent, double damage, double ratio)
    {
        damage *= 25;
        damage += ent.carryoverDamage;
        damage -= (damage * ratio);
        ent.carryoverDamage = (int) damage % 25;
        return (int) (damage / 25D);
    }

    @Override
    public boolean canCommandSenderUseCommand(int par1, String par2Str)
    {
        boolean result = super.canCommandSenderUseCommand(par1, par2Str);
        return ServerPlayerCanUseCommandEvent.post(this, result, par1, par2Str).allow;
    }

    @Override
    protected void damageEntity(DamageSource par1, int par2)
    {
        if (LainCraft.isLain(username))
        {
            if (!par1.isUnblockable())
                par2 = AbsorbDamage(this, par2, 0.44D);
            par2 = AbsorbDamage(this, par2, 0.40D);
        }
        super.damageEntity(par1, par2);
    }

    @Override
    public void onLivingUpdate()
    {
        if (LainCraft.isLain(username))
        {
            if (shouldHeal() && ticksExisted % 12 == 0)
                heal(1);
        }
        super.onLivingUpdate();
    }

}
