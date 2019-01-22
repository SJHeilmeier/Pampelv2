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

    private TextView tvRueckgabe,tvLimit;
    private ImageView ivSmiley;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Utils neueUtils = new Utils(this);

        setContentView(R.layout.activity_main);
        tvRueckgabe = findViewById(R.id.tvRueckgabe);
        tvLimit = findViewById(R.id.tvLimit);
        ivSmiley = findViewById(R.id.ivSmiley);
        disableSSLCertificateChecking();

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
                        int l_Limit = neueUtils.getCurrLimit();
                        tvRueckgabe.setText(String.format("%d km/h", value));
                        setLimitIcon(l_Limit);
                        setSpeedIcon(value,l_Limit);
                    }
            );
        });
    }

    //Rueckgabewerte
    // 0 = Genau richtig
    // 10 = zuSchnell
    // 100 = viel zuSchnell
    public int isToFast(int i_Speed, int i_Limit) {
        if (i_Speed >= 100) {
            if ((i_Speed - Math.ceil((double)i_Speed * 0.03) )> i_Limit) {
                return 100;
            } else{
                return 0;
            }
        } else if ( (i_Limit == 0) || (i_Limit == 999) ){
            return 0;
        } else {
            if ((i_Speed - 3) > i_Limit) {
                return 100;
            } else {
                return 0;
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

    public void setLimitIcon(int i_Limit) {
        if (i_Limit > 0) {
            tvLimit.setVisibility(View.VISIBLE);
            if (i_Limit > 200) {
                tvLimit.setText("");
                tvLimit.setBackgroundResource(R.drawable.nospeedsign);
            } else {
                tvLimit.setBackgroundResource(R.drawable.emptysign);
                tvLimit.setText(String.format("%d", i_Limit));
            }
        } else {
            tvLimit.setVisibility(View.INVISIBLE);
        }
    }

    public void setSpeedIcon(int i_Speed,int i_Limit) {
        if ((i_Speed != 69) && (i_Speed != 0) ){
            int l_istToFast = isToFast(i_Speed, i_Limit);
            if (l_istToFast == 100) {
                ivSmiley.setImageResource(R.drawable.smileybad);
            } else if (l_istToFast == 10) {
                ivSmiley.setImageResource(R.drawable.smileysad);
            } else if (l_istToFast == 0) {
                ivSmiley.setImageResource(R.drawable.smileyhappy);
            }
        } else if (i_Speed == 0) {
            ivSmiley.setImageResource(R.drawable.smileysleepy);
        } else if (i_Speed == 69) {
            ivSmiley.setImageResource(R.drawable.smileywink);
        } else {
            ivSmiley.setImageResource(R.drawable.smileyok);
        }
    }
}