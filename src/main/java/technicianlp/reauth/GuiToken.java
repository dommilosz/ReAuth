package technicianlp.reauth;

import com.mojang.authlib.exceptions.AuthenticationException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;

final class GuiToken extends GuiScreen {

    private Secure.Account startingAccount;

    private GuiTextField token;
    private GuiButton login;
    private GuiButton cancel;
    private GuiButton showToken;
    private GuiButton copy;

    private GuiScreen successPrevScreen;
    private GuiScreen failPrevScreen;

    private String message = "";
    int basey;

    GuiToken(GuiScreen successPrevScreen, GuiScreen failPrevScreen) {
        this(successPrevScreen, failPrevScreen, null);
    }

    GuiToken(GuiScreen successPrevScreen, GuiScreen failPrevScreen, Secure.Account startingAccount) {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        this.successPrevScreen = successPrevScreen;
        this.failPrevScreen = failPrevScreen;
        this.startingAccount = startingAccount;
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        switch (b.id) {
            case 0:
                if (useToken()){
                    String username = Minecraft.getMinecraft().getSession().getUsername();
                    this.mc.displayGuiScreen(new GuiPopup(successPrevScreen,"Logged in as:\n"+username));
                }
                break;
            case 2:
                showToken();

                break;
            case 3:
                copyToken();

                break;
            case 1:
                this.mc.displayGuiScreen(failPrevScreen);
                break;
        }

    }

    private void copyToken() {
        String token = Minecraft.getMinecraft().getSession().getToken();

        StringSelection stringSelection = new StringSelection(token);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private void showToken() {
        String token = Minecraft.getMinecraft().getSession().getToken();
        this.token.setText(token);
    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRenderer, "Bearer Token:", this.width / 2, this.basey,
                Color.WHITE.getRGB());
        if (!(this.message == null || this.message.isEmpty())) {
            this.drawCenteredString(this.fontRenderer, this.message, this.width / 2, this.basey - 15, 0xFFFFFF);
        }
        this.token.drawTextBox();

        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.token.drawTextBox();
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        this.basey = this.height / 2 - 110 / 2;

        this.token = new GuiTextField(0, this.fontRenderer, this.width / 2 - 155, this.basey + 15, 2 * 155, 20);
        this.token.setMaxStringLength(512);
        this.token.setFocused(true);

        this.login = new GuiButton(0, this.width / 2 - 155, this.basey + 105, 100, 20, "Login");
        this.showToken = new GuiButton(2, this.width / 2 - 50, this.basey + 105, 100, 20, "Generate token");
        this.cancel = new GuiButton(1, this.width / 2 + 55, this.basey + 105, 100, 20, "Cancel");
        this.copy = new GuiButton(3, this.width / 2 - 155+2*155+5, this.basey + 15, 30, 20, "Copy");
        this.buttonList.add(this.login);
        this.buttonList.add(this.cancel);
        this.buttonList.add(this.showToken);
        this.buttonList.add(this.copy);
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        super.keyTyped(c, k);
        this.token.textboxKeyTyped(c, k);
        if (k == Keyboard.KEY_TAB) {
            this.token.setFocused(!this.token.isFocused());
        } else if (k == Keyboard.KEY_RETURN) {
            if (this.token.isFocused()) {
                this.token.setFocused(false);
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException {
        super.mouseClicked(x, y, b);
        this.token.mouseClicked(x, y, b);
    }

    /**
     * used as an interface between this and the secure class
     * <p>
     * returns whether the login was successful
     */
    private boolean useToken() {
        try {
            Secure.token(this.token.getText(), startingAccount!=null?startingAccount.AccUUID:null);
            this.message = (char) 167 + "aLogin successful!";
            return true;
        } catch (AuthenticationException e) {
            this.message = (char) 167 + "4Login failed: " + e.getMessage();
            LiteModReAuth.log.error("Login failed:", e);
            return false;
        } catch (Exception e) {
            this.message = (char) 167 + "4Error: Something went wrong!";
            LiteModReAuth.log.error("Error:", e);
            return false;
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
    }
}
