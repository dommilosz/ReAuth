package technicianlp.reauth;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

final class GuiAccInfo extends GuiScreen {

    private GuiScreen successPrevScreen;
    private GuiButton cloeseBtn;

    private Secure.Account account;
    private int basey;

    GuiAccInfo(GuiScreen successPrevScreen, Secure.Account acc) {
        this.mc = Minecraft.getMinecraft();
        this.fontRenderer = mc.fontRenderer;
        this.successPrevScreen = successPrevScreen;
        account = acc;
    }

    @Override
    protected void actionPerformed(GuiButton b) {
        switch (b.id) {
            case 0:mc.displayGuiScreen(successPrevScreen);break;
            case 1:
                mc.displayGuiScreen(new GuiExport(this, account));
                break;
        }

    }

    @Override
    public void drawScreen(int p_73863_1_, int p_73863_2_, float p_73863_3_) {
        this.drawDefaultBackground();
        ArrayList<String[]> props = new ArrayList();

        props.add(new String[] {"Username",account.getUsername()});
        props.add(new String[] {"DisplayName",account.getDisplayName()});
        props.add(new String[] {"UUID",account.getUuid().toString()});
        props.add(new String[] {"Type",account.accountType});
        props.add(new String[] {"LastQuery", String.valueOf(account.getLastQuery())});

        AtomicInteger offset = new AtomicInteger();
        for(String[] prop:props){
            this.drawCenteredString(this.fontRenderer, prop[0], this.width / 2-100, this.basey + offset.get(),
                    Color.WHITE.getRGB());
            this.drawCenteredString(this.fontRenderer, prop[1], this.width / 2+100, this.basey + offset.get(),
                    Color.WHITE.getRGB());
            offset.addAndGet(20);
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

        addButton(new GuiButton(0, this.width / 2 - 50-53, this.basey + 105, 100, 20, "CLOSE"));
        addButton(new GuiButton(1, this.width / 2 - 50+53, this.basey + 105, 100, 20, "EXPORT"));

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
}
