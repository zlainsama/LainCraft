package lain.mods.laincraft.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class ReflectionUtils
{

    public static Field getDeclaredField(Class cls, String name)
    {
        Field f = null;
        for (Class c = cls; c != Object.class; c = c.getSuperclass())
        {
            try
            {
                f = c.getDeclaredField(name);
                return f;
            }
            catch (Throwable ignored)
            {
            }
        }
        return null;
    }

    public static Field getDeclaredField(Object obj, String name)
    {
        return getDeclaredField(obj.getClass(), name);
    }

    public static Method getDeclaredMethod(Class cls, String name, Class... paramtypes)
    {
        Method m = null;
        for (Class c = cls; c != Object.class; c = c.getSuperclass())
        {
            try
            {
                m = c.getDeclaredMethod(name, paramtypes);
                return m;
            }
            catch (Throwable ignored)
            {
            }
        }
        return null;
    }

    public static Method getDeclaredMethod(Object obj, String name, Class... paramtypes)
    {
        return getDeclaredMethod(obj.getClass(), name, paramtypes);
    }

    public static boolean parseField(Field f, Object o, String s)
    {
        try
        {
            Class t = f.getType();
            if (boolean.class.isAssignableFrom(t))
                f.set(o, Boolean.parseBoolean(s));
            else if (Boolean.class.isAssignableFrom(t))
                f.set(o, Boolean.valueOf(s));
            else if (byte.class.isAssignableFrom(t))
                f.set(o, Byte.parseByte(s));
            else if (Byte.class.isAssignableFrom(t))
                f.set(o, Byte.valueOf(s));
            else if (char.class.isAssignableFrom(t))
                f.set(o, s.charAt(0));
            else if (Character.class.isAssignableFrom(t))
                f.set(o, Character.valueOf(s.charAt(0)));
            else if (short.class.isAssignableFrom(t))
                f.set(o, Short.parseShort(s));
            else if (Short.class.isAssignableFrom(t))
                f.set(o, Short.valueOf(s));
            else if (int.class.isAssignableFrom(t))
                f.set(o, Integer.parseInt(s));
            else if (Integer.class.isAssignableFrom(t))
                f.set(o, Integer.valueOf(s));
            else if (long.class.isAssignableFrom(t))
                f.set(o, Long.parseLong(s));
            else if (Long.class.isAssignableFrom(t))
                f.set(o, Long.valueOf(s));
            else if (float.class.isAssignableFrom(t))
                f.set(o, Float.parseFloat(s));
            else if (Float.class.isAssignableFrom(t))
                f.set(o, Float.valueOf(s));
            else if (double.class.isAssignableFrom(t))
                f.set(o, Double.parseDouble(s));
            else if (Double.class.isAssignableFrom(t))
                f.set(o, Double.valueOf(s));
            else if (String.class.isAssignableFrom(t))
                f.set(o, s);
            else
                return false;
            return true;
        }
        catch (Throwable t)
        {
            return false;
        }
    }

    public static boolean parseField(Field f, String s)
    {
        return parseField(f, null, s);
    }

    public static String toString(Field f)
    {
        return toString(f, null);
    }

    public static String toString(Field f, Object o)
    {
        try
        {
            o = f.get(o);
            if (o != null)
            {
                Class t = o.getClass();
                if (boolean.class.isAssignableFrom(t) || Boolean.class.isAssignableFrom(t))
                    return Boolean.toString((Boolean) o);
                else if (byte.class.isAssignableFrom(t) || Byte.class.isAssignableFrom(t))
                    return Byte.toString((Byte) o);
                else if (char.class.isAssignableFrom(t) || Character.class.isAssignableFrom(t))
                    return Character.toString((Character) o);
                else if (short.class.isAssignableFrom(t) || Short.class.isAssignableFrom(t))
                    return Short.toString((Short) o);
                else if (int.class.isAssignableFrom(t) || Integer.class.isAssignableFrom(t))
                    return Integer.toString((Integer) o);
                else if (long.class.isAssignableFrom(t) || Long.class.isAssignableFrom(t))
                    return Long.toString((Long) o);
                else if (float.class.isAssignableFrom(t) || Float.class.isAssignableFrom(t))
                    return Float.toString((Float) o);
                else if (double.class.isAssignableFrom(t) || Double.class.isAssignableFrom(t))
                    return Double.toString((Double) o);
                else if (String.class.isAssignableFrom(t))
                    return (String) o;
            }
            return null;
        }
        catch (Throwable t)
        {
            return null;
        }
    }

}
