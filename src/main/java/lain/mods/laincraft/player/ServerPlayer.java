package lain.mods.laincraft.player;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import lain.mods.laincraft.LainCraft;
import lain.mods.laincraft.event.ServerPlayerCanUseCommandEvent;
import lain.mods.laincraft.util.MinecraftUtils;
import net.minecraft.entity.EntityLiving;
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

    public ServerPlayer(MinecraftServer par1MinecraftServer, World par2World, String par3Str, ItemInWorldManager par4ItemInWorldManager)
    {
        super(par1MinecraftServer, par2World, par3Str, par4ItemInWorldManager);
        PersonalStorage = new InventoryBasic(par3Str + "'s PersonalStorage", true, 54);
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
                heal(Math.max(1, (int) (getMaxHealth() * 0.05)));
        }
        super.onLivingUpdate();
    }

    public void openPersonalStorage()
    {
        if (PersonalStorage != null)
            displayGUIChest(PersonalStorage);
    }

    @Override
    public void readEntityFromNBT(NBTTagCompound par1)
    {
        super.readEntityFromNBT(par1);

        for (int i = 0; i < PersonalStorage.getSizeInventory(); i++)
            PersonalStorage.setInventorySlotContents(i, null);
        File saveDir = new File(new File(MinecraftUtils.getActiveSaveDirectory(), "LainCraft"), "storage");
        if (saveDir.exists() || saveDir.mkdirs())
        {
            File data = new File(saveDir, username + ".dat");
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
        }
    }

    @Override
    public void writeEntityToNBT(NBTTagCompound par1)
    {
        super.writeEntityToNBT(par1);

        File saveDir = new File(new File(MinecraftUtils.getActiveSaveDirectory(), "LainCraft"), "storage");
        if (saveDir.exists() || saveDir.mkdirs())
        {
            File data = new File(saveDir, username + ".dat");
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
        }
    }

}
