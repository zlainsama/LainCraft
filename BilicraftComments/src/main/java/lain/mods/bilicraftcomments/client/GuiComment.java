package lain.mods.bilicraftcomments.client;

import lain.mods.bilicraftcomments.BilicraftComments;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiComment extends GuiScreen
{

    public static enum ControlCodeArea
    {
        c0(EnumChatFormatting.BLACK.toString(), 11, 12, 16, 16), // 0
        c1(EnumChatFormatting.DARK_BLUE.toString(), 26, 12, 16, 16), // 1
        c2(EnumChatFormatting.DARK_GREEN.toString(), 41, 12, 16, 16), // 2
        c3(EnumChatFormatting.DARK_AQUA.toString(), 56, 12, 16, 16), // 3
        c4(EnumChatFormatting.DARK_RED.toString(), 71, 12, 16, 16), // 4
        c5(EnumChatFormatting.DARK_PURPLE.toString(), 86, 12, 16, 16), // 5
        c6(EnumChatFormatting.GOLD.toString(), 101, 12, 16, 16), // 6
        c7(EnumChatFormatting.GRAY.toString(), 116, 12, 16, 16), // 7
        c8(EnumChatFormatting.DARK_GRAY.toString(), 11, 27, 16, 16), // 8
        c9(EnumChatFormatting.BLUE.toString(), 26, 27, 16, 16), // 9
        ca(EnumChatFormatting.GREEN.toString(), 41, 27, 16, 16), // a
        cb(EnumChatFormatting.AQUA.toString(), 56, 27, 16, 16), // b
        cc(EnumChatFormatting.RED.toString(), 71, 27, 16, 16), // c
        cd(EnumChatFormatting.LIGHT_PURPLE.toString(), 86, 27, 16, 16), // d
        ce(EnumChatFormatting.YELLOW.toString(), 101, 27, 16, 16), // e
        cf(EnumChatFormatting.WHITE.toString(), 116, 27, 16, 16), // f
        // ck
        cl(EnumChatFormatting.BOLD.toString(), 136, 22, 20, 21), // l
        cm(EnumChatFormatting.STRIKETHROUGH.toString(), 202, 22, 20, 20), // m
        cn(EnumChatFormatting.UNDERLINE.toString(), 183, 23, 15, 18), // n
        co(EnumChatFormatting.ITALIC.toString(), 160, 22, 19, 20), // o
        cr(EnumChatFormatting.RESET.toString(), 225, 21, 21, 22); // r

        public final String code;
        public final Rect rect;

        private ControlCodeArea(String c, int x, int y, int w, int h)
        {
            code = c;
            rect = new Rect(x, y, w, h);
        }
    }

    public static enum LifespanControlArea
    {
        c0(20, 187, 106, 14, 9), // ++
        c1(2, 203, 106, 9, 9), // +
        c2(-20, 214, 106, 14, 9), // --
        c3(-2, 230, 106, 9, 9); // -

        public final int m;
        public final Rect rect;

        private LifespanControlArea(int modifier, int x, int y, int w, int h)
        {
            m = modifier;
            rect = new Rect(x, y, w, h);
        }
    }

    public static enum ModeArea
    {
        m0(0, 8, 47, 61, 46), // 0
        m1(1, 68, 47, 60, 46), // 1
        m2(2, 127, 47, 60, 46), // 2
        m3(3, 186, 47, 61, 46); // 3

        public final int id;
        public final Rect rect;

        private ModeArea(int i, int x, int y, int w, int h)
        {
            id = i;
            rect = new Rect(x, y, w, h);
        }
    }

    public static class Rect
    {
        public int x;
        public int y;
        public int w;
        public int h;

        public Rect()
        {
            x = y = w = h = 0;
        }

        public Rect(int x, int y, int w, int h)
        {
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public boolean checkCollision(int pointX, int pointY)
        {
            return !(pointX < x || pointY < y || pointX > (x + w) || pointY > (y + h));
        }

        public boolean isMouseHovering(int mouseX, int mouseY)
        {
            return (mouseX >= x && mouseY >= y && mouseX < (x + w) && mouseY < (y + h));
        }
    }

    public static boolean settingsOpened = false;

    public static int mode = 0;
    public static int lifespan = 200;

    public static void drawRectTexture(int x, int y, int w, int h, int u, int v, float zLevel)
    {
        Tessellator a = Tessellator.instance;
        a.startDrawingQuads();
        a.addVertexWithUV((double) (x + 0), (double) (y + h), (double) zLevel, (double) ((float) (u + 0) * fW), (double) ((float) (v + h) * fH));
        a.addVertexWithUV((double) (x + w), (double) (y + h), (double) zLevel, (double) ((float) (u + w) * fW), (double) ((float) (v + h) * fH));
        a.addVertexWithUV((double) (x + w), (double) (y + 0), (double) zLevel, (double) ((float) (u + w) * fW), (double) ((float) (v + 0) * fH));
        a.addVertexWithUV((double) (x + 0), (double) (y + 0), (double) zLevel, (double) ((float) (u + 0) * fW), (double) ((float) (v + 0) * fH));
        a.draw();
    }

    protected GuiTextField inputField;
    protected Rect areaSettingsButton;
    protected Rect areaSettings;

    public static final float fW = 0.00393700787401574803149606299213F;// 254
    public static final float fH = 0.00357142857142857142857142857143F;// 280

    public boolean doesGuiPauseGame()
    {
        return false;
    }

    public void drawScreen(int par1, int par2, float par3)
    {
        drawRect(2, height - 14, width - 2, height - 2, Integer.MIN_VALUE);
        inputField.drawTextBox();
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture("/BcC/gui.png");
        if (areaSettingsButton.isMouseHovering(par1, par2))
            drawRectTexture(areaSettingsButton.x, areaSettingsButton.y, 28, 28, 28, 0, zLevel);
        else
            drawRectTexture(areaSettingsButton.x, areaSettingsButton.y, 28, 28, 0, 0, zLevel);
        if (settingsOpened)
        {
            drawRectTexture(areaSettings.x, areaSettings.y, areaSettings.w, areaSettings.h, 0, 28, zLevel);
            int x = par1 - areaSettings.x;
            int y = par2 - areaSettings.y;
            for (ControlCodeArea area : ControlCodeArea.values())
            {
                if (area.rect.isMouseHovering(x, y))
                {
                    drawRectTexture(areaSettings.x + area.rect.x, areaSettings.y + area.rect.y, area.rect.w, area.rect.h, area.rect.x, 154 + area.rect.y, zLevel);
                    break;
                }
            }
            for (ModeArea area : ModeArea.values())
            {
                if (area.id == mode)
                {
                    drawRectTexture(areaSettings.x + area.rect.x, areaSettings.y + area.rect.y, area.rect.w, area.rect.h, area.rect.x, 154 + area.rect.y, zLevel);
                    break;
                }
            }
            for (ModeArea area : ModeArea.values())
            {
                if (area.rect.isMouseHovering(x, y))
                {
                    drawRectTexture(areaSettings.x + area.rect.x, areaSettings.y + area.rect.y, area.rect.w, area.rect.h, area.rect.x, 154 + area.rect.y, zLevel);
                    break;
                }
            }
            for (LifespanControlArea area : LifespanControlArea.values())
            {
                if (area.rect.isMouseHovering(x, y))
                {
                    drawRectTexture(areaSettings.x + area.rect.x, areaSettings.y + area.rect.y, area.rect.w, area.rect.h, area.rect.x, 154 + area.rect.y, zLevel);
                    break;
                }
            }
            drawString(mc.fontRenderer, String.format("%.1f", (float) lifespan / 20F), areaSettings.x + 33, areaSettings.y + 105, 0xFFFFFF);
        }
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
        areaSettingsButton = new Rect(width - 32, height - 44, 28, 28);
        areaSettings = new Rect(width - 258, height - 171, 254, 126);
    }

    protected void keyTyped(char par1, int par2)
    {
        if (par2 == 1)
        {
            mc.displayGuiScreen(null);
        }
        else if (par2 == 28)
        {
            String s = inputField.getText().trim().replace("\u00a7", "&");
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
        if (areaSettingsButton.isMouseHovering(par1, par2))
        {
            settingsOpened = !settingsOpened;
            mc.sndManager.playSoundFX("random.click", 1.0F, 1.0F);
        }
        if (settingsOpened)
        {
            int x = par1 - areaSettings.x;
            int y = par2 - areaSettings.y;
            for (ControlCodeArea area : ControlCodeArea.values())
            {
                if (area.rect.isMouseHovering(x, y))
                {
                    writeText(area.code);
                    break;
                }
            }
            for (ModeArea area : ModeArea.values())
            {
                if (area.rect.isMouseHovering(x, y))
                {
                    mode = area.id;
                    break;
                }
            }
            for (LifespanControlArea area : LifespanControlArea.values())
            {
                if (area.rect.isMouseHovering(x, y))
                {
                    lifespan += area.m;
                    break;
                }
            }
        }
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

    public void writeText(String par1Str)
    {
        int cursorPosition = inputField.getCursorPosition();
        int selectionEnd = inputField.getSelectionEnd();
        int maxStringLength = inputField.getMaxStringLength();
        String text = inputField.getText();
        String s1 = "";
        String s2 = par1Str;
        int i = cursorPosition < selectionEnd ? cursorPosition : selectionEnd;
        int j = cursorPosition < selectionEnd ? selectionEnd : cursorPosition;
        int k = maxStringLength - text.length() - (i - selectionEnd);
        boolean flag = false;
        if (text.length() > 0)
        {
            s1 = s1 + text.substring(0, i);
        }
        int l;
        if (k < s2.length())
        {
            s1 = s1 + s2.substring(0, k);
            l = k;
        }
        else
        {
            s1 = s1 + s2;
            l = s2.length();
        }
        if (text.length() > 0 && j < text.length())
            s1 = s1 + text.substring(j);
        text = s1;
        inputField.setText(text);
        inputField.moveCursorBy(i - selectionEnd + l);
    }

}
