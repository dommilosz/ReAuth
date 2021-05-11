package technicianlp.reauth;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
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
    static ArrayList<Account> accounts = new ArrayList<>();
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
    static void login(String user, char[] pw, boolean savePassToConfig, int editingIndex) throws AuthenticationException {
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

        saveAccount(editingIndex, a);
    }

    static void loginCustom(String server,String user, char[] pw, boolean savePassToConfig, int editingIndex) throws AuthenticationException, IOException {
        CustomAuth.CustomAuthResponse response = CustomAuth.authenticate(server, user, String.valueOf(pw));

        /* save username and password to config */
        Account a = new Account(server,user, savePassToConfig ? pw : null, UUIDFromShortString(response.selectedProfile.id), response.selectedProfile.name);
        a.Token = response.accessToken;

        saveAccount(editingIndex, a);


    }

    static void addAccount(Account acc) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                AtomicBoolean replace = new AtomicBoolean(false);
                AtomicBoolean duplicate = new AtomicBoolean(false);
                AtomicBoolean exists = new AtomicBoolean(false);
                AtomicReference<Account> existing = new AtomicReference<>();
                for (Account value : Secure.accounts) {
                    if (acc.uuid.equals(value.uuid)) {
                        exists.set(true);
                        existing.set(value);
                        if (!GuiPopup.type.equals(GuiPopup.Type.Type3Optn)) GuiPopup.WaitForUserAction();
                        GuiPopup.Do3Options("Account Already Exists\nDo You want to replace it?", "Replace", "Duplicate", "Do Nothing");

                        GuiPopup.WaitForUserAction();
                        duplicate.set(false);
                        replace.set(false);
                        if (GuiPopup.response3Optn == 0) replace.set(true);
                        if (GuiPopup.response3Optn == 1) {
                            duplicate.set(true);
                        }
                    }
                }
                if (replace.get() && exists.get()) {
                    editAccount(accounts.indexOf(existing.get()), acc);
                    return;
                }
                if (duplicate.get() && exists.get()) {
                    Secure.accounts.add(acc);
                    LiteModReAuth.saveConfig();
                    return;
                }
                if (exists.get()) return;
                Secure.accounts.add(acc);
                LiteModReAuth.saveConfig();
            }
        }).start();
    }

    static void editAccount(int index, Account newAcc) {
        Secure.accounts.set(index, newAcc);

        LiteModReAuth.saveConfig();
    }

    static void saveAccount(int uuid, Account acc) {
        if (uuid >= 0) {
            editAccount(uuid, acc);
        } else {
            addAccount(acc);
        }
    }

    static boolean token(String token, int editingIndex) throws AuthenticationException, IOException {
        try {
            MCApi.MCProfile profile = MCApi.getProfile(token);
            if (profile.name == null || profile.id == null) throw new IOException();
            Sessionutil.set(new Session(profile.name, profile.id, token, "mojang"));
            LiteModReAuth.log.info("Login successful!");
            saveAccount(editingIndex, new Account(token));
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

    static void offlineMode(String username, int editingIndex) {
        /* Create offline uuid */
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(Charsets.UTF_8));
        Sessionutil.set(new Session(username, uuid.toString(), "invalid", "legacy"));
        LiteModReAuth.log.info("Offline Username set!");
        Secure.offlineUsername = username;

        saveAccount(editingIndex, new Account(username, uuid.toString()));

    }

    static Account microsoft(String token, String refreshToken, int editingIndex) throws IOException {
        MCApi.MCProfile profile = MCApi.getProfile(token);

        Sessionutil.set(new Session(profile.name, profile.id, token, "microsoft"));
        LiteModReAuth.log.info("Login successful!");
        Account a = Account.accountFromMicrosoft(token, refreshToken);
        saveAccount(editingIndex, a);
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
        private String displayName;
        private long lastQuery = 0;

        public String accountType = "mojang";
        public String MS_refreshToken;
        public String Token;
        public String authserver;

        Account(String authServer,String username, char[] password, UUID uuid, String displayName) {
            this.username = username;
            this.password = password;
            this.uuid = uuid;
            this.displayName = displayName;
            accountType = "custom";
            this.authserver = authServer;
        }

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
                this.accountType = "token";
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
            accountType = "offline";
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
                        Secure.login(this.getUsername(), this.getPassword(), true, this.getIndex());
                }
                if (accountType.equals("token")) {
                    Secure.useToken(this.Token, this);
                }
                if (accountType.equals("offline")) {
                    Secure.offlineMode(this.displayName, this.getIndex());
                }
                if (accountType.equals("microsoft")) {
                    try {
                        if (!Secure.useToken(this.Token, this)) throw new Exception();
                    } catch (Exception ex) {

                        MSAuth.AuthTokenResponse atr = MSAuth.getRefreshAuthToken(this.MS_refreshToken);
                        GuiMicrosoft gM = new GuiMicrosoft(Minecraft.getMinecraft().currentScreen, this);
                        Minecraft.getMinecraft().displayGuiScreen(gM);
                        gM.safeAuthFlow(atr);
                    }
                }
                if (accountType.equals("custom")) {
                    if (this.Token != null) {
                        if (Secure.useToken(this.Token, this)) return;
                    }
                    if (this.getPassword() == null) {
                        Minecraft.getMinecraft().displayGuiScreen(new GuiCustom(Minecraft.getMinecraft().currentScreen, Minecraft.getMinecraft().currentScreen));
                    } else
                        Secure.loginCustom(authserver,this.getUsername(), this.getPassword(), true, this.getIndex());

                }
            } catch (Exception ignored) {
            }
        }

        public int getIndex() {
            return accounts.indexOf(this);
        }

        public String deserialise() {
            try {
                Gson g = new Gson();
                return g.toJson(this);
            } catch (Exception ex) {
                return "ERROR";
            }
        }

        public static Account serialise(String json) {
            try {
                Gson g = new Gson();
                return g.fromJson(json, Account.class);
            } catch (Exception ex) {
                return null;
            }
        }
    }

    public static UUID UUIDFromShortString(String uuid) {
        try {
            String uuidLong = uuid.substring(0, 8) + "-" + uuid.substring(8, 12) + "-" + uuid.substring(12, 16) + "-" + uuid.substring(16, 20) + "-" + uuid.substring(20);
            if (uuid.length() == 36) uuidLong = uuid;
            return UUID.fromString(uuidLong);
        } catch (Exception ex) {
            return UUID.randomUUID();
        }
    }

    public static Secure.Account getCurrentAccount() {
        try {
            UUID current = UUIDFromShortString(Minecraft.getMinecraft().getSession().getPlayerID());
            AtomicReference<Account> a = new AtomicReference<>();
            for (Account value : accounts) {
                if (value.uuid.equals(current)) a.set(value);
            }
            if (a.get() != null)
                return a.get();
        } catch (Exception ex) {
            return Secure.accounts.get(0);
        }
        return Secure.accounts.get(0);
    }
}
