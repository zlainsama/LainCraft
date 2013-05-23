package lain.mods.mobcontrol;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import lain.mods.laincraft.core.SharedConstants;
import lain.mods.laincraft.utils.configuration.Config;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.StatCollector;
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

    private Map<String, Boolean> Spawn = new HashMap();
    private Map<String, Double> HealthMultiplier = new HashMap();
    private Map<String, Double> DamageMultiplier = new HashMap();
    private Map<String, Boolean> Invincibility = new HashMap();

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

    public void loadEntitySettings(String entityName, boolean tamed, String displayName)
    {
        if (!tamed)
        {
            if (!config.containsKey("SPAWN." + entityName))
                config.setProperty("SPAWN." + entityName, "true");
            config.get("SPAWN." + entityName).comment = displayName;
            Spawn.put(entityName, Boolean.parseBoolean(config.getProperty("SPAWN." + entityName)));
        }
        if (!config.containsKey("HEALTH." + entityName))
            config.setProperty("HEALTH." + entityName, "1.0");
        config.get("HEALTH." + entityName).comment = displayName;
        HealthMultiplier.put(entityName, Double.parseDouble(config.getProperty("HEALTH." + entityName)));
        if (!config.containsKey("DAMAGE." + entityName))
            config.setProperty("DAMAGE." + entityName, "1.0");
        config.get("DAMAGE." + entityName).comment = displayName;
        DamageMultiplier.put(entityName, Double.parseDouble(config.getProperty("DAMAGE." + entityName)));
        if (!config.containsKey("INVINCIBILITY." + entityName))
            config.setProperty("INVINCIBILITY." + entityName, "false");
        config.get("INVINCIBILITY." + entityName).comment = displayName;
        Invincibility.put(entityName, Boolean.parseBoolean(config.getProperty("INVINCIBILITY." + entityName)));
    }

    @Mod.PostInit
    public void modsLoaded(FMLPostInitializationEvent event)
    {
        config.load();
        for (String name : new TreeSet<String>(EntityList.stringToClassMapping.keySet()))
        {
            Class cls = (Class) EntityList.stringToClassMapping.get(name);
            if (EntityLiving.class.isAssignableFrom(cls))
            {
                String displayName = StatCollector.translateToLocal("entity." + name + ".name");
                loadEntitySettings(name, false, displayName);
                if (EntityTameable.class.isAssignableFrom(cls))
                    loadEntitySettings(name + ".Tamed", true, displayName);
            }
        }
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
                if (DamageMultiplier.containsKey(n))
                    d = DamageMultiplier.get(n);
            }
        }
        EntityLiving living = event.entityLiving;
        if (living != null && !(living instanceof EntityPlayer))
        {
            String n = getEntityName(living, true);
            if (Invincibility.containsKey(n) && Invincibility.get(n))
                d = 0.0D;
            else if (HealthMultiplier.containsKey(n))
                d = d / HealthMultiplier.get(n);
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
        }
    }

    @ForgeSubscribe
    public void onSpawnCheck(LivingSpawnEvent.CheckSpawn event)
    {
        String n = getEntityName(event.entityLiving, false);
        if (Spawn.containsKey(n) && !Spawn.get(n))
            event.setResult(Result.DENY);
    }

}
