package lain.mods.laincraft.utils.configuration;

public class ConfigProperty
{

    private final String name;
    private String value;

    public String comment;

    public ConfigProperty(String par1)
    {
        this(par1, "");
    }

    public ConfigProperty(String par1, String par2)
    {
        name = par1;
        value = par2;
    }

    public Boolean getBoolean()
    {
        return Boolean.parseBoolean(value);
    }

    public Double getDouble()
    {
        return getDouble(0D);
    }

    public Double getDouble(Double par1)
    {
        try
        {
            return Double.parseDouble(value);
        }
        catch (NumberFormatException e)
        {
            return par1;
        }
    }

    public Integer getInteger()
    {
        return getInteger(0);
    }

    public Integer getInteger(Integer par1)
    {
        try
        {
            return Integer.parseInt(value);
        }
        catch (NumberFormatException e)
        {
            return par1;
        }
    }

    public String getName()
    {
        return name;
    }

    public String getString()
    {
        return getString("");
    }

    public String getString(String par1)
    {
        if (value == null)
            return par1;
        return value;
    }

    public void set(Boolean par1)
    {
        set(Boolean.toString(par1));
    }

    public void set(Double par1)
    {
        set(Double.toString(par1));
    }

    public void set(Integer par1)
    {
        set(Integer.toString(par1));
    }

    public void set(String par1)
    {
        value = par1;
    }

}
