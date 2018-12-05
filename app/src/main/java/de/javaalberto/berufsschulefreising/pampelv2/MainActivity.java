package de.javaalberto.berufsschulefreising.pampelv2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;
import java.util.Locale;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;

import static de.javaalberto.berufsschulefreising.pampelv2.Constants.s_appCode;
import static de.javaalberto.berufsschulefreising.pampelv2.Constants.s_appID;
import static java.lang.Math.pow;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;


public class MainActivity extends AppCompatActivity {

    TextView tvRueckgabe;

    private static void disableSSLCertificateChecking() {
        TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                // Not implemented
            }
        } };

        try {
            SSLContext sc = SSLContext.getInstance("TLS");

            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (KeyManagementException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        disableSSLCertificateChecking();
        setContentView(R.layout.activity_main);
        tvRueckgabe = (TextView) findViewById(R.id.tvRueckgabe);
        Button btnAbfrageServer = (Button) findViewById(R.id.btnAbfrageServer);
        btnAbfrageServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            double s_latitude = 30.192327;
                            double s_longitude = -81.727523;
                            //https://pde.api.here.com/1/tile.json?layer=SPEED_LIMITS_FC" . (string)$functionClass . "&tilex=" . $tileX . "&tiley=" . $tileY . "&level=9&app_id={YOUR_APP_ID}&app_code={YOUR_APP_CODE}");
                            String tileUrl = String.format(
                                    Locale.ROOT,
                                    "https://reverse.geocoder.api.here.com/6.2/reversegeocode.json?prox=%f,%f,50&mode=retrieveAddresses&locationAttributes=linkInfo&gen=9&app_id=%s&app_code=%s",
                                    s_latitude,
                                    s_longitude,
                                    s_appID,
                                    s_appCode // replace those strings with your constants^^

                            );

                            HttpURLConnection connTile = (HttpURLConnection) new URL(tileUrl).openConnection();
                            BufferedReader readerTile = new BufferedReader(new InputStreamReader(connTile.getInputStream()));

                            // cuz there could be multiple lines ^^

                            StringBuilder sb = new StringBuilder();
                            String line;

                            while((line = readerTile.readLine()) != null) { sb.append(line); }

                            readerTile.close();
                            connTile.disconnect();

                            JSONObject jsonObject = new JSONObject(sb.toString());

                            final String referenceId = jsonObject
                                    .getJSONObject("Response")
                                    .getJSONArray("View")
                                    .getJSONObject(0)
                                    .getJSONArray("Result")
                                    .getJSONObject(0)
                                    .getJSONObject("Location")
                                    .getJSONObject("MapReference")
                                    .getString("ReferenceId");

                            final Integer functionClass = jsonObject
                                    .getJSONObject("Response")
                                    .getJSONArray("View")
                                    .getJSONObject(0)
                                    .getJSONArray("Result")
                                    .getJSONObject(0)
                                    .getJSONObject("Location")
                                    .getJSONObject("LinkInfo")
                                    .getInt("FunctionalClass");

                            final Integer level = 8 + functionClass;
                            final double tileSize = 180 / pow(2, level);
                            final Integer tileY = (int) ((s_latitude + 90) / tileSize);
                            final Integer tileX = (int) ((s_longitude + 180) / tileSize);

                            String speedUrl = String.format(
                                    Locale.ROOT,
                                    "https://pde.api.here.com/1/tile.json?layer=SPEED_LIMITS_FC%d&tilex=%d&tiley=%d&level=9&app_id=%s&app_code=%s",
                                    functionClass,
                                    tileX,
                                    tileY,
                                    s_appID, // don't forget about the constants :P
                                    s_appCode
                            );

                            HttpURLConnection connSpeed = (HttpURLConnection) new URL(speedUrl).openConnection();
                            final long timeStart = System.currentTimeMillis();
                            BufferedReader readerSpeed = new BufferedReader(new InputStreamReader(connSpeed.getInputStream()));
                            sb = new StringBuilder();

                            while((line = readerSpeed.readLine()) != null) { sb.append(line); }
                            final long timeEnd = System.currentTimeMillis();

                            String jsonTextSpeed = sb.toString();

                            readerSpeed.close();
                            connSpeed.disconnect();

                            JSONObject jsonObjectSpeed = new JSONObject(jsonTextSpeed);

                            final Integer pdeLength = jsonObjectSpeed.getJSONArray("Rows").length();
                            //System.out.println("pdeLength: " + pdeLength.toString());

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvRueckgabe.setText("" + TimeUnit.MILLISECONDS.toMinutes(timeEnd - timeStart));
                                }
                            });

                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        });
    }
}