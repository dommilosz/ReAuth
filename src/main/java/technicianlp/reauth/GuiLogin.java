package technicianlp.reauth;

import java.awt.Color;
import java.io.IOException;

import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;

import com.mojang.authlib.exceptions.AuthenticationException;
import com.mumfrey.liteloader.client.gui.GuiCheckbox;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

final class GuiLogin extends GuiScreen {

    private Secure.Account startingAccount;

    private GuiTextField username;
    private GuiPasswordField pw;
    private GuiButton login;
    private GuiButton cancel;
    private GuiButton offline;
    private GuiCheckbox save;

    private GuiScreen successPrevScreen;
    private GuiScreen failPrevScreen;

    private int basey;

    private String message = "";

    GuiLogin(GuiScreen successPrevScreen, GuiScreen failPrevScreen) {
        this(successPrevScreen, failPrevScreen, null);
    }

    GuiLogin(GuiScreen successPrevScreen, GuiScreen failPrevScreen, Secure.Account startingAccount) {
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
                if (login()) {
                    this.mc.displayGuiScreen(successPrevScreen);
                }

                break;
            case 3:
                if (playOffline()) {
                    this.mc.displayGuiScreen(successPrevScreen);
                }


                break;
            case 1:
                this.mc.displayGuiScreen(failPrevScreen);
                break;
            case 2:
                this.save.checked = !this.save.checked;
                break;
            case 4:
                Minecraft.getMinecraft().displayGuiScreen(new GuiToken(successPrevScreen, Minecraft.getMinecraft().currentScreen));
                break;
            case 5: {
                GuiMicrosoft gM = new GuiMicrosoft(successPrevScreen);
                Minecraft.getMinecraft().displayGuiScreen(gM);
                gM.safeAuthFlow();
                break;
            }
            case 6:
                mc.displayGuiScreen(new GuiImport(successPrevScreen));
                break;
        }

    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        this.drawDefaultBackground();

        this.drawCenteredString(this.fontRenderer, "Username/E-Mail:", this.width / 2, this.basey,
                Color.WHITE.getRGB());
        this.drawCenteredString(this.fontRenderer, "Password:", this.width / 2, this.basey + 45,
                Color.WHITE.getRGB());
        if (!(this.message == null || this.message.isEmpty())) {
            this.drawCenteredString(this.fontRenderer, this.message, this.width / 2, this.basey - 15, 0xFFFFFF);
        }
        this.username.drawTextBox();
        this.pw.drawTextBox();

        super.drawScreen(p_73863_1_, p_73863_2_, p_73863_3_);
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.username.drawTextBox();
        this.pw.drawTextBox();
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);

        this.basey = this.height / 2 - 110 / 2;

        this.username = new GuiTextField(0, this.fontRenderer, this.width / 2 - 155, this.basey + 15, 2 * 155, 20);
        this.username.setMaxStringLength(512);
        if (startingAccount != null)
            this.username.setText(startingAccount.getUsername());
        this.username.setFocused(true);

        this.pw = new GuiPasswordField(this.fontRenderer, this.width / 2 - 155, this.basey + 60, 2 * 155, 20);
        if (startingAccount != null)
            this.pw.setPassword(startingAccount.getPassword());

        this.save = new GuiCheckbox(2, this.width / 2 - 155, this.basey + 85, "Save Password to Config (WARNING: SECURITY RISK!)");
        this.buttonList.add(this.save);

        this.login = new GuiButton(0, this.width / 2 - 155, this.basey + 105, 100, 20, "Login");
        this.offline = new GuiButton(3, this.width / 2 - 50, this.basey + 105, 100, 20, "Play Offline");
        this.cancel = new GuiButton(1, this.width / 2 + 55, this.basey + 105, 100, 20, "Cancel");
        addButton(new GuiImageButton(5, this.width - 50, this.height - 25, 20, 20, "", new ResourceLocation("reauth", "textures/microsoft.png")));
        addButton(new GuiImageButton(4, this.width - 25, this.height - 25, 20, 20, "", new ResourceLocation("reauth", "textures/key.png")));
        addButton(new GuiImageButton(6, this.width - 75, this.height - 25, 20, 20, "", new ResourceLocation("reauth", "textures/import.png")));
        this.buttonList.add(this.login);
        this.buttonList.add(this.cancel);
        this.buttonList.add(this.offline);
    }

    @Override
    protected void keyTyped(char c, int k) throws IOException {
        super.keyTyped(c, k);
        this.username.textboxKeyTyped(c, k);
        this.pw.textboxKeyTyped(c, k);
        if (k == Keyboard.KEY_TAB) {
            this.username.setFocused(!this.username.isFocused());
            this.pw.setFocused(!this.pw.isFocused());
        } else if (k == Keyboard.KEY_RETURN) {
            if (this.username.isFocused()) {
                this.username.setFocused(false);
                this.pw.setFocused(true);
            } else if (this.pw.isFocused()) {
                this.actionPerformed(this.login);
            }
        }
    }

    @Override
    protected void mouseClicked(int x, int y, int b) throws IOException {
        super.mouseClicked(x, y, b);
        this.username.mouseClicked(x, y, b);
        this.pw.mouseClicked(x, y, b);
    }

    /**
     * used as an interface between this and the secure class
     * <p>
     * returns whether the login was successful
     */
    private boolean login() {
        try {
            if (startingAccount != null) {
                Secure.login(this.username.getText(), this.pw.getPW(), this.save.checked, startingAccount.getIndex());
            } else {
                Secure.login(this.username.getText(), this.pw.getPW(), this.save.checked, -1);
            }
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

    /**
     * sets the name for playing offline
     */
    private boolean playOffline() {
        String username = this.username.getText();
        if (!(username.length() >= 2 && username.length() <= 16)) {
            this.message = (char) 167 + "4Error: Username needs a length between 2 and 16";
            return false;
        }
        if (!username.matches("[A-Za-z0-9_]{2,16}")) {
            this.message = (char) 167 + "4Error: Username has to be alphanumerical";
            return false;
        }
        try {
            if (startingAccount != null) {
                Secure.offlineMode(username, startingAccount.getIndex());
            } else {
                Secure.offlineMode(username, -1);
            }

            return true;
        } catch (Exception e) {
            this.message = (char) 167 + "4Error: Something went wrong!";
            LiteModReAuth.log.error("Error:", e);
            return false;
        }
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        this.pw.setPassword(new char[0]);
        Keyboard.enableRepeatEvents(false);
    }
}
