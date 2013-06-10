package lain.mods.bilicraftcomments.client;

import lain.mods.bilicraftcomments.BilicraftComments;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiComment extends GuiScreen
{

    public static int mode = 0;
    public static int lifespan = 200;

    protected GuiTextField inputField;

    public boolean doesGuiPauseGame()
    {
        return false;
    }

    public void drawScreen(int par1, int par2, float par3)
    {
        drawRect(2, height - 14, width - 2, height - 2, Integer.MIN_VALUE);
        inputField.drawTextBox();
        super.drawScreen(par1, par2, par3);
    }

    public void handleMouseInput()
    {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();
        if (i != 0)
        {
            if (i > 1)
                i = 1;
            if (i < -1)
                i = -1;
            if (!isShiftKeyDown())
                i *= 7;
            mc.ingameGUI.getChatGUI().scroll(i);
        }
    }

    public void initGui()
    {
        Keyboard.enableRepeatEvents(true);
        inputField = new GuiTextField(fontRenderer, 4, height - 12, width - 4, 12);
        inputField.setMaxStringLength(100);
        inputField.setEnableBackgroundDrawing(false);
        inputField.setFocused(true);
        inputField.setText("");
        inputField.setCanLoseFocus(false);
    }

    protected void keyTyped(char par1, int par2)
    {
        if (par2 == 1)
        {
            mc.displayGuiScreen(null);
        }
        else if (par2 == 28)
        {
            String s = inputField.getText().trim();
            if (s.length() > 0)
                mc.thePlayer.sendQueue.addToSendQueue(BilicraftComments.createRequestPacket(mode, lifespan, s));
            mc.displayGuiScreen(null);
        }
        else if (par2 == 201)
        {
            mc.ingameGUI.getChatGUI().scroll(mc.ingameGUI.getChatGUI().func_96127_i() - 1);
        }
        else if (par2 == 209)
        {
            mc.ingameGUI.getChatGUI().scroll(-mc.ingameGUI.getChatGUI().func_96127_i() + 1);
        }
        else
        {
            inputField.textboxKeyTyped(par1, par2);
        }
    }

    protected void mouseClicked(int par1, int par2, int par3)
    {
        inputField.mouseClicked(par1, par2, par3);
        super.mouseClicked(par1, par2, par3);
    }

    public void onGuiClosed()
    {
        Keyboard.enableRepeatEvents(false);
        mc.ingameGUI.getChatGUI().resetScroll();
    }

    public void updateScreen()
    {
        inputField.updateCursorCounter();
    }

}
