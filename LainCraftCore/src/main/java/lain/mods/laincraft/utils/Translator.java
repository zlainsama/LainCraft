package lain.mods.laincraft.utils;

import net.minecraft.command.ICommandSender;
import cpw.mods.fml.common.registry.LanguageRegistry;

public class Translator
{

    private String key;
    private String lang;

    public Translator()
    {
        this.lang = "en_US";
    }

    public Translator(String key)
    {
        this();
        this.key = key;
    }

    public void a(String value)
    {
        a(value, l());
    }

    public void a(String value, String lang)
    {
        LanguageRegistry.instance().addStringLocalization(k(), lang, value);
    }

    public String k()
    {
        return k(null);
    }

    public String k(String newKey)
    {
        if (newKey != null)
            key = newKey;
        return key;
    }

    public String l()
    {
        return l(null);
    }

    public String l(String newLang)
    {
        if (newLang != null)
            lang = newLang;
        return lang;
    }

    public void s(ICommandSender receiver)
    {
        s(receiver, null, null);
    }

    public void s(ICommandSender receiver, String head)
    {
        s(receiver, head, null);
    }

    public void s(ICommandSender receiver, String head, String tail)
    {
        receiver.sendChatToPlayer((head == null ? "" : head) + t(receiver) + (tail == null ? "" : tail));
    }

    public String t()
    {
        return t(l());
    }

    public String t(ICommandSender receiver)
    {
        return receiver.translateString(k(), new Object[0]);
    }

    public String t(String lang)
    {
        return LanguageRegistry.instance().getStringLocalization(k(), lang);
    }

}
