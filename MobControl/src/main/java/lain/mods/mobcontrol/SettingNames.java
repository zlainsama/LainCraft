package lain.mods.mobcontrol;

public enum SettingNames
{

    Spawn("true", "SPAWN", false), //
    HealthMultiplier("1.0", "HEALTH", true), //
    DamageMultiplier("1.0", "DAMAGE", true), //
    Invincibility("false", "INVINCIBILITY", true); //

    public static final SettingNames topmostName;

    static
    {
        SettingNames topmost = null;
        for (SettingNames name : values())
        {
            if (topmost == null || topmost.configName.compareTo(name.configName) > 0)
                topmost = name;
        }
        topmostName = topmost;
    }

    public final String defaultValue;
    public final String configName;
    public final boolean availableForTamedEntity;

    private SettingNames(String par1, String par2, boolean par3)
    {
        defaultValue = par1;
        configName = par2;
        availableForTamedEntity = par3;
    }

}
