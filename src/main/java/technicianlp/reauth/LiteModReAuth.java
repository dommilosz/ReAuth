package technicianlp.reauth;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.core.LiteLoader;

public class LiteModReAuth implements LiteMod {

    static boolean offlineModeEnabled;

    static final Logger log = LogManager.getLogger("ReAuth");

    @Override
    public String getName() {
        return "reauth";
    }

    @Override
    public String getVersion() {
        return "3.6.0";
    }

    @Override
    public void init(File configPath) {
        loadConfig(new File(configPath, ".ReAuth.properties"));
    }

    @Override
    public void upgradeSettings(String version, File configPath, File oldConfigPath) {
    }

    private static void loadConfig(File configFile) {
        Properties config = new Properties();
        try {
            config.load(new FileReader(configFile));
        } catch (IOException e) {
            return;
        }

        try {
            int numAccounts = Integer.parseInt(config.getProperty("accounts", "0"));
            if (numAccounts > 1000) // who has more than 1000 accounts anyway?
                numAccounts = 1000;
            for (int accNo = 0; accNo < numAccounts; accNo++) {
                String user = config.getProperty("username." + accNo);
                if (user == null)
                    continue;
                String pass = config.getProperty("password." + accNo);
                char[] pw = pass == null ? null : pass.toCharArray();
                String uuidStr = config.getProperty("uuid." + accNo);
                UUID uuid = uuidStr == null ? null : UUID.fromString(uuidStr);
                String displayName = config.getProperty("displayName." + accNo, user);
                Secure.accounts.put(user, new Secure.Account(user, pw, uuid, displayName));
            }

            LiteModReAuth.offlineModeEnabled = Boolean.parseBoolean(config.getProperty("offlineModeEnabled", "false"));

            GuiHandler.enabled = Boolean.parseBoolean(config.getProperty("validatorEnabled", "true"));
            GuiHandler.bold = Boolean.parseBoolean(config.getProperty("validatorBold", "true"));

        } catch (NumberFormatException e) {
            return;
        }
    }

    public static void saveConfig() {
        Properties config = new Properties();
        config.setProperty("accounts", String.valueOf(Secure.accounts.size()));

        int accNo = 0;
        for (Secure.Account acc : Secure.accounts.values()) {
            config.setProperty("username." + accNo, acc.getUsername());
            if (acc.getPassword() != null)
                config.setProperty("password." + accNo, new String(acc.getPassword()));
            config.setProperty("displayName." + accNo, acc.getDisplayName());
            if (acc.getUuid() != null)
                config.setProperty("uuid." + accNo, acc.getUuid().toString());
            accNo++;
        }

        config.setProperty("offlineModeEnabled", String.valueOf(LiteModReAuth.offlineModeEnabled));

        config.setProperty("validatorEnabled", String.valueOf(GuiHandler.enabled));
        config.setProperty("validatorBold", String.valueOf(GuiHandler.bold));

        try {
            config.store(new FileWriter(new File(LiteLoader.getCommonConfigFolder(), ".ReAuth.properties")),
                    "ReAuth configuration file");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
