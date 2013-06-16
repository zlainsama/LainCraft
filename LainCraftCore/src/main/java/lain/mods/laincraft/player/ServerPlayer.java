package lain.mods.laincraft.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import lain.mods.laincraft.core.SharedConstants;
import lain.mods.laincraft.utils.MinecraftUtils;
import lain.mods.laincraft.utils.PositionData;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemInWorldManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

public class ServerPlayer extends EntityPlayerMP
{

    private IInventory PersonalStorage;
    private PositionData lastPosition;
    private PositionData homePosition;

    private int regenTimer = 0;

    private final boolean flag;

    public ServerPlayer(MinecraftServer par1MinecraftServer, World par2World, String par3Str, ItemInWorldManager par4ItemInWorldManager)
    {
        super(par1MinecraftServer, par2World, par3Str, par4ItemInWorldManager);
        PersonalStorage = new InventoryBasic(par3Str + "'s PersonalStorage", true, 54);
        flag = SharedConstants.isLain(username);
    }

    private int _absorbDamage(EntityLiving ent, double damage, double ratio)
    {
        damage *= 25;
        damage += ent.carryoverDamage;
        damage -= (damage * ratio);
        ent.carryoverDamage = (int) damage % 25;
        return (int) (damage / 25D);
    }

    public PositionData _getHomePosition()
    {
        return homePosition;
    }

    public PositionData _getLastPosition()
    {
        return lastPosition;
    }

    public void _openPersonalStorage()
    {
        if (PersonalStorage != null)
            displayGUIChest(PersonalStorage);
    }

    public void _setHomePosition(PositionData par1)
    {
        homePosition = par1;
    }

    public void _setLastPosition(PositionData par1)
    {
        lastPosition = par1;
    }

    public void _teleportTo(PositionData par1)
    {
        _setLastPosition(new PositionData(this));
        par1.teleportEntity(this);
    }

    public void _teleportTo(PositionData par1, boolean checkCollision)
    {
        _setLastPosition(new PositionData(this));
        par1.teleportEntity(this, checkCollision);
    }

    @Override
    public void clonePlayer(EntityPlayer par1, boolean par2)
    {
        super.clonePlayer(par1, par2);

        if (par1 instanceof ServerPlayer)
        {
            PersonalStorage = ((ServerPlayer) par1).PersonalStorage;
            lastPosition = ((ServerPlayer) par1).lastPosition;
            homePosition = ((ServerPlayer) par1).homePosition;
        }
    }

    @Override
    protected void damageEntity(DamageSource par1, int par2)
    {
        if (flag)
        {
            if (par2 > 0)
                regenTimer += 20;
            if (!par1.isUnblockable())
                par2 = _absorbDamage(this, par2, 0.28D);
            par2 = _absorbDamage(this, par2, 0.40D);
            if (par2 > 0)
                regenTimer += 10 + par2 * 10;
        }
        super.damageEntity(par1, par2);
    }

    @Override
    public void onDeath(DamageSource par1)
    {
        super.onDeath(par1);

        if (!isEntityAlive())
            _setLastPosition(new PositionData(this));
    }

    @Override
    public void onLivingUpdate()
    {
        if (flag)
        {
            if (shouldHeal())
            {
                if (regenTimer > 0)
                {
                    if (regenTimer > 200)
                        regenTimer = 200;
                    regenTimer--;
                }
                else
                    heal(Math.max(1, (int) (getMaxHealth() * 0.05)));
            }
            else
                regenTimer = 30;
        }
        super.onLivingUpdate();
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1)
    {
        super.readEntityFromNBT(par1);

        for (int i = 0; i < PersonalStorage.getSizeInventory(); i++)
            PersonalStorage.setInventorySlotContents(i, null);
        File data = new File(MinecraftUtils.getCustomSaveDirectory("storage"), username + ".dat");
        try
        {
            NBTTagCompound var1 = CompressedStreamTools.readCompressed(new FileInputStream(data));
            if (var1.hasKey("PersonalStorage") && !(var1.getTag("PersonalStorage") instanceof NBTTagList))
                var1.removeTag("PersonalStorage");
            if (var1.hasKey("PersonalStorage"))
            {
                NBTTagList var2 = var1.getTagList("PersonalStorage");
                for (int i = 0; i < var2.tagCount(); i++)
                {
                    NBTTagCompound var3 = (NBTTagCompound) var2.tagAt(i);
                    int var4 = var3.getByte("Slot") & 0xFF;
                    if (var4 >= 0 && var4 < PersonalStorage.getSizeInventory())
                        PersonalStorage.setInventorySlotContents(var4, ItemStack.loadItemStackFromNBT(var3));
                }
            }
        }
        catch (Exception e)
        {
        }

        data = new File(MinecraftUtils.getCustomSaveDirectory("lastPos"), username + ".dat");
        try
        {
            NBTTagCompound var1 = CompressedStreamTools.readCompressed(new FileInputStream(data));
            lastPosition = new PositionData();
            lastPosition.readFromNBT(var1);
        }
        catch (Exception e)
        {
        }

        data = new File(MinecraftUtils.getCustomSaveDirectory("home"), username + ".dat");
        try
        {
            NBTTagCompound var1 = CompressedStreamTools.readCompressed(new FileInputStream(data));
            homePosition = new PositionData();
            homePosition.readFromNBT(var1);
        }
        catch (Exception e)
        {
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1)
    {
        super.writeEntityToNBT(par1);

        File data = new File(MinecraftUtils.getCustomSaveDirectory("storage"), username + ".dat");
        NBTTagList var1 = new NBTTagList();
        for (int i = 0; i < PersonalStorage.getSizeInventory(); i++)
        {
            ItemStack var2 = PersonalStorage.getStackInSlot(i);
            if (var2 != null)
            {
                NBTTagCompound var3 = new NBTTagCompound();
                var2.writeToNBT(var3);
                var3.setByte("Slot", (byte) i);
                var1.appendTag(var3);
            }
        }
        NBTTagCompound var2 = new NBTTagCompound();
        var2.setTag("PersonalStorage", var1);
        try
        {
            CompressedStreamTools.writeCompressed(var2, new FileOutputStream(data));
        }
        catch (Exception e)
        {
        }

        data = new File(MinecraftUtils.getCustomSaveDirectory("lastPos"), username + ".dat");
        try
        {
            if (lastPosition == null && data.exists())
                data.delete();
            else if (lastPosition != null)
            {
                var2 = new NBTTagCompound();
                lastPosition.writeToNBT(var2);
                CompressedStreamTools.writeCompressed(var2, new FileOutputStream(data));
            }
        }
        catch (Exception e)
        {
        }

        data = new File(MinecraftUtils.getCustomSaveDirectory("home"), username + ".dat");
        try
        {
            if (homePosition == null && data.exists())
                data.delete();
            else if (homePosition != null)
            {
                var2 = new NBTTagCompound();
                homePosition.writeToNBT(var2);
                CompressedStreamTools.writeCompressed(var2, new FileOutputStream(data));
            }
        }
        catch (Exception e)
        {
        }
    }

}
