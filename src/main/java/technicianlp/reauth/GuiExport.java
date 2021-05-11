package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import static technicianlp.reauth.HTTPUtils.Base64Encode;
import static technicianlp.reauth.Utils.copyString;

final class GuiExport extends GuiScreen {

    private GuiScreen successPrevScreen;

    private Secure.Account account;
    private GuiTextField tokenField;
    private GuiTextField accDataField;

    private int basey;

    GuiExport(GuiScreen successPrevScreen, Secure.Account acc) {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        this.successPrevScreen = successPrevScreen;
        account = acc;
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        switch (b.id) {
            case 0:mc.displayGuiScreen(successPrevScreen);break;
            case 1: copyString(tokenField.getText());break;
            case 2: copyString(accDataField.getText());break;
        }

    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        super.keyTyped(c, k);
        this.accDataField.textboxKeyTyped(c, k);
        this.tokenField.textboxKeyTyped(c, k);
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        this.drawDefaultBackground();
        tokenField.drawTextBox();
        accDataField.drawTextBox();

        this.drawString(fontRenderer,"Bearer Token: ",this.width / 2 - 125-100+25,this.basey+7,Color.white.getRGB());
        this.drawString(fontRenderer,"Account Data: ",this.width / 2 - 125-100+25,this.basey+25+7,Color.white.getRGB());

        this.drawCenteredString(fontRenderer, "Account: "+ account.getUsername(),this.width / 2,this.basey-40,Color.white.getRGB());

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

        addButton(new GuiButton(0, this.width / 2 - 50, this.basey + 105, 100, 20, "CLOSE"));
        addButton(new GuiButton(1, this.width / 2 - 125+255,this.basey, 30, 20, "COPY"));
        addButton(new GuiButton(2, this.width / 2 - 125+255,this.basey+25, 30, 20, "COPY"));

        tokenField = new GuiReadOnlyTextField(-1,fontRenderer,this.width / 2 - 125,this.basey,250,20);
        accDataField = new GuiReadOnlyTextField(-1,fontRenderer,this.width / 2 - 125,this.basey+25,250,20);

        tokenField.setMaxStringLength(512);
        accDataField.setMaxStringLength(16384);

        tokenField.setText(account.Token!=null?account.Token:"");
        accDataField.setText(account.getUsername().replaceAll("#",".")+"#"+Base64Encode(account.deserialise()));
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException {
        this.accDataField.mouseClicked(x, y, b);
        this.tokenField.mouseClicked(x, y, b);
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
    }
}
