import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

public class SMSAdapter {

    private String useProxy;
    private String useAuth;
    private String scheme;
    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPass;
    private String restUrl = "https://abc.xyz/abc.php/sms/send?q=123456&destination=1234567&message=message text";
    private String restMethod = "GET";
    private RequestConfig config = null;

    public SMSAdapter(final String useProxy, final String useAuth, final String scheme, final String proxyHost,
                      final int proxyPort, final String proxyUser, final String proxyPass) {

        this.useProxy = useProxy;
        this.useAuth = useAuth;
        this.scheme = scheme;
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPass = proxyPass;

        System.out.println("Configured an SMSAdapter with settings as [ Use Proxy : " + this.useProxy + " Use Auth : "
                + this.useAuth + " Scheme : " + this.scheme + " Proxy Host " + this.proxyHost + " Proxy Port : " + this.proxyPort + " Proxy User : "
                + this.proxyUser + " Proxy Password : " + this.proxyPass);
    }

    public String sendSMS() {

        config = null;
        System.out.println("Attempting to get a client");
        CloseableHttpClient client = getClient();
        System.out.println("Client configured");
        String status = "0";
        try {
            System.out.println("configuring request...");
            HttpRequestBase request = getRequest();
            System.out.println("Configured request");

            System.out.println("checking if config is null");
            if (config != null) {
                System.out.println("setting proxy config in request");
                request.setConfig(config);
                System.out.println("Proxy config setting completed");
            }
            System.out.println("Executing request");
            CloseableHttpResponse response = client.execute(request);
            System.out.println("Request executed");
            status = String.valueOf(response.getStatusLine());
            System.out.println("status" + status);
            System.out.println("Closing response");
            response.close();
            System.out.println("Response closed");
        } catch (Exception ex) {
            System.out.println("Exception occurred");
            ex.printStackTrace();
        } finally {
            try {
                System.out.println("Closing client");
                client.close();
                System.out.println("Client closed");
            } catch (Exception e) {
                System.out.println("Exception occurred while closing client");
                e.printStackTrace();
            }
        }
        return status;
    }

    private CloseableHttpClient getClient() {

        CloseableHttpClient client = null;
        System.out.println("Checking if use proxy is enabled");
        if (useProxy.equalsIgnoreCase("Y")) {
            System.out.println("Use proxy is enable");
            System.out.println("Verifying if Authentication is enabled");
            if (useAuth.equalsIgnoreCase("Y")) {
                System.out.println("Authentication is enabled");
                CredentialsProvider credsProvider = new BasicCredentialsProvider();
                credsProvider.setCredentials(
                        new AuthScope(proxyHost, proxyPort),
                        new UsernamePasswordCredentials(proxyUser, proxyPass));
                client = HttpClients.custom().setDefaultCredentialsProvider(credsProvider).build();
                System.out.println("Configured a client using proxy as well as authentication");
            } else {
                System.out.println("Authentication is disabled");
                client = HttpClients.createDefault();
                HttpHost proxy = new HttpHost(proxyHost, proxyPort, scheme);
                config = RequestConfig.custom()
                        .setProxy(proxy)
                        .build();
                System.out.println("Configured a client using proxy but no authentication");
            }
        } else {
            client = HttpClients.createDefault();
            System.out.println("Configured a simple client using no proxy and authentication");
        }
        System.out.println("returning client");
        return client;
    }

    private HttpRequestBase getRequest() {

        HttpRequestBase request = null;
        String parsedUrl = parseUrl();
        if (restMethod.equalsIgnoreCase("GET")) {
            request = new HttpGet(parsedUrl);
        } else if (restMethod.equalsIgnoreCase("POST")) {
            request = new HttpPost(parsedUrl);
        } else if (restMethod.equalsIgnoreCase("PUT")) {
            request = new HttpPut(parsedUrl);
        }
        return request;
    }

    private String parseUrl() {
        return restUrl;
    }

    public static void main(String args[]) {

        String useProxy;
        String useAuth;
        String scheme;
        String proxyHost;
        int proxyPort;
        String proxyUser;
        String proxyPass;

        try {
            //For command line arguments
            useProxy = args[0];
            useAuth = args[1];
            scheme = args[2];
            proxyHost = args[3];
            proxyPort = Integer.parseInt(args[4]);
            proxyUser = args[5];
            proxyPass = args[6];

        } catch (Exception e) {
            //For hardcoded  arguments
            useProxy = "";
            useAuth = "";
            scheme = "";
            proxyHost = "";
            proxyPort = 0;
            proxyUser = "";
            proxyPass = "";
        }

        SMSAdapter smsAdapter = new SMSAdapter(useProxy, useAuth, scheme, proxyHost, proxyPort, proxyUser, proxyPass);
        smsAdapter.sendSMS();
    }
}
