package technicianlp.reauth;

import com.google.gson.Gson;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.awt.Desktop;
import java.util.*;
import java.net.URI;

import static technicianlp.reauth.HTTPUtils.postRequestFormURLEncoded;
import static technicianlp.reauth.HTTPUtils.postRequestJSON;


public class MSAuth {

    static String appID = "747bf062-ab9c-4690-842d-a77d18d4cf82";
    static String appSecret = "AZu1.RyB~4KRMj2t0_r4MdvV93..6BR6Aj";
    static String redirectURL = "http://localhost:23566/auth";
    static String scope = "XboxLive.signin offline_access";

    //client_id=747bf062-ab9c-4690-842d-a77d18d4cf82
    //response_type=code
    //redirect_uri=https%3A%2F%2Flocalhost%3A23566
    //scope=XboxLive.signin%20offline_access


    public static void runBrowserAuth() {
        startInternalServer();
    }

    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (Exception ex) {
        }
        return "";
    }

    public static void startInternalServer() {
        try {
            InternalHTTPServer.main();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static AuthTokenResponse getAuthToken(String code) throws IOException {
        List<NameValuePair> args = new ArrayList<>();
        args.add(new BasicNameValuePair("client_id",appID));
        args.add(new BasicNameValuePair("client_secret",appSecret));
        args.add(new BasicNameValuePair("code",code));
        args.add(new BasicNameValuePair("grant_type","authorization_code"));
        args.add(new BasicNameValuePair("redirect_uri",redirectURL));
        String resp = postRequestFormURLEncoded("https://login.live.com/oauth20_token.srf",args);

        Gson gson = new Gson();
        AuthTokenResponse atr = gson.fromJson(resp,AuthTokenResponse.class);
        return atr;
    }

    public static AuthTokenResponse getRefreshAuthToken(String refreshToken) throws IOException {
        List<NameValuePair> args = new ArrayList<>();
        args.add(new BasicNameValuePair("client_id",appID));
        args.add(new BasicNameValuePair("client_secret",appSecret));
        args.add(new BasicNameValuePair("refresh_token",refreshToken));
        args.add(new BasicNameValuePair("grant_type","refresh_token"));
        args.add(new BasicNameValuePair("redirect_uri",redirectURL));
        String resp = postRequestFormURLEncoded("https://login.live.com/oauth20_token.srf",args);

        Gson gson = new Gson();
        AuthTokenResponse atr = gson.fromJson(resp,AuthTokenResponse.class);
        return atr;
    }

    public static XBLAuthResponse AuthXBL(AuthTokenResponse atr) throws IOException {
        String json = "{\"Properties\":{\"AuthMethod\":\"RPS\",\"SiteName\":\"user.auth.xboxlive.com\",\"RpsTicket\":\"d="+atr.access_token+"\"},\"RelyingParty\":\"http://auth.xboxlive.com\",\"TokenType\":\"JWT\"}";
        Gson gson = new Gson();
        String resp =  postRequestJSON("https://user.auth.xboxlive.com/user/authenticate",json);
        return gson.fromJson(resp,XBLAuthResponse.class);
    }
    public static XBLAuthResponse AuthXSTS(XBLAuthResponse xbl) throws IOException {
        String json = "{\"Properties\":{\"SandboxId\":\"RETAIL\",\"UserTokens\":[\""+xbl.Token+"\"]},\"RelyingParty\":\"rp://api.minecraftservices.com/\",\"TokenType\":\"JWT\"}";
        Gson gson = new Gson();
        String resp =  postRequestJSON("https://xsts.auth.xboxlive.com/xsts/authorize",json);
        return gson.fromJson(resp,XBLAuthResponse.class);
    }

    public static MCAuthResponse AuthMC(XBLAuthResponse xsts) throws IOException { //finally
        String json = "{\"identityToken\":\"XBL3.0 x="+xsts.DisplayClaims.xui[0].uhs+";"+xsts.Token+"\"}";
        Gson gson = new Gson();
        String resp =  postRequestJSON("https://api.minecraftservices.com/authentication/login_with_xbox",json);
        return gson.fromJson(resp,MCAuthResponse.class);

    }

    public static class AuthTokenResponse{
        public String error;
        public String error_description;
        public String correlation_id;

        public String token_type;
        public String scope;
        public String access_token;
        public String expires_in;
        public String refresh_token;
        public String user_id;
    }

    public static class XBLAuthResponse{
        public String IssueInstant;
        public String NotAfter;
        public String Token;

        public DisplayClaims_ DisplayClaims;

        public static class DisplayClaims_{
            public uhs_[] xui;
            public static class uhs_{
                public String uhs;
            }
        }
    }

    public static class MCAuthResponse{
        public String username;
        public String[] roles;
        public String access_token;
        public String token_type;
        public int expires_in;
    }
}
