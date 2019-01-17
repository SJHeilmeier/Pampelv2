package de.javaalberto.berufsschulefreising.pampelv2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

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

    private TextView tvLatitude,tvLongitude,tvRueckgabe,tvSpeed;
    Integer i;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Utils neueUtils = new Utils(this);

        setContentView(R.layout.activity_main);
        tvRueckgabe = findViewById(R.id.tvRueckgabe);
        final ImageView ivSmiley = findViewById(R.id.ivSmiley);
        disableSSLCertificateChecking();
        i = 0;

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        neueUtils.start();
        neueUtils.getSpeed().observe(this, (value) -> {
            runOnUiThread(() -> {

                        int i_Limit = neueUtils.getCurrLimit();
                        tvRueckgabe.setText(String.format("%d km/h | %d km/h | %d", value, i_Limit, i++));

                        if (isToFast(value, i_Limit)) {
                            ivSmiley.setImageResource(R.drawable.smileybad);
                        } else {
                            ivSmiley.setImageResource(R.drawable.smileyhappy);
                        }
                    }
            );
        });
    }

    public void setKMH(double kmh) {
        tvSpeed.setText(String.format("%d km/h", kmh));
    }
        /*btnAbfrageServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        i++;

                        tvLatitude.setText(String.valueOf(neueUtils.getCurrLatitude()));
                        tvLongitude.setText(String.valueOf(neueUtils.getCurrLongitude()));
                        tvRueckgabe.setText(String.format("%d km/h %d",neueUtils.getCurrLimit(),i));

                        if (neueUtils.isToFast()) {
                            ivSmiley.setImageResource(R.drawable.smileybad);
                        }else {
                            ivSmiley.setImageResource(R.drawable.smileyhappy);
                        }
                    }
                });
            }
        });*/

    public boolean isToFast(int i_Speed, int i_Limit) {
        if (i_Speed >= 100) {
            if ((53 - Math.ceil((double)i_Speed * 0.03) )> i_Limit) {
                return true;
            } else{
                return false;
            }
        } else if (i_Limit == 0){
            return false;
        } else {
            if ((i_Speed - 3) > i_Limit) {
                return true;
            } else{
                return false;
            }
        }
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
}