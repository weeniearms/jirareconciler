package com.infusion.jirareconciler;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by rcieslak on 21/04/2015.
 */
public class BoardFetcher {
    private static final String TAG = "BoardFetcher";
    private static final String PATH_BOARD_LIST = "rest/greenhopper/1.0/rapidviews/list";
    private static final String PATH_BOARD_DETAILS = "rest/greenhopper/1.0/xboard/work/allData.json";
    private static final String PARAM_BOARD_ID = "rapidViewId";
    private static final String JSON_VIEWS = "views";
    private final Context context;

    public BoardFetcher(Context context) {
        this.context = context;
    }

    public List<Board> fetchBoards() {
        List<Board> boards = new ArrayList<>();

        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String url = Uri.parse(preferences.getString(SettingsFragment.PREF_JIRA_URL, null))
                    .buildUpon()
                    .appendEncodedPath(PATH_BOARD_LIST)
                    .build()
                    .toString();

            String jsonString = new String(getUrlBytes(url));
            JSONObject json = (JSONObject) new JSONTokener(jsonString).nextValue();
            JSONArray array = json.getJSONArray(JSON_VIEWS);
            for (int i = 0; i < array.length(); i++) {
                boards.add(new Board(array.getJSONObject(i)));
            }
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to fetch boards", e);
        }

        return boards;
    }

    public BoardDetails fetchBoardDetails(String boardId) {
        try {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            String url = Uri.parse(preferences.getString(SettingsFragment.PREF_JIRA_URL, null))
                    .buildUpon()
                    .appendEncodedPath(PATH_BOARD_DETAILS)
                    .appendQueryParameter(PARAM_BOARD_ID, boardId)
                    .build()
                    .toString();

            String jsonString = new String(getUrlBytes(url));
            JSONObject json = (JSONObject) new JSONTokener(jsonString).nextValue();
            return new BoardDetails(json);
        } catch (IOException | JSONException e) {
            Log.e(TAG, "Failed to fetch board details", e);
            return null;
        }
    }

    private byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try {
            connection.setRequestProperty("Authorization", getUserPass());

            if (connection instanceof HttpsURLConnection) {
                acceptAllCerts((HttpsURLConnection) connection);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            InputStream inputStream = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                Log.w(TAG, "The service returned " + connection.getResponseCode());
                return null;
            }

            int bytesRead;
            byte[] buffer = new byte[1024];
            while ((bytesRead = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            return outputStream.toByteArray();
        } finally {
            connection.disconnect();
        }
    }

    private void acceptAllCerts(HttpsURLConnection connection) {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) { }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) { }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            connection.setSSLSocketFactory(sc.getSocketFactory());

            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) { return true; }
            };

            connection.setHostnameVerifier(allHostsValid);
        }
        catch (Exception e) {
            Log.w(TAG, "Failed to setup accept all certs and hostnames verifier", e);
        }
    }

    private String getUserPass() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String user = preferences.getString(SettingsFragment.PREF_JIRA_USER, null);
        String password = preferences.getString(SettingsFragment.PREF_JIRA_PASSWORD, null);
        return "Basic " + Base64.encodeToString((user + ":" + password).getBytes(), Base64.NO_WRAP);
    }
}
