package lain.mods.permissionmanager.permission;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Permission implements Comparable<Permission>
{

    public final String name;
    private Set<Permission> wildcards;

    public Permission(String par1)
    {
        if (par1 == null)
            throw new IllegalArgumentException("permission name cant be null");
        if (par1.isEmpty())
            throw new IllegalArgumentException("permission name cant be empty");
        name = par1;
    }

    @Override
    public int compareTo(Permission o)
    {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
            return true;
        if (o instanceof Permission)
            return name.equals(((Permission) o).name);
        return false;
    }

    @Override
    public int hashCode()
    {
        return name.hashCode();
    }

    public boolean matches(Permission p)
    {
        if (equals(p))
            return true;
        if (name.equals("*") || name.endsWith(".*"))
            if (p.wildcards().contains(this))
                return true;
        return false;
    }

    public Set<Permission> wildcards()
    {
        if (wildcards == null)
        {
            Set<Permission> c = new HashSet();
            String base = "";
            for (String part : name.split("\\."))
            {
                c.add(new Permission(base.isEmpty() ? "*" : base.concat(".*")));
                base = base.isEmpty() ? part : base.concat(".").concat(part);
            }
            wildcards = Collections.unmodifiableSet(c);
        }
        return wildcards;
    }

}
