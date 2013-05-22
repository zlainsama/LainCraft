package lain.mods.laincraft.utils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.packet.Packet28EntityVelocity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.World;

public class PositionData
{

    public static PositionData getSpawnPoint(World par1)
    {
        PositionData result = new PositionData(par1.getSpawnPoint());
        result.dimension = par1.getWorldInfo().getDimension();
        return result;
    }

    public int dimension = -999;
    public double x, y, z;
    public float yaw, pitch;

    public PositionData()
    {
    }

    public PositionData(ChunkCoordinates par1)
    {
        this.x = (double) par1.posX + 0.5D;
        this.y = (double) par1.posY + 0.5D;
        this.z = (double) par1.posZ + 0.5D;
    }

    public PositionData(double x, double y, double z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public PositionData(double x, double y, double z, float yaw, float pitch)
    {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public PositionData(Entity par1)
    {
        this.dimension = par1.dimension;
        this.x = par1.posX;
        this.y = par1.posY;
        this.z = par1.posZ;
        this.yaw = par1.rotationYaw;
        this.pitch = par1.rotationPitch;
    }

    public PositionData(int dimension, double x, double y, double z, float yaw, float pitch)
    {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public void readFromNBT(NBTTagCompound par1)
    {
        dimension = par1.getInteger("dimension");
        x = par1.getDouble("x");
        y = par1.getDouble("y");
        z = par1.getDouble("z");
        yaw = par1.getFloat("yaw");
        pitch = par1.getFloat("pitch");
    }

    public void teleportEntity(Entity par1)
    {
        teleportEntity(par1, true);
    }

    public void teleportEntity(Entity par1, boolean checkCollision)
    {
        teleportEntity(par1, checkCollision, false);
    }

    public void teleportEntity(Entity par1, boolean checkCollision, boolean keepVelocity)
    {
        if (dimension != -999 && dimension != par1.dimension)
            par1.travelToDimension(dimension);
        double var1 = y;
        double var2 = par1.motionX;
        double var3 = par1.motionY;
        double var4 = par1.motionZ;
        par1.setPositionAndRotation(x, var1, z, yaw, pitch);
        if (checkCollision)
            while (!par1.worldObj.getCollidingBoundingBoxes(par1, par1.boundingBox).isEmpty())
                par1.setPosition(x, ++var1, z);
        if (!keepVelocity)
        {
            par1.fallDistance = 0.0F;
            par1.motionX = par1.motionY = par1.motionZ = 0D;
        }
        if (par1 instanceof EntityPlayerMP)
        {
            ((EntityPlayerMP) par1).playerNetServerHandler.setPlayerLocation(x, var1, z, yaw, pitch);
            if (keepVelocity)
            {
                par1.motionX = var2;
                par1.motionY = var3;
                par1.motionZ = var4;
                ((EntityPlayerMP) par1).playerNetServerHandler.sendPacketToPlayer(new Packet28EntityVelocity(par1));
            }
        }
    }

    public void writeToNBT(NBTTagCompound par1)
    {
        par1.setInteger("dimension", dimension);
        par1.setDouble("x", x);
        par1.setDouble("y", y);
        par1.setDouble("z", z);
        par1.setFloat("yaw", yaw);
        par1.setFloat("pitch", pitch);
    }

}
