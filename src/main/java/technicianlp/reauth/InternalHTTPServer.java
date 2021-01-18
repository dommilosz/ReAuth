package technicianlp.reauth;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;

import java.io.*;
import java.net.InetSocketAddress;
import javax.xml.ws.*;
import javax.xml.ws.http.*;
import javax.xml.transform.*;
import javax.xml.transform.stream.*;

@WebServiceProvider
@ServiceMode(value = Service.Mode.PAYLOAD)
public class InternalHTTPServer {
    static HttpServer server;
    public static void main() throws Exception {
        codeExport = "";
        if(server!=null)server.stop(0);
        server = HttpServer.create(new InetSocketAddress(23566), 0);
        server.createContext("/auth", new MyHandler());
        server.createContext("/auth/", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
    }

    static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String uri =t.getRequestURI().toString();
            String code = uri.split("auth\\?")[1];

            String html = "";
            html+="<html>";
            html+="<body>";
            if(code.contains("code=")){
                html+="<h1>Everything is successful. You do not need to do anything with this code.</h1>";
                html+="<h1>Your code: "+code.split("code=")[1]+"</h1>";
            }else {
                html+="<h1>ERROR</h1>";
                html+=code;
            }

            html+="</body>";
            html+="</html>";

            t.sendResponseHeaders(200, html.length());
            OutputStream os = t.getResponseBody();
            os.write(html.getBytes());
            os.close();

            server.stop(0);
            codeExport = code;
        }
    }
    public static String codeExport = "";
}
