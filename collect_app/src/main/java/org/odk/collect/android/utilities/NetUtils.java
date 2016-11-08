package org.odk.collect.android.utilities;

/**
 * Created by sabbir on 10/27/16.
 */



        import android.content.Context;
        import android.content.SharedPreferences;
        import android.net.ConnectivityManager;
        import android.net.NetworkInfo;
        import android.preference.PreferenceManager;
        import android.util.Base64;
        import android.util.Log;

        import org.apache.http.HttpResponse;
        import org.apache.http.auth.AuthScope;
        import org.apache.http.auth.Credentials;
        import org.apache.http.auth.UsernamePasswordCredentials;
        import org.apache.http.client.CookieStore;
        import org.apache.http.client.CredentialsProvider;
        import org.apache.http.client.HttpClient;
        import org.apache.http.client.methods.HttpGet;
        import org.apache.http.client.params.AuthPolicy;
        import org.apache.http.client.params.ClientPNames;
        import org.apache.http.client.params.HttpClientParams;
        import org.apache.http.client.protocol.ClientContext;
        import org.apache.http.conn.ClientConnectionManager;
        import org.apache.http.conn.scheme.PlainSocketFactory;
        import org.apache.http.conn.scheme.Scheme;
        import org.apache.http.conn.scheme.SchemeRegistry;
        import org.apache.http.impl.client.BasicCookieStore;
        import org.apache.http.impl.client.BasicCredentialsProvider;
        import org.apache.http.impl.client.DefaultHttpClient;
        import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
        import org.apache.http.params.BasicHttpParams;
        import org.apache.http.params.HttpConnectionParams;
        import org.apache.http.params.HttpParams;
        import org.apache.http.protocol.BasicHttpContext;
        import org.apache.http.protocol.HttpContext;
        import org.apache.http.protocol.SyncBasicHttpContext;
        import org.koboc.collect.android.R;


        import java.io.UnsupportedEncodingException;
        import java.net.MalformedURLException;
        import java.net.URI;
        import java.net.URL;
        import java.net.URLDecoder;
        import java.security.MessageDigest;
        import java.security.NoSuchAlgorithmException;
        import java.util.ArrayList;
        import java.util.List;
        import org.koboc.collect.android.*;




/**
 * NetUtils - URL and network related utility class
 *
 * @author Mehdi Hasan <mhasan@mpower-health.com>
 * @author ratna halder  <ratna@mpower-health.com>
 *
 */
public class NetUtils {

    public static final String URL_PART_LOGIN = "/login/";
    public static final String URL_PART_ACTIVE_INTERVIEW = "/m/interview/active";

    public static final String HTTP_CONTENT_TYPE_JSON = "application/json";
    public static final String HTTP_CONTENT_TYPE_JPEG = "image/jpeg";
    public static final String URL_PART_NOTIFICATION = "/m/notification";
    public static final String URL_PART_IDENTIFICATION = "/m/identification";
    private static HttpContext localContext = null;

    public static final AuthScope buildAuthScopes(String host) {

        AuthScope a;
        // allow digest auth on any port...
        a = new AuthScope(host, -1, null, AuthPolicy.DIGEST);

        return a;
    }

    public static final void clearAllCredentials() {
        HttpContext localContext = getHttpContext();
        CredentialsProvider credsProvider = (CredentialsProvider) localContext
                .getAttribute(ClientContext.CREDS_PROVIDER);
        credsProvider.clear();

        //Clear credentials from odk authentication
        org.koboc.collect.android.utilities.WebUtils.clearAllCredentials();
    }

    public static boolean hasCredentials(String username, String host, HttpContext context) {
        HttpContext localContext = context;
        CredentialsProvider credsProvider = (CredentialsProvider) localContext
                .getAttribute(ClientContext.CREDS_PROVIDER);

        AuthScope a = buildAuthScopes(host);
        boolean hasCreds = true;

        Credentials c = credsProvider.getCredentials(a);
        if (c == null) {
            hasCreds = false;
        }

        return hasCreds;
    }

    public static String getSHA512(String input) {
        String retval = "";
        try {
            MessageDigest m = MessageDigest.getInstance("SHA-512");
            byte[] out = m.digest(input.getBytes());
            retval = Base64.encodeToString(out, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return retval;
    }

    public static void addCredentials(String username, String password) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(org.koboc.collect.android.application.Collect.getAppContext());
        String serverUrl = prefs.getString(org.koboc.collect.android.preferences.PreferencesActivity.KEY_SERVER_URL, org.koboc.collect.android.application.Collect.getAppContext()
                .getResources().getString(R.string.default_server_url));

        String host = "";
        Log.d(NetUtils.class.getSimpleName(), "Server Url = " + serverUrl);
        try {
            host = new URL(URLDecoder.decode(serverUrl, "utf-8")).getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        HttpContext context = getHttpContext();
        Log.d(NetUtils.class.getSimpleName(), "Server host = " + host);
        addCredentials(username, password, host, context);
    }

    public static void addCredentials(String username, String password, HttpContext context) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(org.koboc.collect.android.application.Collect.getAppContext());
        String scheduleUrl = prefs.getString(org.koboc.collect.android.preferences.PreferencesActivity.KEY_SERVER_URL, org.koboc.collect.android.application.Collect.getAppContext()
                .getResources().getString(R.string.url));

        String host = "";

        try {
            host = new URL(URLDecoder.decode(scheduleUrl, "utf-8")).getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        addCredentials(username, password, host, context);
    }


    private static void addCredentials(String username, String password, String host, HttpContext context) {
        HttpContext localContext = context;
        Credentials c = new UsernamePasswordCredentials(username, password);
        addCredentials(localContext, c, host);

        //also add credentials for odk authentication;
        org.koboc.collect.android.utilities.WebUtils.addCredentials(username, password, host);

    }

    /*public static void refreshCredential() {
        if (User.getInstance().isLoggedin()) {
            clearAllCredentials();
            addCredentials(User.getInstance().getUserData().getUsername(), User.getInstance().getUserData()
                    .getPassword());
        }
    }
*/
    private static final void addCredentials(HttpContext localContext, Credentials c, String host) {
        CredentialsProvider credsProvider = (CredentialsProvider) localContext
                .getAttribute(ClientContext.CREDS_PROVIDER);

        AuthScope a = buildAuthScopes(host);
        credsProvider.setCredentials(a, c);
    }
	/*protected org.apache.http.conn.ssl.SSLSocketFactory createAdditionalCertsSSLSocketFactory() {
		try {
			final KeyStore ks = KeyStore.getInstance("BKS");

			// the bks file we generated above
			final InputStream in = ClientCollection.getAppContext().getResources().openRawResource( R.raw.mystore);
			try {
				// don't forget to put the password used above in strings.xml/mystore_password
				ks.load(in, ClientCollection.getAppContext().getString( R.string.mystore_password ).toCharArray());
			} finally {
				in.close();
			}

			return new AdditionalKeyStoresSSLSocketFactory(ks);

		} catch( Exception e ) {
			throw new RuntimeException(e);
		}
	}*/


    public static HttpClient createHttpClient(int timeout) {
        // configure connection
        HttpParams params = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(params, timeout);
        HttpConnectionParams.setSoTimeout(params, timeout);

        // support redirecting to handle http: => https: transition
        HttpClientParams.setRedirecting(params, true);
        // support authenticating
        HttpClientParams.setAuthenticating(params, true);
        // if possible, bias toward digest auth (may not be in 4.0 beta 2)
        List<String> authPref = new ArrayList<String>();
        authPref.add(AuthPolicy.DIGEST);
        // authPref.add(AuthPolicy.BASIC);
        params.setParameter("http.auth-target.scheme-pref", authPref);

        // setup client
        HttpClient httpclient = null;
        //TODO modified for not certified server
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
       // schemeRegistry.register(new Scheme("https", new EasySSLSocketFactory(), 443));
        ClientConnectionManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);

        httpclient = new DefaultHttpClient(cm, params);

        httpclient.getParams().setParameter(ClientPNames.MAX_REDIRECTS, 1);
        httpclient.getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);

        return httpclient;
    }

    public static HttpResponse stringResponseGet(String urlString, HttpContext localContext,
                                                 HttpClient httpclient) throws Exception {

        URL url = new URL(URLDecoder.decode(urlString, "utf-8"));
        URI u = url.toURI();

        HttpGet req = new HttpGet();
        req.setURI(u);

        HttpResponse response = null;
        response = httpclient.execute(req, localContext);

        return response;
    }


	/*public static HttpResponse stringResponsePost(String urlString, String content, HttpContext localContext,
			HttpClient httpclient) throws Exception {

		URL url = new URL(URLDecoder.decode(urlString, "utf-8"));
		URI u = url.toURI();

		HttpPost post = new HttpPost();
		post.setURI(u);

		MultipartEntity reqEntity = new MultipartEntity();
		StringBody sb = new StringBody(content, HTTP_CONTENT_TYPE_JSON, Charset.forName("UTF-8"));

		reqEntity.addPart("data", sb);
		post.setEntity(reqEntity);

		HttpResponse response = null;
		response = httpclient.execute(post, localContext);

		return response;
	}*/

    public static synchronized HttpContext getHttpContext() {
        if (localContext == null) {
            // set up one context for all HTTP requests so that authentication
            // and cookies can be retained.
            localContext = new SyncBasicHttpContext(new BasicHttpContext());

            // establish a local cookie store for this attempt at downloading...
            CookieStore cookieStore = new BasicCookieStore();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            // and establish a credentials provider. Default is 7 minutes.
            CredentialsProvider credsProvider = new BasicCredentialsProvider();
            localContext.setAttribute(ClientContext.CREDS_PROVIDER,
                    credsProvider);
        }
        return localContext;
    }

    public static boolean isConnected(Context context) {
        final ConnectivityManager conMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = conMgr.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.getState() == NetworkInfo.State.CONNECTED) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isValidUrl(String url) {

        try {
            new URL(URLDecoder.decode(url, "utf-8"));
            return true;
        } catch (MalformedURLException e) {
            return false;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }
}