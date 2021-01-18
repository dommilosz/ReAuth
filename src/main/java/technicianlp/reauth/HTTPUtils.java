package technicianlp.reauth;

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
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

public class HTTPUtils {
    public static String postRequestFormURLEncoded(String url, List<NameValuePair> urlParameters) throws IOException {
        return postRequest(url,new UrlEncodedFormEntity(urlParameters));
    }

    public static String postRequestJSON(String url,String json) throws IOException {
        return postRequest(url,new StringEntity(json, ContentType.APPLICATION_JSON));
    }
    public static String postRequest(String url, HttpEntity data) throws IOException {
        HttpPost post = new HttpPost(url);
        post.setEntity(data);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            return (EntityUtils.toString(response.getEntity()));
        }
    }

    public static String getRequestWithBearer(String url, String bearerToken ) throws IOException {
        HttpGet post = new HttpGet(url);
        post.setHeader("Authorization", "Bearer "+bearerToken);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            return (EntityUtils.toString(response.getEntity()));
        }
    }
    public static String getRequest(String url) throws IOException {
        HttpGet post = new HttpGet(url);
        try (CloseableHttpClient httpClient = HttpClients.createDefault();
             CloseableHttpResponse response = httpClient.execute(post)) {

            return (EntityUtils.toString(response.getEntity()));
        }
    }
}
