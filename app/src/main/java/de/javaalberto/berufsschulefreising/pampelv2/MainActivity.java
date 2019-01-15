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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
//hier steht ein haufen scheiß doppelt 123
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

import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;


public class MainActivity extends AppCompatActivity {

    private GpsTracker gpsTracker;
    private TextView tvLatitude,tvLongitude;
    TextView tvRueckgabe;
    String SpeedValue;
    Integer i;

    public void getLocation() {
        gpsTracker = new GpsTracker(MainActivity.this);
        if (gpsTracker.canGetLocation()) {
            double latitude = gpsTracker.getLatitude();
            double longitude = gpsTracker.getLongitude();
            tvLatitude.setText(String.valueOf(latitude));
            tvLongitude.setText(String.valueOf(longitude));
        } else {
            gpsTracker.showSettingsAlert();
        }
    }

    public String getSpeedLimit(final double s_latitude,final double s_longitude) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String tileUrl = String.format(
                            Locale.ROOT,
                            "https://reverse.geocoder.api.here.com/6.2/reversegeocode.json?prox=%f,%f,50&mode=retrieveAddresses&locationAttributes=linkInfo&gen=9&app_id=%s&app_code=%s",
                            s_latitude,
                            s_longitude,
                            s_appID,
                            s_appCode
                    );

                    HttpURLConnection connTile = (HttpURLConnection) new URL(tileUrl).openConnection();
                    BufferedReader readerTile = new BufferedReader(new InputStreamReader(connTile.getInputStream()));

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
                            "https://pde.api.here.com/1/tile.json?layer=SPEED_LIMITS_FC%d&tilex=%d&tiley=%d&level=%d&app_id=%s&app_code=%s",
                            functionClass,
                            tileX,
                            tileY,
                            level,
                            s_appID,
                            s_appCode
                    );

                    HttpURLConnection connSpeed = (HttpURLConnection) new URL(speedUrl).openConnection();
                    //final long timeStart = System.currentTimeMillis();
                    BufferedReader readerSpeed = new BufferedReader(new InputStreamReader(connSpeed.getInputStream()));
                    sb = new StringBuilder();

                    while((line = readerSpeed.readLine()) != null) { sb.append(line); }
                    //final long timeEnd = System.currentTimeMillis();

                    String jsonTextSpeed = sb.toString();

                    readerSpeed.close();
                    connSpeed.disconnect();

                    JSONObject jsonObjectSpeed = new JSONObject(jsonTextSpeed);

                    final Integer pdeLength = jsonObjectSpeed.getJSONArray("Rows").length();

                    for(int i=1; i<pdeLength; i++){
                        if (jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("LINK_ID").equals(referenceId)){

                            String Value1 = jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("FROM_REF_SPEED_LIMIT");
                            String Value2 = jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("TO_REF_SPEED_LIMIT");

                            if (Value1 != null) {
                                if (Integer.parseInt(Value1) > 0) {
                                    SpeedValue = jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("FROM_REF_SPEED_LIMIT");
                                    break;
                                }
                            } else if (Value2 != null) {
                                if (Integer.parseInt(Value2) > 0) {
                                    SpeedValue = jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("TO_REF_SPEED_LIMIT");
                                    break;
                                }
                            }
                        }else {
                            SpeedValue = "0";
                        };
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        return SpeedValue;
    }

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
        tvLatitude = (TextView) findViewById(R.id.latitude);
        tvLongitude = (TextView) findViewById(R.id.longitude);
        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Button btnAbfrageServer = (Button)findViewById(R.id.btnAbfrageServer);
        // yo was geht
        btnAbfrageServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getLocation();

                final String l_SpeedLimit = getSpeedLimit(gpsTracker.getLatitude(),gpsTracker.getLongitude());

                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvRueckgabe.setText(l_SpeedLimit + "km/h");
                        }
                    });
            }
        });
    }
}