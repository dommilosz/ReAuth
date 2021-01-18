package technicianlp.reauth;

import com.mojang.authlib.exceptions.AuthenticationException;
import jdk.nashorn.internal.runtime.regexp.RegExp;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.Base64;

final class GuiPopup extends GuiScreen {

    private String yesTxt;
    private String noTxt;
    private GuiScreen successPrevScreen;
    private GuiButton okBtn;

    public static boolean response = false;
    public static boolean complete = false;
    public static String type = "popup";

    private String message = "";
    int basey;

    GuiPopup(GuiScreen successPrevScreen, String msg) {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        this.successPrevScreen = successPrevScreen;
        this.message = msg;
        this.yesTxt = null;
        this.noTxt = null;
        complete = false;
        response = false;
        type = "popup";
    }

    GuiPopup(GuiScreen successPrevScreen, String msg, String yes, String no) {
        complete = false;
        response = false;
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        this.successPrevScreen = successPrevScreen;
        this.message = msg;
        this.yesTxt = yes;
        this.noTxt = no;
        type = "yesno";
    }

    public static void DoYesNo(String msg, String yes, String no) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayGuiScreen(new GuiPopup(mc.currentScreen, msg, yes, no));
    }

    public static void WaitForUserAction() {
        while (!complete) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    protected void actionPerformed(GuiButton b) {
        switch (b.id) {
            case 0:
            case 1: {
                this.mc.displayGuiScreen(successPrevScreen);
                complete = true;
                response = true;
                break;
            }
            case 2: {
                this.mc.displayGuiScreen(successPrevScreen);
                complete = true;
                response = false;
                break;
            }
        }

    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        this.drawDefaultBackground();
        String[] lines = message.split("\\n");
        int offset = 0;
        for (String line : lines) {
            this.drawCenteredString(this.fontRenderer, line, this.width / 2, this.basey + offset,
                    Color.WHITE.getRGB());
            offset += 20;
        }


        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        this.basey = this.height / 2 - 110 / 2;
        if(yesTxt==null&&noTxt==null){
            this.okBtn = new GuiButton(0, this.width / 2 - 50, this.basey + 105, 100, 20, "OK");
            this.buttonList.add(this.okBtn);
        }else {
            addButton(new GuiButton(1, this.width / 2 - 50-53, this.basey + 105, 100, 20, yesTxt));
            addButton(new GuiButton(2, this.width / 2 - 50+53, this.basey + 105, 100, 20, noTxt));

        }

    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException {
        super.mouseClicked(x, y, b);
    }

    /**
     * used as an interface between this and the secure class
     * <p>
     * returns whether the login was successful
     */

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        complete = true;
    }
}
