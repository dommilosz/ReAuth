package technicianlp.reauth;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.google.common.base.Charsets;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService;
import com.mojang.authlib.yggdrasil.YggdrasilMinecraftSessionService;
import com.mojang.authlib.yggdrasil.YggdrasilUserAuthentication;
import com.mojang.util.UUIDTypeAdapter;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.PlayerProfileCache;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.util.Session;

final class Secure {

    /**
     * Username/email -> password map
     */
    static Map<String, Account> accounts = new LinkedHashMap<>();
    static String offlineUsername = "";

    /**
     * Mojang authentificationservice
     */
    private static final YggdrasilAuthenticationService yas;
    private static final YggdrasilUserAuthentication yua;
    private static final YggdrasilMinecraftSessionService ymss;

    static {
        /* initialize the authservices */
        yas = new YggdrasilAuthenticationService(Minecraft.getMinecraft().getProxy(), UUID.randomUUID().toString());
        yua = (YggdrasilUserAuthentication) yas.createUserAuthentication(Agent.MINECRAFT);
        ymss = (YggdrasilMinecraftSessionService) yas.createMinecraftSessionService();
    }

    public static void initSkinStuff() {
        GameProfileRepository gpr = yas.createProfileRepository();
        PlayerProfileCache ppc = new PlayerProfileCache(gpr, new File(Minecraft.getMinecraft().mcDataDir, MinecraftServer.USER_CACHE_FILE.getName()));
        TileEntitySkull.setProfileCache(ppc);
        TileEntitySkull.setSessionService(ymss);
    }

    /**
     * Logs you in; replaces the Session in your client; and saves to config
     */
    static void login(String user, char[] pw, boolean savePassToConfig) throws AuthenticationException {
        /* set credentials */
        Secure.yua.setUsername(user);
        Secure.yua.setPassword(new String(pw));

        /* login */
        Secure.yua.logIn();

        LiteModReAuth.log.info("Login successful!");

        /* put together the new Session with the auth-data */
        String username = Secure.yua.getSelectedProfile().getName();
        UUID uuid = Secure.yua.getSelectedProfile().getId();
        String uuidStr = UUIDTypeAdapter.fromUUID(uuid);
        String access = Secure.yua.getAuthenticatedToken();
        String type = Secure.yua.getUserType().getName();
        Sessionutil.set(new Session(username, uuidStr, access, type));

        /* logout to discard the credentials in the object */
        Secure.yua.logOut();

        /* save username and password to config */
        Secure.accounts.put(user, new Account(user, savePassToConfig ? pw : null, uuid, username));

        LiteModReAuth.saveConfig();
    }

    static void offlineMode(String username) {
        /* Create offline uuid */
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
        Sessionutil.set(new Session(username, uuid.toString(), "invalid", "legacy"));
        LiteModReAuth.log.info("Offline Username set!");
        Secure.offlineUsername = username;
    }

    /**
     * checks online if the session is valid
     */
    static boolean SessionValid() {
        try {
            GameProfile gp = Sessionutil.get().getProfile();
            String token = Sessionutil.get().getToken();
            String id = UUID.randomUUID().toString();

            Secure.ymss.joinServer(gp, token, id);
            if (Secure.ymss.hasJoinedServer(gp, id, null).isComplete()) {
                LiteModReAuth.log.info("Session validation successful");
                return true;
            }
        } catch (Exception e) {
            LiteModReAuth.log.info("Session validation failed: " + e.getMessage());
            return false;
        }
        LiteModReAuth.log.info("Session validation failed!");
        return false;
    }

    static final class Sessionutil {
        static Session get() {
            return Minecraft.getMinecraft().getSession();
        }

        static void set(Session s) {
            ((ISessionHolder) Minecraft.getMinecraft()).setSession(s);
            GuiHandler.invalidateStatus();
        }
    }

    static class Account {
        private String username;
        private char[] password;
        private UUID uuid;
        private String displayName;
        private long lastQuery = 0;

        Account(String username, char[] password, UUID uuid, String displayName) {
            this.username = username;
            this.password = password;
            this.uuid = uuid;
            this.displayName = displayName;
        }

        String getUsername() {
            return username;
        }

        char[] getPassword() {
            return password;
        }

        UUID getUuid() {
            return uuid;
        }

        String getDisplayName() {
            return displayName;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }

        public void setLastQuery(long lastQuery) {
            this.lastQuery = lastQuery;
        }

        public long getLastQuery() {
            return lastQuery;
        }
    }
}
