package lain.mods.mobcontrol;

public class Setting
{

    public final String name;
    private String value;
    private int intValue;
    private double doubleValue;
    private boolean booleanValue;

    public Setting(String name)
    {
        this.name = name;
        set("");
    }

    public Setting(String name, String defaultValue)
    {
        this.name = name;
        set(defaultValue);
    }

    public boolean getBoolean()
    {
        return booleanValue;
    }

    public double getDouble()
    {
        return doubleValue;
    }

    public int getInt()
    {
        return intValue;
    }

    public String getString()
    {
        return value;
    }

    public void set(boolean value)
    {
        set(Boolean.toString(value));
    }

    public void set(double value)
    {
        set(Double.toString(value));
    }

    public void set(int value)
    {
        set(Integer.toString(value));
    }

    public void set(String value)
    {
        this.value = value;
        try
        {
            intValue = Integer.parseInt(value);
        }
        catch (Throwable ignored)
        {
        }
        try
        {
            doubleValue = Double.parseDouble(value);
        }
        catch (Throwable ignored)
        {
        }
        try
        {
            booleanValue = Boolean.parseBoolean(value);
        }
        catch (Throwable ignored)
        {
        }
    }

}
