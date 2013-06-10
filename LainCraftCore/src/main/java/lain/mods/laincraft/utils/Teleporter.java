package lain.mods.laincraft.utils;

import java.util.Collection;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.packet.Packet41EntityEffect;
import net.minecraft.network.packet.Packet9Respawn;
import net.minecraft.potion.PotionEffect;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.registry.GameRegistry;

public class Teleporter
{

    public static void teleportEntity(Entity ent, int dimension, double x, double y, double z, float yaw, float pitch)
    {
        if (ent instanceof EntityPlayerMP)
        {
            if (dimension != -999 && dimension != ent.dimension)
                transferPlayerToDimension((EntityPlayerMP) ent, dimension, x, y, z, yaw, pitch);
            else
            {
                if (ent.riddenByEntity != null)
                    ent.riddenByEntity.mountEntity(null);
                if (ent.ridingEntity != null)
                    ent.mountEntity(null);
                ((EntityPlayerMP) ent).playerNetServerHandler.setPlayerLocation(x, y, z, yaw, pitch);
            }
        }
        else if (!ent.worldObj.isRemote)
        {
            World targetWorld = dimension == -999 ? null : FMLCommonHandler.instance().getMinecraftServerInstance().worldServerForDimension(dimension);
            if (targetWorld != null && targetWorld != ent.worldObj)
                transferEntityToWorld(ent, ent.worldObj, targetWorld, x, y, z, yaw, pitch);
            else
            {
                if (ent.riddenByEntity != null)
                    ent.riddenByEntity.mountEntity(null);
                if (ent.ridingEntity != null)
                    ent.mountEntity(null);
                ent.setLocationAndAngles(x, y, z, yaw, pitch);
            }
        }
    }

    public static void transferEntityToWorld(Entity ent, World oldWorld, World newWorld)
    {
        transferEntityToWorld(ent, oldWorld, newWorld, ent.posX, ent.posY, ent.posZ, ent.rotationYaw, ent.rotationPitch);
    }

    public static void transferEntityToWorld(Entity ent, World oldWorld, World newWorld, double x, double y, double z, float yaw, float pitch)
    {
        oldWorld.theProfiler.startSection("moving");
        oldWorld.removeEntity(ent);
        oldWorld.updateEntityWithOptionalForce(ent, false);
        ent.isDead = false;
        oldWorld.theProfiler.endSection();
        oldWorld.theProfiler.startSection("placing");
        ent.setLocationAndAngles(x, y, z, yaw, pitch);
        newWorld.spawnEntityInWorld(ent);
        newWorld.updateEntityWithOptionalForce(ent, false);
        ent.setWorld(newWorld);
        oldWorld.theProfiler.endSection();
    }

    public static void transferPlayerToDimension(EntityPlayerMP player, int dimension)
    {
        transferPlayerToDimension(player, dimension, player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
    }

    public static void transferPlayerToDimension(EntityPlayerMP player, int dimension, double x, double y, double z, float yaw, float pitch)
    {
        int dim = player.dimension;
        WorldServer oldWorld = player.mcServer.worldServerForDimension(player.dimension);
        player.dimension = dimension;
        WorldServer newWorld = player.mcServer.worldServerForDimension(player.dimension);
        player.playerNetServerHandler.sendPacketToPlayer(new Packet9Respawn(player.dimension, (byte) newWorld.difficultySetting, newWorld.getWorldInfo().getTerrainType(), newWorld.getHeight(), player.theItemInWorldManager.getGameType()));
        oldWorld.removePlayerEntityDangerously(player);
        player.isDead = false;
        transferEntityToWorld(player, oldWorld, newWorld, x, y, z, yaw, pitch);
        player.mcServer.getConfigurationManager().func_72375_a(player, oldWorld);
        player.playerNetServerHandler.setPlayerLocation(player.posX, player.posY, player.posZ, player.rotationYaw, player.rotationPitch);
        player.theItemInWorldManager.setWorld(newWorld);
        player.mcServer.getConfigurationManager().updateTimeAndWeatherForPlayer(player, newWorld);
        player.mcServer.getConfigurationManager().syncPlayerInventory(player);
        for (PotionEffect effect : (Collection<PotionEffect>) player.getActivePotionEffects())
            player.playerNetServerHandler.sendPacketToPlayer(new Packet41EntityEffect(player.entityId, effect));
        player.addExperienceLevel(0);
        GameRegistry.onPlayerChangedDimension(player);
    }

}
