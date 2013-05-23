package lain.mods.permissionmanager.permission;

public interface Permission
{

    public enum MatcherType
    {
        ALL('*'), ANY('?');

        public static MatcherType getMatcherType(String par1)
        {
            for (MatcherType type : values())
                if (type.toString().equals(par1))
                    return type;
            return null;
        }

        public final String s;

        private MatcherType(char par1)
        {
            s = Character.toString(par1);
        }

        @Override
        public String toString()
        {
            return s;
        }
    }

    MatcherType getMatcherType();

    String getName();

    boolean isMatcher();

    boolean matcherPass(MatcherType type);

    boolean matches(Permission par1);

    void onAttach(PermissionUser user);

    void onDetach(PermissionUser user);

}
