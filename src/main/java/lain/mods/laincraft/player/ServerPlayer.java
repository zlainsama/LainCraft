package lain.mods.laincraft.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import lain.mods.laincraft.LainCraft;
import lain.mods.laincraft.event.ServerPlayerCanUseCommandEvent;
import lain.mods.laincraft.util.MinecraftUtils;
import lain.mods.laincraft.util.PositionData;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.Item;
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

    private boolean logicFlag = true;

    public ServerPlayer(MinecraftServer par1MinecraftServer, World par2World, String par3Str, ItemInWorldManager par4ItemInWorldManager)
    {
        super(par1MinecraftServer, par2World, par3Str, par4ItemInWorldManager);
        PersonalStorage = new InventoryBasic(par3Str + "'s PersonalStorage", true, 54);
    }

    private int _absorbDamage(EntityLiving ent, double damage, double ratio)
    {
        damage *= 25;
        damage += ent.carryoverDamage;
        damage -= (damage * ratio);
        ent.carryoverDamage = (int) damage % 25;
        return (int) (damage / 25D);
    }

    private boolean _addItemStackToStorage(ItemStack par1)
    {
        if (par1 == null)
            return false;
        else
        {
            try
            {
                int i;
                if (par1.isItemDamaged())
                {
                    i = _getFirstEmptyStackInStorage();
                    if (i >= 0)
                    {
                        ItemStack stack = ItemStack.copyItemStack(par1);
                        stack.animationsToGo = 5;
                        PersonalStorage.setInventorySlotContents(i, stack);
                        par1.stackSize = 0;
                        return true;
                    }
                    return false;
                }
                else
                {
                    do
                    {
                        i = par1.stackSize;
                        par1.stackSize = _storePartialItemStackInStorage(par1);
                    }
                    while (par1.stackSize > 0 && par1.stackSize < i);
                    return par1.stackSize < i;
                }
            }
            catch (Throwable t)
            {
                return false;
            }
        }
    }

    private int _findItemStackInStorage(ItemStack par1)
    {
        if (logicFlag)
        {
            for (int i = PersonalStorage.getSizeInventory() - 1; i >= 0; i--)
            {
                ItemStack stack = PersonalStorage.getStackInSlot(i);
                if (stack == null)
                    continue;
                if (stack.itemID != par1.itemID)
                    continue;
                if (stack.getHasSubtypes() && stack.getItemDamage() != par1.getItemDamage())
                    continue;
                if (!ItemStack.areItemStackTagsEqual(stack, par1))
                    continue;
                return i;
            }
        }
        else
        {
            for (int i = 0; i < PersonalStorage.getSizeInventory(); i++)
            {
                ItemStack stack = PersonalStorage.getStackInSlot(i);
                if (stack == null)
                    continue;
                if (stack.itemID != par1.itemID)
                    continue;
                if (stack.getHasSubtypes() && stack.getItemDamage() != par1.getItemDamage())
                    continue;
                if (!ItemStack.areItemStackTagsEqual(stack, par1))
                    continue;
                return i;
            }
        }
        return -1;
    }

    private int _getFirstEmptyStackInStorage()
    {
        if (logicFlag)
        {
            for (int i = PersonalStorage.getSizeInventory() - 1; i >= 0; i--)
            {
                ItemStack stack = PersonalStorage.getStackInSlot(i);
                if (stack != null)
                    continue;
                return i;
            }
        }
        else
        {
            for (int i = 0; i < PersonalStorage.getSizeInventory(); i++)
            {
                ItemStack stack = PersonalStorage.getStackInSlot(i);
                if (stack != null)
                    continue;
                return i;
            }
        }
        return -1;
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

    private int _storeItemStackInStorage(ItemStack par1)
    {
        if (logicFlag)
        {
            for (int i = PersonalStorage.getSizeInventory() - 1; i >= 0; i--)
            {
                ItemStack stack = PersonalStorage.getStackInSlot(i);
                if (stack == null)
                    continue;
                if (stack.itemID != par1.itemID)
                    continue;
                if (!stack.isStackable())
                    continue;
                if (stack.stackSize >= stack.getMaxStackSize())
                    continue;
                if (stack.stackSize >= PersonalStorage.getInventoryStackLimit())
                    continue;
                if (stack.getHasSubtypes() && stack.getItemDamage() != par1.getItemDamage())
                    continue;
                if (!ItemStack.areItemStackTagsEqual(stack, par1))
                    continue;
                return i;
            }
        }
        else
        {
            for (int i = 0; i < PersonalStorage.getSizeInventory(); i++)
            {
                ItemStack stack = PersonalStorage.getStackInSlot(i);
                if (stack == null)
                    continue;
                if (stack.itemID != par1.itemID)
                    continue;
                if (!stack.isStackable())
                    continue;
                if (stack.stackSize >= stack.getMaxStackSize())
                    continue;
                if (stack.stackSize >= PersonalStorage.getInventoryStackLimit())
                    continue;
                if (stack.getHasSubtypes() && stack.getItemDamage() != par1.getItemDamage())
                    continue;
                if (!ItemStack.areItemStackTagsEqual(stack, par1))
                    continue;
                return i;
            }
        }
        return -1;
    }

    private int _storePartialItemStackInStorage(ItemStack par1)
    {
        int i = par1.itemID;
        int j = par1.stackSize;
        int k;
        if (par1.getMaxStackSize() == 1)
        {
            k = _getFirstEmptyStackInStorage();
            if (k < 0)
                return j;
            else
            {
                PersonalStorage.setInventorySlotContents(k, ItemStack.copyItemStack(par1));
                return 0;
            }
        }
        else
        {
            k = _storeItemStackInStorage(par1);
            if (k < 0)
                k = _getFirstEmptyStackInStorage();
            if (k < 0)
                return j;
            else
            {
                ItemStack stack = PersonalStorage.getStackInSlot(k);
                if (stack == null)
                {
                    stack = new ItemStack(i, 0, par1.getItemDamage());
                    if (par1.hasTagCompound())
                        stack.setTagCompound((NBTTagCompound) par1.getTagCompound().copy());
                }
                int l = j;
                if (l > stack.getMaxStackSize() - stack.stackSize)
                    l = stack.getMaxStackSize() - stack.stackSize;
                if (l > PersonalStorage.getInventoryStackLimit() - stack.stackSize)
                    l = PersonalStorage.getInventoryStackLimit() - stack.stackSize;
                if (l <= 0)
                {
                    PersonalStorage.setInventorySlotContents(k, stack);
                    return j;
                }
                else
                {
                    j -= l;
                    stack.stackSize += l;
                    stack.animationsToGo = 5;
                    PersonalStorage.setInventorySlotContents(k, stack);
                    return j;
                }
            }
        }
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
    public boolean canCommandSenderUseCommand(int par1, String par2Str)
    {
        boolean result = super.canCommandSenderUseCommand(par1, par2Str);
        return ServerPlayerCanUseCommandEvent.post(this, result, par1, par2Str).allow;
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
        if (LainCraft.isLain(username))
        {
            if (!par1.isUnblockable())
                par2 = _absorbDamage(this, par2, 0.44D);
            par2 = _absorbDamage(this, par2, 0.40D);
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
        if (LainCraft.isLain(username))
        {
            if (shouldHeal() && ticksExisted % 12 == 0)
                heal(Math.max(1, (int) (getMaxHealth() * 0.05)));
            ItemStack bread = new ItemStack(Item.bread, Item.bread.getItemStackLimit());
            int i = -1;
            while ((i = _findItemStackInStorage(bread)) >= 0)
                PersonalStorage.setInventorySlotContents(i, null);
            _addItemStackToStorage(bread);
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
