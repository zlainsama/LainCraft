package lain.mods.mobcontrol;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import lain.mods.laincraft.core.SharedConstants;
import lain.mods.laincraft.utils.configuration.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntityDamageSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event.Result;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingSpawnEvent;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "MobControl", name = "MobControl", version = "", dependencies = "required-after:LainCraftCore;after:*", useMetadata = true)
public class MobControl
{

    private Config config;

    private Controls global = new Controls();
    private Map<Integer, Controls> overrides = new HashMap();

    public void applyDimensionOverride(int dimension)
    {
        Controls override = overrides.get(dimension);
        if (override == null)
        {
            override = new Controls();
            Config config = new Config(new File(SharedConstants.getLainCraftDirFile(), "MobControl.DIM" + dimension + ".cfg"), "MobControl");
            config.load();
            override.load(config, false);
            config.save();
            overrides.put(dimension, override);
        }
        global.override = override;
    }

    public String getEntityName(EntityLiving ent, boolean flag)
    {
        String n = ent != null ? EntityList.getEntityString(ent) : null;
        if (flag && n != null && ent instanceof EntityTameable && ((EntityTameable) ent).isTamed())
            n = n + ".Tamed";
        return n;
    }

    @Mod.PreInit
    public void init(FMLPreInitializationEvent event)
    {
        config = new Config(new File(SharedConstants.getLainCraftDirFile(), "MobControl.cfg"), "MobControl");
    }

    @Mod.PostInit
    public void modsLoaded(FMLPostInitializationEvent event)
    {
        config.load();
        global.load(config, true);
        config.save();
        MinecraftForge.EVENT_BUS.register(this);
    }

    @ForgeSubscribe
    public void onLivingHurt(LivingHurtEvent event)
    {
        if (event.source.canHarmInCreative())
            return;
        double d = 1.0D;
        if (event.source instanceof EntityDamageSource)
        {
            Entity ent = event.source.getSourceOfDamage();
            if (ent instanceof EntityLiving && !(ent instanceof EntityPlayer))
            {
                String n = getEntityName((EntityLiving) ent, true);
                applyDimensionOverride(ent.dimension);
                d = global.getDouble(n, SettingNames.DamageMultiplier);
                releaseDimensionOverride();
            }
        }
        EntityLiving living = event.entityLiving;
        if (living != null && !(living instanceof EntityPlayer))
        {
            applyDimensionOverride(living.dimension);
            String n = getEntityName(living, true);
            if (global.getBoolean(n, SettingNames.Invincibility))
                d = 0.0D;
            else
                d = d / global.getDouble(n, SettingNames.HealthMultiplier);
            if (d > 1.0D)
            {
                double dmg = event.ammount * d * 25D;
                dmg += living.carryoverDamage;
                living.carryoverDamage = (int) (dmg % 25D);
                event.ammount = (int) (dmg / 25D);
            }
            else if (d < 0.0D)
                event.ammount = 0;
            else if (d < 1.0D)
            {
                double dmg = event.ammount * 25D;
                dmg += living.carryoverDamage;
                dmg -= (dmg * (1.0D - d));
                living.carryoverDamage = (int) (dmg % 25D);
                event.ammount = (int) (dmg / 25D);
            }
            releaseDimensionOverride();
        }
    }

    @ForgeSubscribe
    public void onSpawnCheck(LivingSpawnEvent.CheckSpawn event)
    {
        applyDimensionOverride(event.world.provider.dimensionId);
        String n = getEntityName(event.entityLiving, false);
        if (!global.getBoolean(n, SettingNames.Spawn))
            event.setResult(Result.DENY);
        releaseDimensionOverride();
    }

    public void releaseDimensionOverride()
    {
        global.override = null;
    }

}
