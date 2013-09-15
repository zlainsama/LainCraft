package lain.mods.accman;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.ServerConfigurationManager;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.FMLCommonHandler;

public class Teleporter
{

    public static void teleport(Entity ent, int dimension, final double posX, final double posY, final double posZ, final float yaw, final float pitch)
    {
        if (ent.dimension != dimension)
        {
            MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
            ServerConfigurationManager conman = server.getConfigurationManager();
            WorldServer oldworld = server.worldServerForDimension(ent.dimension);
            WorldServer newworld = server.worldServerForDimension(dimension);
            net.minecraft.world.Teleporter teleporter = new net.minecraft.world.Teleporter(newworld)
            {
                @Override
                public void placeInPortal(Entity par1Entity, double par2, double par4, double par6, float par8)
                {
                    par1Entity.setLocationAndAngles(posX, posY, posZ, yaw, pitch);
                }
            };
            if (ent instanceof EntityPlayerMP)
                conman.transferPlayerToDimension((EntityPlayerMP) ent, dimension, teleporter);
            else
                conman.transferEntityToWorld(ent, dimension, oldworld, newworld, teleporter);
        }
        else
        {
            ent.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
            ent.motionX = ent.motionY = ent.motionZ = 0D;
            if (ent instanceof EntityPlayerMP)
                ((EntityPlayerMP) ent).playerNetServerHandler.setPlayerLocation(posX, posY, posZ, yaw, pitch);
        }
    }

}
