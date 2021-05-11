package technicianlp.reauth;

import com.mojang.authlib.exceptions.AuthenticationException;
import jdk.nashorn.internal.runtime.regexp.RegExp;
import jdk.nashorn.internal.runtime.regexp.joni.Regex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.Base64;

import static technicianlp.reauth.Utils.alignButtons;

final class GuiPopup extends GuiScreen {

    private String yesTxt;
    private String noTxt;
    private String aTxt;
    private String bTxt;
    private String cTxt;


    private GuiScreen successPrevScreen;
    private GuiButton okBtn;

    public static boolean response = false;
    public static int response3Optn = -1;
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
        type =  Type.TypeOk;
        response3Optn = -1;
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
        type =  Type.TypeYesNo;
        response3Optn = -1;
    }

    public GuiPopup(GuiScreen successPrevScreen, String msg, String a, String b, String c) {
        complete = false;
        response = false;
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        aTxt = a;
        bTxt = b;
        cTxt = c;
        this.message = msg;
        this.successPrevScreen = successPrevScreen;
        type = Type.Type3Optn;
        response3Optn = -1;
    }

    public static void DoYesNo(String msg, String yes, String no) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayGuiScreen(new GuiPopup(mc.currentScreen, msg, yes, no));
    }
    public static void DoNormal(String msg) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayGuiScreen(new GuiPopup(mc.currentScreen, msg));
    }
    public static void Do3Options(String msg, String a, String b,String c) {
        Minecraft mc = Minecraft.getMinecraft();
        mc.displayGuiScreen(new GuiPopup(mc.currentScreen, msg, a, b,c));
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
    public static void WaitForUserAction(Runnable callback) {
        Thread t = new Thread(() -> {
            WaitForUserAction();
            callback.run();
        });
        t.start();
    }


    @Override
    protected void actionPerformed(GuiButton b) {
        switch (b.id) {
            case 0:
            case 1: {
                this.mc.displayGuiScreen(successPrevScreen);
                complete = true;
                response = true;
                response3Optn = 0;
                break;
            }
            case 2: {
                this.mc.displayGuiScreen(successPrevScreen);
                complete = true;
                response = false;
                response3Optn = 1;
                break;
            }
            case 3: {
                this.mc.displayGuiScreen(successPrevScreen);
                complete = true;
                response3Optn = 2;
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
        if(type.equals(Type.TypeOk)){
            this.okBtn = new GuiButton(0, this.width / 2 - 50, this.basey + 105, 100, 20, "OK");
            this.buttonList.add(this.okBtn);
        }if(type.equals(Type.TypeYesNo)) {
            GuiButton b1 = new GuiButton(1, this.width / 2 - 50-53, this.basey + 105, 100, 20, yesTxt);
            GuiButton b2 = new GuiButton(2, this.width / 2 - 50+53, this.basey + 105, 100, 20, noTxt);
            alignButtons(this.width,b1,b2);
            addButton(b1);
            addButton(b2);


        }if(type.equals(Type.Type3Optn)) {
            GuiButton b1 = (new GuiButton(1, this.width / 2 - 156, this.basey + 105, 100, 20, aTxt));
            GuiButton b2 =(new GuiButton(2, this.width / 2 - 53, this.basey + 105, 100, 20, bTxt));
            GuiButton b3 =(new GuiButton(3, this.width / 2 + 50, this.basey + 105, 100, 20, cTxt));

            alignButtons(this.width,b1,b2,b3);
            addButton(b1);
            addButton(b2);
            addButton(b3);
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

    public static class Type {
        public static String Type3Optn = "3optn";
        public static String TypeYesNo = "yesno";
        public static String TypeOk = "popup";
    }
}
