package technicianlp.reauth;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

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
    static void login(String user, char[] pw, boolean savePassToConfig, String editingUUID) throws AuthenticationException {
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
        Account a = new Account(user, savePassToConfig ? pw : null, uuid, username);
        a.Token = access;

        saveAccount(editingUUID, a);


    }

    static void addAccount(Account acc) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AtomicBoolean replace = new AtomicBoolean(false);
                AtomicBoolean exists = new AtomicBoolean(false);
                AtomicReference<Account> existing = new AtomicReference<>();
                Secure.accounts.forEach((key, value) -> {
                    if (acc.uuid.equals(value.uuid)) {
                        exists.set(true);
                        existing.set(value);
                        if (!GuiPopup.type.equals("yesno")) GuiPopup.WaitForUserAction();
                        GuiPopup.DoYesNo("Account Already Exists\nDo You want to replace it?", "Replace", "Cancel");

                        GuiPopup.WaitForUserAction();
                        replace.set(GuiPopup.response);
                    }
                });
                if (replace.get() && exists.get()) {
                    editAccount(existing.get().AccUUID, acc);
                    return;
                }
                if (exists.get()) return;
                String uuid = UUID.randomUUID().toString();
                acc.AccUUID = uuid;
                Secure.accounts.put(uuid, acc);
                LiteModReAuth.saveConfig();
            }
        }).start();
    }

    static void editAccount(String uuid, Account newAcc) {
        Secure.accounts.put(uuid, newAcc);
        newAcc.AccUUID = uuid;
        LiteModReAuth.saveConfig();
    }

    static void saveAccount(String uuid, Account acc) {
        if (uuid != null) {
            editAccount(uuid, acc);
        } else {
            addAccount(acc);
        }
    }

    static boolean token(String token, String editingUUID) throws AuthenticationException, IOException {
        try {
            MCApi.MCProfile profile = MCApi.getProfile(token);
            if (profile.name == null || profile.id == null) throw new IOException();
            Sessionutil.set(new Session(profile.name, profile.id, token, "mojang"));
            LiteModReAuth.log.info("Login successful!");
            saveAccount(editingUUID, new Account(token));
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    static boolean useToken(String token, Account a) throws AuthenticationException, IOException {
        try {
            MCApi.MCProfile profile = MCApi.getProfile(token);
            if (profile.name == null || profile.id == null) throw new IOException();
            Sessionutil.set(new Session(profile.name, profile.id, token, "mojang"));
            LiteModReAuth.log.info("Login successful!");
            a.uuid = UUIDFromShortString(profile.id);
            a.displayName = profile.name;
            LiteModReAuth.saveConfig();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    static void offlineMode(String username, String editingUUID) {
        /* Create offline uuid */
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
        Sessionutil.set(new Session(username, uuid.toString(), "invalid", "legacy"));
        LiteModReAuth.log.info("Offline Username set!");
        Secure.offlineUsername = username;
        saveAccount(editingUUID, new Account(username, uuid.toString()));

    }

    static Account microsoft(String token, String refreshToken, String editingUUID) throws IOException {
        MCApi.MCProfile profile = MCApi.getProfile(token);

        Sessionutil.set(new Session(profile.name, profile.id, token, "microsoft"));
        LiteModReAuth.log.info("Login successful!");
        Account a = Account.accountFromMicrosoft(token, refreshToken);
        saveAccount(editingUUID, a);
        return a;
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
        public String AccUUID;
        private String displayName;
        private long lastQuery = 0;

        public String accountType = "mojang";
        public String MS_refreshToken;
        public String Token;

        Account(String username, char[] password, UUID uuid, String displayName) {
            this.username = username;
            this.password = password;
            this.uuid = uuid;
            this.displayName = displayName;
        }

        Account(String token) {
            try {
                MCApi.MCProfile profile = MCApi.getProfile(token);
                this.uuid = UUIDFromShortString(profile.id);
                this.displayName = profile.name;
                this.Token = token;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Account(String token, String user, char[] pass) {
            try {
                MCApi.MCProfile profile = MCApi.getProfile(token);
                this.uuid = UUID.nameUUIDFromBytes(profile.id.getBytes("UTF-8"));
                this.displayName = profile.name;
                this.Token = token;
                this.username = user;
                this.password = pass;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Account(String username, String uuid) {
            this.uuid = UUID.fromString(uuid);
            this.displayName = username;

        }

        public static Account accountFromMicrosoft(String token, String refreshToken) {
            Account a = new Account(token);
            a.MS_refreshToken = refreshToken;
            a.accountType = "microsoft";
            a.Token = token;
            return a;
        }

        public static Account accountFromOffline(String username, String uuid) {
            Account a = new Account(username, uuid);
            a.accountType = "offline";
            return a;
        }


        String getUsername() {
            if (username == null) return displayName;
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

        public void login() {
            try {
                if (accountType.equals("mojang")) {
                    if (this.Token != null) {
                        if (Secure.useToken(this.Token, this)) return;
                    }
                    if (this.getPassword() == null) {
                        Minecraft.getMinecraft().displayGuiScreen(new GuiLogin(Minecraft.getMinecraft().currentScreen, Minecraft.getMinecraft().currentScreen));
                    } else
                        Secure.login(this.getUsername(), this.getPassword(), true, this.AccUUID);
                }
                if (accountType.equals("token")) {
                    Secure.useToken(this.Token, this);
                }
                if (accountType.equals("offline")) {
                    Secure.offlineMode(this.username, this.AccUUID);
                }
                if (accountType.equals("microsoft")) {
                    try {
                        if (!Secure.useToken(this.Token, this)) throw new Exception();
                    } catch (Exception ex) {

                        MSAuth.AuthTokenResponse atr = MSAuth.getRefreshAuthToken(this.MS_refreshToken);
                        GuiMicrosoft gM = new GuiMicrosoft(Minecraft.getMinecraft().currentScreen,this);
                        Minecraft.getMinecraft().displayGuiScreen(gM);
                        gM.safeAuthFlow(atr);
                    }
                }
            } catch (Exception ex) {
            }
        }
    }

    public static UUID UUIDFromShortString(String uuid) {
        String uuidLong = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
        return UUID.fromString(uuidLong);
    }
}
