package net.minecraft.client.gui;

import java.nio.charset.Charset;
import org.lwjgl.input.Keyboard;

public class GuiScreenFix
{

    public static String encoding = Charset.defaultCharset().name();

    public static void handleKeyboardInput(GuiScreen gui)
    {
        if (Keyboard.getEventKeyState())
        {
            do
            {
                int k = Keyboard.getEventKey();
                char c = Keyboard.getEventCharacter();
                if (k == 87)
                {
                    gui.mc.toggleFullscreen();
                    return;
                }
                if (gui.isMacOs && k == 28 && c == 0)
                {
                    k = 29;
                }
                if (c > 0x7F && c <= 0xFF && Keyboard.next())
                {
                    int k2 = Keyboard.getEventKey();
                    char c2 = Keyboard.getEventCharacter();
                    try
                    {
                        c2 = new String(new byte[] { (byte) c, (byte) c2 }, encoding).charAt(0);
                        gui.keyTyped(c2, k);
                    }
                    catch (Throwable t)
                    {
                        gui.keyTyped(c, k);
                        gui.keyTyped(c2, k2);
                    }
                    continue;
                }
                gui.keyTyped(c, k);
            }
            while (Keyboard.next());
        }
    }

}
