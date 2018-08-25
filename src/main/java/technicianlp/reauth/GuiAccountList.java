package technicianlp.reauth;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiErrorScreen;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class GuiAccountList extends GuiScreen {

	private GuiScreen parentScreen;

	private GuiButton loginButton;
	private GuiButton cancelButton;
	private GuiButton addButton;
	private GuiButton editButton;
	private GuiButton removeButton;

	private String selectedAccount = "";
	private GuiSlotAccounts accountList;

	public GuiAccountList(GuiScreen parentScreen) {
		this.parentScreen = parentScreen;
	}

	@Override
	public void initGui() {
		super.initGui();

		addButton(loginButton = new GuiButton(0, 10, height - 50, width / 2 - 30, 20, "Login"));
		addButton(cancelButton = new GuiButton(1, width / 2 + 20, height - 50, width / 2 - 30, 20,
				I18n.format("gui.cancel")));
		addButton(addButton = new GuiButton(2, 10, height - 25, width / 3 - 40, 20, "Add Account"));
		addButton(editButton = new GuiButton(3, width / 3 + 20, height - 25, width / 3 - 40, 20, "Edit account"));
		addButton(
				removeButton = new GuiButton(4, width * 2 / 3 + 30, height - 25, width / 3 - 40, 20, "Remove account"));
		if (Secure.accounts.isEmpty()) {
			loginButton.enabled = false;
			editButton.enabled = false;
			removeButton.enabled = false;
		} else {
			selectedAccount = Secure.accounts.keySet().iterator().next();
		}

		accountList = new GuiSlotAccounts(mc, width, height, 50, height - 60, 38);
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
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 0:
			char[] pw = Secure.accounts.get(selectedAccount);
			if (pw == null) {
				mc.displayGuiScreen(new GuiLogin(parentScreen, this, selectedAccount));
			} else {
				try {
					Secure.login(selectedAccount, pw, true);
					mc.displayGuiScreen(parentScreen);
				} catch (AuthenticationException e) {
					mc.displayGuiScreen(new GuiErrorScreen("ReAuth", "Authentication Failed"));
				}
			}
			break;
		case 1:
			mc.displayGuiScreen(parentScreen);
			break;
		case 2:
			mc.displayGuiScreen(new GuiLogin(parentScreen, this));
			break;
		case 3:
			mc.displayGuiScreen(new GuiLogin(parentScreen, this, selectedAccount));
			break;
		case 4:
			Secure.accounts.remove(selectedAccount);
			if (Secure.accounts.isEmpty())
				mc.displayGuiScreen(parentScreen);
			else
				selectedAccount = Secure.accounts.keySet().iterator().next();
			LiteModReAuth.saveConfig();
			break;
		}
	}

	private class GuiSlotAccounts extends GuiSlot {

		public GuiSlotAccounts(Minecraft mcIn, int width, int height, int topIn, int bottomIn, int slotHeightIn) {
			super(mcIn, width, height, topIn, bottomIn, slotHeightIn);
		}

		@Override
		protected int getSize() {
			return Secure.accounts.size();
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			int i = 0;
			for (String accName : Secure.accounts.keySet()) {
				if (i == slotIndex) {
					selectedAccount = accName;
					break;
				}
				i++;
			}
			if (isDoubleClick) {
				actionPerformed(loginButton);
			}
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			int i = 0;
			for (String accName : Secure.accounts.keySet()) {
				if (i == slotIndex)
					return selectedAccount.equals(accName);
				i++;
			}
			return false;
		}

		@Override
		protected void drawBackground() {
			drawDefaultBackground();
		}

		@Override
		protected void drawSlot(int slotIndex, int xPos, int yPos, int heightIn, int mouseXIn, int mouseYIn,
				float partialTicks) {
			String username = "";
			int i = 0;
			for (String accName : Secure.accounts.keySet()) {
				if (i == slotIndex) {
					username = accName;
					break;
				}
			}

			String displayName = Secure.displayNames.get(username);
			drawString(fontRenderer, displayName, xPos + 50, yPos + 10, 0xffffff);

			GameProfile gameProfile = new GameProfile(null, displayName);
			Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> profileTextures = Minecraft.getMinecraft()
					.getSkinManager().loadSkinFromCache(gameProfile);
			ResourceLocation skinLocation;
			if (profileTextures.containsKey(MinecraftProfileTexture.Type.SKIN)) {
				skinLocation = Minecraft.getMinecraft().getSkinManager().loadSkin(
						profileTextures.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
			} else {
				UUID id = EntityPlayer.getUUID(gameProfile);
				skinLocation = DefaultPlayerSkin.getDefaultSkin(id);
			}

			Minecraft.getMinecraft().getTextureManager().bindTexture(skinLocation);
			drawScaledCustomSizeModalRect(xPos + 1, yPos + 1, 8, 8, 8, 8, 32, 32, 64, 64);
		}

	}

}
