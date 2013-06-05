package lain.mods.permissionmanager.permission;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class PermissionHolder
{

    protected Set<Permission> permissions = new TreeSet();
    protected List<PermissionHolder> parents = new ArrayList();
    private final Map<Permission, Boolean> cachedResults = new HashMap();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private boolean _hasPermission0(Permission permission)
    {
        for (Permission perm : permissions)
            if (perm.matches(permission))
                return true;
        for (PermissionHolder parent : parents)
            if (parent.hasPermission(permission))
                return true;
        return false;
    }

    public boolean addParent(PermissionHolder parent)
    {
        lock.writeLock().lock();
        try
        {
            if (!parents.contains(parent))
            {
                parents.add(parent);
                cachedResults.clear();
                return true;
            }
            return false;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean addPermission(Permission permission)
    {
        lock.writeLock().lock();
        try
        {
            if (!permissions.contains(permission))
            {
                permissions.add(permission);
                cachedResults.remove(permission);
                return true;
            }
            return false;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean addPermission(String permission)
    {
        return addPermission(new Permission(permission));
    }

    public boolean hasParent(PermissionHolder parent)
    {
        lock.readLock().lock();
        try
        {
            return parents.contains(parent);
        }
        finally
        {
            lock.readLock().unlock();
        }
    }

    public boolean hasPermission(Permission permission)
    {
        lock.readLock().lock();
        try
        {
            if (cachedResults.containsKey(permission))
                return cachedResults.get(permission);
        }
        finally
        {
            lock.readLock().unlock();
        }
        lock.writeLock().lock();
        try
        {
            boolean result = _hasPermission0(permission);
            cachedResults.put(permission, result);
            return result;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean hasPermission(String permission)
    {
        return hasPermission(permission, false);
    }

    public boolean hasPermission(String permission, boolean exact)
    {
        return hasPermission(exact ? new ExactPermission(permission) : new Permission(permission));
    }

    public void invalidate()
    {
        lock.writeLock().lock();
        try
        {
            cachedResults.clear();
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void invalidate(Permission permission)
    {
        lock.writeLock().lock();
        try
        {
            cachedResults.remove(permission);
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public void invalidate(String permission)
    {
        invalidate(new Permission(permission));
    }

    public List<PermissionHolder> parents()
    {
        return Collections.unmodifiableList(parents);
    }

    public Set<Permission> permissions()
    {
        return Collections.unmodifiableSet(permissions);
    }

    public boolean removeParent(PermissionHolder parent)
    {
        lock.writeLock().lock();
        try
        {
            if (parents.contains(parent))
            {
                parents.remove(parent);
                cachedResults.clear();
                return true;
            }
            return false;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean removePermission(Permission permission)
    {
        lock.writeLock().lock();
        try
        {
            if (permissions.contains(permission))
            {
                permissions.remove(permission);
                cachedResults.remove(permission);
                return true;
            }
            return false;
        }
        finally
        {
            lock.writeLock().unlock();
        }
    }

    public boolean removePermission(String permission)
    {
        return removePermission(new Permission(permission));
    }

}
