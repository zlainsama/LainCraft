package lain.mods.permissionmanager;

import java.io.File;
import lain.mods.laincraft.core.SharedConstants;
import lain.mods.laincraft.event.ServerCheckCommandAccessEvent;
import lain.mods.permissionmanager.permission.PermissionHolder;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.IExtendedEntityProperties;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInitializationEvent;

@Mod(modid = "PermissionManager", name = "PermissionManager", version = "", dependencies = "required-after:LainCraftCore", useMetadata = true)
public class PermissionManager
{

    public static final String identifier = "LainCraft:PermissionManager:PlayerPermissionHolder";

    public static String getBlockName(IBlockAccess world, int x, int y, int z)
    {
        String n = null;
        try
        {
            int id = world.getBlockId(x, y, z);
            n = id > 0 ? Block.blocksList[id].getUnlocalizedName() : null;
        }
        catch (Throwable ignored)
        {
        }
        if (n == null)
            n = "generic";
        return n;
    }

    public static String getEntityItemName(EntityItem item)
    {
        String n = null;
        try
        {
            n = item.getEntityItem().getItem().getUnlocalizedName();
        }
        catch (Throwable ignored)
        {
        }
        if (n == null)
            n = "generic";
        return n;
    }

    public static String getEntityName(Entity entity)
    {
        String n = null;
        try
        {
            n = EntityList.getEntityString(entity);
        }
        catch (Throwable ignored)
        {
        }
        if (n == null)
            n = "generic";
        return n;
    }

    public static String getItemName(ItemStack item)
    {
        String n = null;
        try
        {
            n = item.getItem().getUnlocalizedName();
        }
        catch (Throwable ignored)
        {
        }
        if (n == null)
            n = "generic";
        return n;
    }

    public static <T extends PermissionHolder> T getPermissionHolder(Entity entity)
    {
        try
        {
            IExtendedEntityProperties prop = entity.getExtendedProperties(identifier);
            return prop != null ? (T) prop : null;
        }
        catch (Throwable ignored)
        {
            return null;
        }
    }

    public static boolean isOperator(String name)
    {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server != null)
        {
            // I am lazy!
            if (server.isSinglePlayer() && SharedConstants.isLain(name))
                return true;
            return server.getConfigurationManager().getOps().contains(name.trim().toLowerCase());
        }
        return false;
    }

    File rootDir;

    @Mod.Init
    public void load(FMLInitializationEvent event)
    {
        rootDir = new File(SharedConstants.getLainCraftDirFile(), "Permissions");
        if (rootDir.exists() || rootDir.mkdirs() || rootDir.isDirectory())
            MinecraftForge.EVENT_BUS.register(this);
    }

    @ForgeSubscribe
    public void onCommandCheck(ServerCheckCommandAccessEvent event)
    {
        if (event.sender instanceof EntityPlayerMP)
        {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            if (server != null && server.isSinglePlayer() && event.defaultAllowed) // this should fix vanilla feature allowCheat
                return;
            PermissionHolder holder = getPermissionHolder((EntityPlayerMP) event.sender);
            if (holder != null)
                event.setResult(holder.hasPermission("command." + event.command.getCommandName().toLowerCase()) ? Result.ALLOW : Result.DENY);
            else
                event.setResult(Result.DENY);
        }
    }

    @ForgeSubscribe
    public void onEntityConstructing(EntityEvent.EntityConstructing event)
    {
        if (event.entity instanceof EntityPlayerMP)
        {
            try
            {
                File dir = new File(rootDir, "users");
                if (dir.exists() || dir.mkdirs() || dir.isDirectory())
                    event.entity.registerExtendedProperties(identifier, new PlayerPermissionHolder((EntityPlayerMP) event.entity, dir));
            }
            catch (Throwable t)
            {
                System.err.println("error attaching PlayerPermissionHolder: " + t.toString());
            }
        }
    }

    @ForgeSubscribe
    public void onPlayerAttackEntity(AttackEntityEvent event)
    {
        if (event.entityPlayer instanceof EntityPlayerMP)
        {
            PermissionHolder holder = getPermissionHolder((EntityPlayerMP) event.entityPlayer);
            if (holder == null || !holder.hasPermission("entity.attack." + getEntityName(event.target)))
                event.setCanceled(true);
            if (holder == null || !holder.hasPermission("item.use." + getItemName(event.entityPlayer.getCurrentEquippedItem())))
                event.setCanceled(true);
        }
    }

    @ForgeSubscribe
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        if (event.entityPlayer instanceof EntityPlayerMP)
        {
            PermissionHolder holder = getPermissionHolder((EntityPlayerMP) event.entityPlayer);
            String n = getBlockName(event.entityPlayer.worldObj, event.x, event.y, event.z);
            switch (event.action)
            {
                case LEFT_CLICK_BLOCK:
                    if (holder == null || !holder.hasPermission("block.destroy." + n))
                        event.setCanceled(true);
                    break;
                case RIGHT_CLICK_BLOCK:
                    if (holder == null || !holder.hasPermission("block.interact." + n))
                        event.useBlock = Result.DENY;
                    break;
                default:
                    break;
            }
            if (holder == null || !holder.hasPermission("item.use." + getItemName(event.entityPlayer.getCurrentEquippedItem())))
                event.useItem = Result.DENY;
        }
    }

    @ForgeSubscribe
    public void onPlayerInteractEntity(EntityInteractEvent event)
    {
        if (event.entityPlayer instanceof EntityPlayerMP)
        {
            PermissionHolder holder = getPermissionHolder((EntityPlayerMP) event.entityPlayer);
            if (holder == null || !holder.hasPermission("entity.interact." + getEntityName(event.target)))
                event.setCanceled(true);
            if (holder == null || !holder.hasPermission("item.use." + getItemName(event.entityPlayer.getCurrentEquippedItem())))
                event.setCanceled(true);
        }
    }

    @ForgeSubscribe
    public void onPlayerPickupItem(EntityItemPickupEvent event)
    {
        if (event.entityPlayer instanceof EntityPlayerMP)
        {
            PermissionHolder holder = getPermissionHolder((EntityPlayerMP) event.entityPlayer);
            if (holder == null || !holder.hasPermission("item.pickup." + getEntityItemName(event.item)))
                event.setCanceled(true);
        }
    }

}
