package technicianlp.reauth;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.ResourceLocation;

import static technicianlp.reauth.GuiHandler.bold;
import static technicianlp.reauth.Secure.*;
import static technicianlp.reauth.Utils.alignButtons;

public class GuiAccountList extends GuiScreen {

    private final static int BUTTON_WIDTH = 308;

    private GuiScreen parentScreen;

    private GuiButton loginButton;
    private GuiButton cancelButton;
    private GuiButton addButton;
    private GuiButton editButton;
    private GuiButton removeButton;
    private GuiButton reloadButton;

    private Secure.Account selectedAccount = null;
    private GuiSlotAccounts accountList;

    public GuiAccountList(GuiScreen parentScreen) {
        this.parentScreen = parentScreen;
    }

    @Override
    public void initGui() {
        super.initGui();

        int start = width / 2 - BUTTON_WIDTH / 2;
        int halfWidth = (BUTTON_WIDTH / 2 - 4);
        int thirdWidth = (BUTTON_WIDTH / 3 - 4);
        GuiButton detailsButton;
        addButton(loginButton = new GuiButton(0, 0, height - 27 - 25, thirdWidth, 20, "Login"));
        addButton(addButton = new GuiButton(1, 0, height - 27 - 25, thirdWidth, 20, "Add Account"));
        addButton(editButton = new GuiButton(2, 0, height - 27, thirdWidth, 20, "Edit account"));
        addButton(removeButton = new GuiButton(3, 0, height - 27, thirdWidth, 20, "Remove account"));
        addButton(cancelButton = new GuiButton(4, 0, height - 27, thirdWidth, 20, I18n.format("gui.cancel")));
        addButton(reloadButton = new GuiButton(5, 5, 5, 100, 20, "Reload Accounts"));
        addButton(detailsButton = new GuiButton(6, 0, height - 27 - 25, thirdWidth, 20, "Details"));


        alignButtons(this.width, loginButton, detailsButton, addButton);
        alignButtons(this.width, editButton, removeButton, cancelButton);
        if (Secure.accounts.isEmpty()) {
            loginButton.enabled = false;
            editButton.enabled = false;
            removeButton.enabled = false;
        } else {
            selectedAccount = getCurrentAccount();
        }

        accountList = new GuiSlotAccounts(mc, width, height, 50, height - 60, 38);

        Secure.initSkinStuff();
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        accountList.handleMouseInput();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        accountList.drawScreen(mouseX, mouseY, partialTicks);
        super.drawScreen(mouseX, mouseY, partialTicks);

        drawCenteredString(fontRenderer, "Account List", width / 2, 10, 0xffffff);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                try {
                    selectedAccount.login();
                } catch (Exception e) {
                    mc.displayGuiScreen(new GuiErrorScreen("ReAuth", "Authentication Failed"));
                }
                break;
            case 1:
                mc.displayGuiScreen(new GuiLogin(parentScreen, this));
                break;
            case 3: {
                if (selectedAccount == null)return;
                Secure.Account selAcc = selectedAccount;
                GuiPopup.DoYesNo("Do you want to remove\n" + selAcc.getUsername() + "\n" + selAcc.getDisplayName() + "\nTYPE: " + selAcc.accountType, "Delete", "Cancel");
                GuiPopup.WaitForUserAction(new Runnable() {
                    @Override
                    public void run() {
                        if (!GuiPopup.response) return;
                        Secure.accounts.remove(selAcc);
                        if (Secure.accounts.isEmpty())
                            mc.displayGuiScreen(parentScreen);
                        else
                            selectedAccount = getCurrentAccount();
                        LiteModReAuth.saveConfig();
                    }
                });
                break;
            }
            case 2: {
                if (selectedAccount != null)
                    mc.displayGuiScreen(new GuiLogin(this, this, selectedAccount));
                break;
            }
            case 4:
                mc.displayGuiScreen(parentScreen);
                break;
            case 5:
                LiteModReAuth.loadConfigDefault();
                break;
            case 6: {
                if (selectedAccount != null)
                    mc.displayGuiScreen(new GuiAccInfo(this, selectedAccount));
                break;
            }

        }
    }

    private class GuiSlotAccounts extends GuiSlot {

        public final ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");
        public int activeActionButton = -1;

        public GuiSlotAccounts(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
            super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
        }

        @Override
        protected int getSize() {
            return Secure.accounts.size();
        }

        public long lastClicked;

        @Override
        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            long diffclicked = System.currentTimeMillis() - lastClicked;
            lastClicked = System.currentTimeMillis();
            if (diffclicked < 50) return;
            int i = 0;
            for (Secure.Account account : Secure.accounts) {
                if (account.getIndex() == slotIndex) {
                    selectedAccount = account;
                    break;
                }
                i++;
            }
            if (activeActionButton == 0) {
                GuiAccountList.this.actionPerformed(loginButton);
                lastClicked += 10000;
            }
            if (activeActionButton == 1) {
                moveUp(slotIndex);
            }
            if (activeActionButton == 2) {
                moveDown(slotIndex);
            }

            if (isDoubleClick && activeActionButton == -1) {
                GuiAccountList.this.actionPerformed(loginButton);
            }
        }

        @Override
        protected boolean isSelected(int slotIndex) {
            try {
                if (!Secure.accounts.contains(selectedAccount))
                    selectedAccount = getCurrentAccount();
                if (selectedAccount == null) return false;
                int i = 0;
                for (Secure.Account account : Secure.accounts) {
                    if (account.getIndex() == slotIndex)
                        return selectedAccount.equals(account);
                    i++;
                }
                return false;
            } catch (Exception ex) {
                return false;
            }
        }

        @Override
        protected void drawBackground() {
            drawDefaultBackground();
        }

        public Map<String, ResourceLocation> skins = new HashMap<>();

        public boolean isMouseInBoundsSlot(int index, int mouseX, int mouseY) {
            return (getSlotIndexFromScreenCoords(mouseX, mouseY) == index);
        }

        @Override
        protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn,
                                float partialTicks) {
            Account account = accounts.get(slotIndex);

            if (account == null) {
                return;
            }

            String accTypeFileName = "textures/no_texture.png";
            switch (account.accountType) {
                case "mojang":
                    accTypeFileName = "textures/mojang.png";
                    break;
                case "microsoft":
                    accTypeFileName = "textures/microsoft.png";
                    break;
                case "token":
                    accTypeFileName = "textures/key_yellow.png";
                    break;
                case "offline":
                    accTypeFileName = "textures/grass_side.png";
                    break;
                case "custom":
                    accTypeFileName = "textures/custom_yellow.png";
                    break;
            }
            ResourceLocation accTypeIcon = new ResourceLocation("reauth", accTypeFileName);
            mc.getTextureManager().bindTexture(accTypeIcon);
            GuiImageButton.drawModalRectWithCustomSizedTexture(xPos + 182, yPos, 0, 0, 32, 32, 32, 32);

            drawString(fontRenderer, account.getDisplayName(), xPos + 50, yPos + 7, UUIDFromShortString(mc.getSession().getPlayerID()).toString().equals(account.getUuid().toString()) ? Color.blue.getRGB() : 0xffffff);
            drawString(fontRenderer, account.getUsername(), xPos + 50, yPos + 19, 0x777777);

            GameProfile gameProfile = new GameProfile(account.getUuid(), account.getDisplayName());
            if (account.getLastQuery() + 10 * 60 * 1000 < System.currentTimeMillis() || !skins.containsKey(account.getUuid().toString())) {
                if (!gameProfile.getProperties().containsKey("textures") || !gameProfile.isComplete()) {
                    gameProfile = TileEntitySkull.updateGameprofile(gameProfile);
                    if (account.getUuid() == null) {
                        account.setUuid(gameProfile.getId());
                        LiteModReAuth.saveConfig();
                    }
                    account.setLastQuery(System.currentTimeMillis());
                }
            }
            Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> profileTextures = Minecraft.getMinecraft()
                    .getSkinManager().loadSkinFromCache(gameProfile);
            ResourceLocation skinLocation;
            if (profileTextures.containsKey(MinecraftProfileTexture.Type.SKIN)) {
                skinLocation = Minecraft.getMinecraft().getSkinManager().loadSkin(
                        profileTextures.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
                skins.put(account.getUuid().toString(), skinLocation);
            } else if (skins.containsKey(account.getUuid().toString())) {
                skinLocation = skins.get(account.getUuid().toString());
            } else {
                UUID id = EntityPlayer.getUUID(gameProfile);
                skinLocation = DefaultPlayerSkin.getDefaultSkin(id);
                skins.put(account.getUuid().toString(), skinLocation);
            }

            Minecraft.getMinecraft().getTextureManager().bindTexture(skinLocation);
            ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");

            drawScaledCustomSizeModalRect(xPos + 1, yPos + 1, 8, 8, 8, 8, 32, 32, 64, 64);
            if (this.mc.gameSettings.touchscreen || isMouseInBoundsSlot(slotIndex, mouseXIn, mouseYIn)) {
                this.mc.getTextureManager().bindTexture(SERVER_SELECTION_BUTTONS);
                Gui.drawRect(xPos, yPos, xPos + 32 + 2, yPos + 32 + 2, -1601138544);
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                int lvt_22_1_ = mouseXIn - xPos;
                int lvt_23_1_ = mouseYIn - yPos;
                activeActionButton = -1;
                if (lvt_22_1_ < 32 && lvt_22_1_ > 16) {
                    activeActionButton = 0;
                    Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);

                }

                if (slotIndex > 0) {
                    if (lvt_22_1_ < 16 && lvt_23_1_ < 16) {
                        activeActionButton = 1;
                        Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                    } else {
                        Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);

                    }

                }

                if (slotIndex < Secure.accounts.size() - 1) {
                    if (lvt_22_1_ < 16 && lvt_23_1_ > 16) {
                        activeActionButton = 2;
                        Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                    } else {
                        Gui.drawModalRectWithCustomSizedTexture(xPos, yPos, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);

                    }

                }
            }
        }

    }

    private void moveDown(int slotIndex) {
        try {
            if (slotIndex >= Secure.accounts.size() - 1) return;
            Collections.swap(accounts, slotIndex, slotIndex + 1);
            LiteModReAuth.saveConfig();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void moveUp(int slotIndex) {
        try {
            if (slotIndex <= 0) return;
            Collections.swap(accounts, slotIndex, slotIndex - 1);
            LiteModReAuth.saveConfig();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void drawTextureAt(int p_drawTextureAt_1_, int p_drawTextureAt_2_, ResourceLocation p_drawTextureAt_3_) {
        this.mc.getTextureManager().bindTexture(p_drawTextureAt_3_);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(p_drawTextureAt_1_, p_drawTextureAt_2_, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GlStateManager.disableBlend();
    }

    public boolean isMouseInBounds(int x, int y, int w, int h, int mX, int mY) {
        if (mX < x) return false;
        if (mY < y) return false;
        if (mX > x + w) return false;
        return mY <= y + h;
    }
}
