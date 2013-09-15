package lain.mods.accman;

import java.util.Set;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.StringUtils;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import com.google.common.collect.Sets;
import cpw.mods.fml.client.FMLClientHandler;

public class Limiter
{

    private boolean flag = true;
    private Set<String> list = Sets.newHashSet();

    public void addToList(EntityPlayer player)
    {
        if (player != null)
            addToList(player.username);
    }

    public void addToList(String playername)
    {
        list.add(StringUtils.stripControlCodes(playername).toLowerCase());
    }

    public void clearList()
    {
        list.clear();
    }

    public boolean isWhitelistMode()
    {
        return flag;
    }

    @ForgeSubscribe
    public void onAttack(AttackEntityEvent event)
    {
        if (shouldLimitPlayer(event.entityPlayer))
            event.setCanceled(true);
    }

    @ForgeSubscribe
    public void onBlockInteract(PlayerInteractEvent event)
    {
        if (shouldLimitPlayer(event.entityPlayer))
            event.setCanceled(true);
    }

    @ForgeSubscribe
    public void onDamage(LivingAttackEvent event)
    {
        if (event.entityLiving instanceof EntityPlayer)
            if (shouldLimitPlayer((EntityPlayer) event.entityLiving))
                event.setCanceled(true);
    }

    @ForgeSubscribe
    public void onEntityInteract(EntityInteractEvent event)
    {
        if (shouldLimitPlayer(event.entityPlayer))
            event.setCanceled(true);
    }

    @ForgeSubscribe
    public void onGuiOpen(GuiOpenEvent event)
    {
        if (event.gui != null && shouldLimitPlayer(FMLClientHandler.instance().getClient().thePlayer))
            event.gui = new GuiLogin();
    }

    @ForgeSubscribe
    public void onItemPickup(EntityItemPickupEvent event)
    {
        if (shouldLimitPlayer(event.entityPlayer))
            event.setCanceled(true);
    }

    @ForgeSubscribe
    public void onItemToss(ItemTossEvent event)
    {
        if (shouldLimitPlayer(event.player))
            event.setCanceled(true);
    }

    @ForgeSubscribe
    public void onSetTarget(LivingSetAttackTargetEvent event)
    {
        if (event.entityLiving instanceof EntityLiving && event.target instanceof EntityPlayer)
            if (shouldLimitPlayer((EntityPlayer) event.target))
                ((EntityLiving) event.entityLiving).setAttackTarget(null);
    }

    public void removeFromList(EntityPlayer player)
    {
        if (player != null)
            removeFromList(player.username);
    }

    public void removeFromList(String playername)
    {
        list.remove(StringUtils.stripControlCodes(playername).toLowerCase());
    }

    public void setWhitelistMode(boolean flag)
    {
        this.flag = flag;
    }

    public boolean shouldLimitPlayer(EntityPlayer player)
    {
        return player == null ? false : shouldLimitPlayer(player.username);
    }

    public boolean shouldLimitPlayer(String playername)
    {
        if (isWhitelistMode())
            return !list.contains(StringUtils.stripControlCodes(playername).toLowerCase());
        return list.contains(StringUtils.stripControlCodes(playername).toLowerCase());
    }

}
