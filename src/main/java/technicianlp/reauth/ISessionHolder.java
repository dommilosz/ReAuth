package technicianlp.reauth;

import net.minecraft.util.Session;

public interface ISessionHolder {

    Session getSession();

    void setSession(Session session);

}
