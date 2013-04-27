package lain.mods.laincraft;

public abstract class Plugin implements Comparable
{

    public String getName()
    {
        return getClass().getSimpleName();
    }

    @Override
    public int compareTo(Object o)
    {
        if (o instanceof Plugin)
            return getName().compareTo(((Plugin) o).getName());
        return 0;
    }

}
