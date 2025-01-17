package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.net.URI;

import static technicianlp.reauth.MSAuth.*;
import static technicianlp.reauth.Utils.alignButtons;
import static technicianlp.reauth.Utils.copyString;

final class GuiMicrosoft extends GuiScreen {

    private Secure.Account startingAccount;

    private GuiScreen successPrevScreen;
    private GuiButton okBtn;
    private GuiButton openBtn;
    private GuiButton copyBtn;

    private String message = "";
    private boolean exception = false;
    int basey;

    GuiMicrosoft(GuiScreen successPrevScreen) {
        this(successPrevScreen, null);
    }

    GuiMicrosoft(GuiScreen successPrevScreen, Secure.Account startingAccount) {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        this.successPrevScreen = successPrevScreen;
        this.message = "Waiting for user input - redirect";
        this.startingAccount = startingAccount;
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        String API_Url = "https://login.live.com/oauth20_authorize.srf";
        String url = API_Url + "?" + "client_id=" + appID + "&response_type=code&redirect_uri=" + encodeValue(redirectURL) + "&scope=" + encodeValue(scope);

        switch (b.id) {
            case 0:
                cancelAuth();
                break;
            case 1:
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    try {
                        Desktop.getDesktop().browse(new URI(url));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                copyString(url);
                break;
        }

    }

    public boolean cancel = false;
    public boolean awaitingBrowserAuth = false;

    public void authFlow() throws IOException, InterruptedException {
        message = "Waiting for user input - redirect";
        awaitingBrowserAuth = true;
        runBrowserAuth();
        String code = "";
        while (InternalHTTPServer.codeExport.equals("")) {
            code = InternalHTTPServer.codeExport;
            if (cancel) {
                return;
            }
            if(InternalHTTPServer.server == null){
                message = "Internal server ended before gathering code";
                cancelAuth();
            }
            Thread.sleep(500);
        }
        awaitingBrowserAuth = false;
        code = InternalHTTPServer.codeExport;
        if (cancel) {
            return;
        }
        message = "Making POST Request to get auth token";
        MSAuth.AuthTokenResponse atr;
        if (code.contains("code=")) {
            atr = MSAuth.getAuthToken(code.split("code=")[1]);
        } else throw new IOException("Authentication Error");
        if (atr.error != null || atr.access_token == null) {
            throw new IOException("Authentication Error");
        }
        if (cancel) {
            return;
        }
        authFlowFromATR(atr);
    }
    timeMeasureUtil tms;
    public void authFlowFromATR(AuthTokenResponse atr) throws IOException, InterruptedException {
        message = "Using ATR to login with XBL";
        if (atr.error != null || atr.access_token == null) {
            throw new IOException("Authentication Error");
        }
        tms = new timeMeasureUtil();
        if (cancel) {
            return;
        }
        message = "Authenticating with XBL";
        MSAuth.XBLAuthResponse xbl = AuthXBL(atr);
        if (cancel) {
            return;
        }
        message = "Authenticating with XSTS";
        MSAuth.XBLAuthResponse xsts = AuthXSTS(xbl);
        if (cancel) {
            return;
        }
        message = "Authenticating with Minecraft";
        MSAuth.MCAuthResponse mcauth = MSAuth.AuthMC(xsts);
        if (cancel) {
            return;
        }
        message = "Checking Game Ownership";
        if(!MCApi.checkGameOwnership(mcauth.access_token)){
            message = "You don't have the game on your Microsoft Account";
            cancelAuth();
        }
        if (cancel) {
            return;
        }
        message = "Using Token";
        Secure.Account a = Secure.microsoft(mcauth.access_token, atr.refresh_token, startingAccount!=null? startingAccount.getIndex() : null);
        message = "DONE";
        Minecraft.getMinecraft().displayGuiScreen(new GuiPopup(successPrevScreen, "Logged in as:\n" + a.getDisplayName()+"\n"+"Time Took: "+tms.getMeasureFormated()));
    }

    public void safeAuthFlow() {
        safeAuthAny(()->{
            try{
                authFlow();
                if (cancel) {
                    message += "\n" + "Cancelled";
                    Minecraft.getMinecraft().displayGuiScreen(new GuiPopup(successPrevScreen, message));
                }
            }catch (Exception ex){
                handleError(ex);
            }
        });
    }

    public void safeAuthAny(Runnable r){
        try{
            new Thread(r).start();
        }catch (Exception ex){
            message += "\n" + ex.getMessage();
            cancelAuth();
            exception = true;

            Minecraft.getMinecraft().displayGuiScreen(new GuiPopup(successPrevScreen, message));
        }
    }

    public void safeAuthFlow(AuthTokenResponse atr) {
        safeAuthAny(()->{
            try{
                authFlowFromATR(atr);
                if (cancel) {
                    message += "\n" + "Cancelled";
                    Minecraft.getMinecraft().displayGuiScreen(new GuiPopup(successPrevScreen, message));
                }
            }catch (Exception ex){
                handleError(ex);
            }
        });
    }
    public void handleError(Exception ex){
        message += "\n" + ex.getMessage();
        exception = true;
        cancelAuth();
        GuiPopup.DoYesNo(message,"OK","ReAuth");

        GuiPopup.WaitForUserAction();

        if(GuiPopup.response){
            mc.displayGuiScreen(successPrevScreen);
        }else {
            GuiMicrosoft gM = new GuiMicrosoft(successPrevScreen);
            Minecraft.getMinecraft().displayGuiScreen(gM);
            gM.safeAuthFlow();
        }
    }

    public void cancelAuth() {
        if(exception){
            mc.displayGuiScreen(successPrevScreen);
            return;
        }
        cancel = true;
        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)&&Keyboard.isKeyDown(Keyboard.KEY_LCONTROL)){
            message += "\n" + "Force Cancelled";
            Minecraft.getMinecraft().displayGuiScreen(new GuiPopup(successPrevScreen, message));

            return;
        }
        if(InternalHTTPServer.server!=null)
        InternalHTTPServer.server.stop(0);
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
        if(tms!=null)
        this.drawCenteredString(this.fontRenderer, "Time: "+tms.getMeasureFormated(), this.width / 2, this.basey - 20,
                Color.WHITE.getRGB());

        this.drawString(this.fontRenderer, "Tip: Hold Ctrl+Shift and click Cancel to force it.", 5, this.height-15,
                0x777777);

        this.buttonList.removeIf(btn -> btn.id == 1 || btn.id == 2 || btn.id==3);
        if (awaitingBrowserAuth) {
            this.openBtn = new GuiButton(1, this.width / 2 - 50 + 53, this.basey + 55, 100, 20, "Open Browser");
            this.buttonList.add(this.openBtn);
            this.copyBtn = new GuiButton(2, this.width / 2 - 50 - 53, this.basey + 55, 100, 20, "Copy URL");
            this.buttonList.add(this.copyBtn);
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

        this.okBtn = new GuiButton(0, this.width / 2 - 50, this.basey + 105, 100, 20, "Cancel");
        this.buttonList.add(this.okBtn);

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
    }

    @Override
    protected void keyTyped(char p_keyTyped_1_, int p_keyTyped_2_) throws IOException {

    }
}
