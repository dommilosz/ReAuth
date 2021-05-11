package technicianlp.reauth;

import com.google.gson.Gson;
import com.mojang.authlib.UserType;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.exceptions.InvalidCredentialsException;
import com.mojang.authlib.yggdrasil.request.AuthenticationRequest;
import com.mojang.authlib.yggdrasil.response.AuthenticationResponse;
import com.mojang.authlib.yggdrasil.response.User;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static technicianlp.reauth.HTTPUtils.postRequestJSON;

public class CustomAuth {
    private static final Logger LOGGER = LogManager.getLogger();
    public static CustomAuthResponse authenticate(String url, String username, String password ) throws IOException {
        String resp = postRequestJSON(url,"{\"username\":\""+username+"\",\"password\":\""+password+"\"}");
        Gson gson = new Gson();
        return gson.fromJson(resp, CustomAuthResponse.class);
    }
    class CustomAuthResponse{
        public String clientToken;
        public String accessToken;
        public Profile[] availableProfiles;
        public Profile selectedProfile;


        public class User{
            public String username;
            public Prop[] properties;
            public String id;
            public class Prop{
                public String name;
                public String value;
            }
        }

        public class Profile{
            public String name;
            public String id;
        }
    }
}

