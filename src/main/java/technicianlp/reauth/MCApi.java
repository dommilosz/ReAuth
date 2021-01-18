package technicianlp.reauth;

import com.google.gson.Gson;

import java.io.IOException;

import static technicianlp.reauth.HTTPUtils.*;

public class MCApi {
    public static MCProfile getProfile(String token) throws IOException {
        String resp = getRequestWithBearer("https://api.minecraftservices.com/minecraft/profile",token);
        Gson g = new Gson();
        return g.fromJson(resp,MCProfile.class);
    }

    public static ownershipReq checkGameOwnership(String token) throws IOException {
        String resp = getRequestWithBearer("https://api.minecraftservices.com/entitlements/mcstore",token);
        Gson g = new Gson();
        return g.fromJson(resp,ownershipReq.class);
    }

    public class MCProfile{
        public String id;
        public String name;
        public Skin[] skins;
        public class Skin{
            public String id;
            public String state;
            public String url;
            public String variant;
            public String alias;
        }
    }
    public class ownershipReq{
        Object[] items;
        String signature;
        String keyId;
    }
}
