package technicianlp.reauth.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Session;
import technicianlp.reauth.ISessionHolder;

@Mixin(Minecraft.class)
public class MixinMinecraft implements ISessionHolder {

	@Shadow
	private Session session;

	@Override
	@Shadow
	public Session getSession() {
		return null;
	}

	@Override
	public void setSession(Session session) {
		this.session = session;
	}
	
}
