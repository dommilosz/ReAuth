package technicianlp.reauth;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.*;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mumfrey.liteloader.LiteMod;
import com.mumfrey.liteloader.core.LiteLoader;
import static technicianlp.reauth.Secure.Account;

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
    public static void loadConfigDefault(){
        loadConfig(new File(LiteLoader.getCommonConfigFolder(), ".ReAuth.properties"));
    }
    public static void loadConfig(File configFile) {
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
            Gson g = new Gson();
            Secure.accounts.clear();
            configAccount[] accounts = g.fromJson(config.getProperty("accounts_data"), configAccount[].class);
            Secure.accounts = configAccount.toSecureAccounts(accounts);

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

        Gson g = new Gson();
        config.setProperty("accounts_data", g.toJson(configAccount.getConfigAccounts()));

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

    public static class configAccount{
        public String key;
        public Secure.Account account;

        public configAccount(String key, Account account) {
            this.key = key;
            this.account = account;
        }

        public static configAccount[] getConfigAccounts(){
            configAccount[] accountsArr = new configAccount[Secure.accounts.size()];
            final int[] i = {0};
            Secure.accounts.forEach((key, value)-> {
                accountsArr[i[0]] = new configAccount(key,value);
                i[0]++;
            });
            return accountsArr;
        }

        public static Map<String, Account> toSecureAccounts(configAccount[] accounts) {
            Map<String, Account> accountMap = new LinkedHashMap<>();
            for (configAccount acc:accounts){
                if(acc.account.AccUUID!=null){
                    acc.account.setLastQuery(0);
                    accountMap.put(acc.key, acc.account);
                }

            }
            return accountMap;
        }
    }

}
