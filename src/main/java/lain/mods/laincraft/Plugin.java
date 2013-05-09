package lain.mods.laincraft;

import lain.mods.laincraft.util.configuration.Config;

public abstract class Plugin implements Comparable<Plugin>
{

    private boolean enabled = false;

    private Config config;

    @Override
    public int compareTo(Plugin o)
    {
        return getName().compareTo(o.getName());
    }

    public final void disable()
    {
        if (enabled)
        {
            enabled = false;
            onDisable();
        }
    }

    public final void enable()
    {
        if (!enabled)
        {
            enabled = true;
            onEnable();
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof Plugin)
            return getClass().getName().equals(o.getClass().getName());
        return false;
    }

    public final Config getConfig()
    {
        return config;
    }

    public abstract String getName();

    @Override
    public int hashCode()
    {
        return getClass().getName().hashCode();
    }

    public final boolean isEnabled()
    {
        return enabled;
    }

    public abstract void onDisable();

    public abstract void onEnable();

    public final void setConfig(Config config)
    {
        this.config = config;
    }

    protected final void setEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

}
