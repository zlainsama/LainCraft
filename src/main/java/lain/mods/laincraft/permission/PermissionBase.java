package lain.mods.laincraft.permission;

public class PermissionBase implements Permission
{

    private final String name;
    private final MatcherType type;

    protected PermissionBase(String par1)
    {
        name = par1;
        type = MatcherType.getMatcherType(par1);
    }

    @Override
    public MatcherType getMatcherType()
    {
        return type;
    }

    @Override
    public String getName()
    {
        return name;
    }

    @Override
    public boolean isMatcher()
    {
        return type != null;
    }

    @Override
    public boolean matcherPass(MatcherType type)
    {
        return false;
    }

    @Override
    public boolean matches(Permission par1)
    {
        if (par1 == this)
            return true;
        if (type == null)
            return false;
        if (par1.matcherPass(type))
            return false;
        return getClass().isAssignableFrom(par1.getClass());
    }

    @Override
    public void onAttach(PermissionUser user)
    {
    }

    @Override
    public void onDetach(PermissionUser user)
    {
    }

}
