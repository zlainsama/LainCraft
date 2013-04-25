package lain.mods.laincraft;

import java.util.EnumSet;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

public class LainHelper implements ITickHandler
{

    protected static final LainHelper instance = new LainHelper();

    public static boolean isLain(String username)
    {
        return "zlainsama".equalsIgnoreCase(StringUtils.stripControlCodes(username));
    }

    private LainHelper()
    {
        MinecraftForge.EVENT_BUS.register(this);
        TickRegistry.registerTickHandler(this, Side.SERVER);
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
    public String getLabel()
    {
        return "LainHelper";
    }

    @ForgeSubscribe
    public void onHurt(LivingHurtEvent event)
    {
        if (event.entityLiving instanceof EntityPlayer)
        {
            EntityPlayer plr = (EntityPlayer) event.entityLiving;
            if (isLain(plr.username))
            {
                if (!event.source.isUnblockable())
                    event.ammount = AbsorbDamage(plr, event.ammount, 0.44D);
                event.ammount = AbsorbDamage(plr, event.ammount, 0.40D);
            }
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData)
    {
    }

    @Override
    public EnumSet<TickType> ticks()
    {
        return EnumSet.of(TickType.PLAYER);
    }

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData)
    {
        if (type.contains(TickType.PLAYER))
        {
            EntityPlayer plr = (EntityPlayer) tickData[0];
            if (isLain(plr.username))
            {
                if (plr.shouldHeal() && plr.ticksExisted % 12 == 0)
                {
                    plr.heal(1);
                }
            }
        }
    }

}
