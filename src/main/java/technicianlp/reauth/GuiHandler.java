package technicianlp.reauth;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;

import java.awt.Color;

public final class GuiHandler {

    /**
     * Cache the Status for 5 Minutes
     */
    private static final CachedProperty<ValidationStatus> status = new CachedProperty<>(1000 * 60 * 5, ValidationStatus.Unknown);
    private static Thread validator;

    static boolean enabled = true;
    static boolean bold = true;

    public static void openGuiMultiplayer(GuiMultiplayer gui) {
        ((IGuiScreen) gui).doAddButton(new GuiButton(17325, 5, 5, 100, 20, "Re-Login"));

        if (enabled && !status.check()) {
            if (validator != null)
                validator.interrupt();
            validator = new Thread(() -> status.set(Secure.SessionValid() ? ValidationStatus.Valid : ValidationStatus.Invalid), "Session-Validator");
            validator.setDaemon(true);
            validator.start();
        }
    }
    
    public static void openGuiMainMenu(GuiMainMenu gui) {
    	// Support for Custom Main Menu (add button outside of viewport)
        ((IGuiScreen) gui).doAddButton(new GuiButton(17325, -50, -50, 20, 20, "ReAuth"));
    }

    public static void onGuiMultiplayerDrawScreen(GuiMultiplayer gui) {
        if (enabled) {
            gui.drawString(Minecraft.getMinecraft().fontRenderer, "Online:", 110, 10, 0xFFFFFFFF);
            ValidationStatus state = status.get();
            gui.drawString(Minecraft.getMinecraft().fontRenderer, (bold ? ChatFormatting.BOLD : "") + state.text, 145, 10, state.color);
            gui.drawString(Minecraft.getMinecraft().fontRenderer, "("+Minecraft.getMinecraft().getSession().getUsername()+")", 160, 10, 0xFFFFFFFF);
        }
    }

    public static void onActionPerformed(int buttonId) {
        if (buttonId == 17325) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiAccountList(Minecraft.getMinecraft().currentScreen));
        }
    }

    public static void preGuiMultiplayerActionPerformed(int buttonId) {
        if (enabled && buttonId == 8 && GuiScreen.isShiftKeyDown()) {
            status.invalidate();
        }
    }

    static void invalidateStatus() {
        status.invalidate();
    }

    enum ValidationStatus {
        Unknown("?", Color.GRAY.getRGB()), Valid("\u2714", Color.GREEN.getRGB()), Invalid("\u2718", Color.RED.getRGB());

        public final String text;
        public final int color;

        ValidationStatus(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }

}
