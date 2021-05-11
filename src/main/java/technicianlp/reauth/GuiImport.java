package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;

import static technicianlp.reauth.HTTPUtils.Base64Decode;
import static technicianlp.reauth.HTTPUtils.Base64Encode;
import static technicianlp.reauth.Utils.alignButtons;
import static technicianlp.reauth.Utils.copyString;

final class GuiImport extends GuiScreen {

    private GuiScreen successPrevScreen;

    private Secure.Account account;
    private GuiTextField accDataField;

    private int basey;

    GuiImport(GuiScreen successPrevScreen) {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        this.successPrevScreen = successPrevScreen;
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        switch (b.id) {
            case 0:mc.displayGuiScreen(successPrevScreen);break;
            case 1: addAccount();break;
        }

    }

    public void addAccount() {
        String str = accDataField.getText();
        String accTxt= (str.split("#").length>1)?str.split("#")[1]:str.split("#")[0];
        Secure.Account a = Secure.Account.serialise( Base64Decode(accTxt));
        if(a!=null){
            Secure.addAccount(a);
            mc.displayGuiScreen(successPrevScreen);
            GuiPopup.DoNormal("Account successfully imported!\n"+a.getUsername()+"\n"+a.getUsername());
        }else {
            GuiPopup.DoNormal("Incorrect Account!");
        }

    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        super.keyTyped(c, k);
        this.accDataField.textboxKeyTyped(c, k);
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        this.drawDefaultBackground();
        accDataField.drawTextBox();

        this.drawCenteredString(fontRenderer,"Account Data: ",this.width / 2,this.basey,Color.white.getRGB());

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
        GuiButton b1,b2;
        addButton( b1 = new GuiButton(0, this.width / 2 - 50, this.basey + 105, 100, 20, "CLOSE"));
        addButton( b2 = new GuiButton(1, this.width / 2 - 125+255,this.basey + 105, 30, 20, "IMPORT"));
        alignButtons(this.width,b1,b2);

        accDataField = new GuiTextField(-1,fontRenderer,this.width / 2 - 125,this.basey+25,250,20);
        accDataField.setMaxStringLength(16384);
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException {
        this.accDataField.mouseClicked(x, y, b);
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
