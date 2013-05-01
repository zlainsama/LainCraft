package lain.mods.laincraft.permission;

public interface Permission
{

    void onAttach(PermissionUser user, boolean silent);

    void onDetach(PermissionUser user, boolean silent);

}
