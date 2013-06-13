package lain.mods.mobcontrol;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import lain.mods.laincraft.utils.configuration.Config;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.util.StatCollector;

public class Controls
{

    private final Map<String, Map<SettingNames, Setting>> settings = new HashMap();
    public Controls override;

    public boolean getBoolean(String name, SettingNames setting)
    {
        if (override != null && (override.settings.containsKey(name) && override.settings.get(name).containsKey(setting)))
            return override.settings.get(name).get(setting).getBoolean();
        return settings.containsKey(name) && settings.get(name).containsKey(setting) ? settings.get(name).get(setting).getBoolean() : Boolean.parseBoolean(setting.defaultValue);
    }

    public double getDouble(String name, SettingNames setting)
    {
        if (override != null && (override.settings.containsKey(name) && override.settings.get(name).containsKey(setting)))
            return override.settings.get(name).get(setting).getDouble();
        try
        {
            return settings.containsKey(name) && settings.get(name).containsKey(setting) ? settings.get(name).get(setting).getDouble() : Double.parseDouble(setting.defaultValue);
        }
        catch (Throwable t)
        {
            return 0D;
        }
    }

    public int getInt(String name, SettingNames setting)
    {
        if (override != null && (override.settings.containsKey(name) && override.settings.get(name).containsKey(setting)))
            return override.settings.get(name).get(setting).getInt();
        try
        {
            return settings.containsKey(name) && settings.get(name).containsKey(setting) ? settings.get(name).get(setting).getInt() : Integer.parseInt(setting.defaultValue);
        }
        catch (Throwable t)
        {
            return 0;
        }
    }

    public String getString(String name, SettingNames setting)
    {
        if (override != null && (override.settings.containsKey(name) && override.settings.get(name).containsKey(setting)))
            return override.settings.get(name).get(setting).getString();
        return settings.containsKey(name) && settings.get(name).containsKey(setting) ? settings.get(name).get(setting).getString() : setting.defaultValue;
    }

    public void load(Config config, boolean writeDefaults)
    {
        for (String name : (Set<String>) EntityList.stringToClassMapping.keySet())
        {
            Class cls = (Class) EntityList.stringToClassMapping.get(name);
            if (EntityLiving.class.isAssignableFrom(cls))
            {
                String displayName = StatCollector.translateToLocal("entity." + name + ".name");
                boolean tameable = EntityTameable.class.isAssignableFrom(cls);
                load(config, writeDefaults, name, displayName, false);
                if (tameable)
                    load(config, writeDefaults, name + ".Tamed", displayName, true);
            }
        }
    }

    private void load(Config config, boolean writeDefaults, String name, String displayName, boolean tamed)
    {
        for (SettingNames setting : SettingNames.values())
        {
            if (tamed && !setting.availableForTamedEntity)
                continue;
            String k = name + "." + setting.configName;
            if (writeDefaults && !config.containsKey(k))
                config.setProperty(k, setting.defaultValue);
            if (config.containsKey(k))
            {
                Map<SettingNames, Setting> var1 = settings.get(name);
                if (var1 == null)
                {
                    var1 = new HashMap();
                    settings.put(name, var1);
                }
                Setting var2 = var1.get(setting);
                if (var2 == null)
                {
                    var2 = new Setting(setting.name(), setting.defaultValue);
                    var1.put(setting, var2);
                }
                var2.set(config.getProperty(k, setting.defaultValue));
                if (!tamed && setting == SettingNames.topmostName)
                    config.get(k).comment = displayName;
            }
        }
    }

}
