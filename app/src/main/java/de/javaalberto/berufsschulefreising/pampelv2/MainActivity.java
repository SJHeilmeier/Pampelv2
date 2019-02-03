package de.javaalberto.berufsschulefreising.pampelv2;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
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
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements LoginAlert.LoginAlertListener, RegistrationAlert.RegAlertListener {

    private TextView tvRueckgabe,tvLimit,tvNote;
    private ImageView ivSmiley;
    private boolean isAuthenticated;
    private LoginUtils newLoginUtils;
    private Utils neueUtils;
    private SharedPreferences sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPref = this.getPreferences(this.getApplicationContext().MODE_PRIVATE);
        isAuthenticated = sharedPref.getBoolean(getString(R.string.saved_Login), false);
        newLoginUtils = new LoginUtils();

        if (!isAuthenticated) {
            new LoginAlert().show(getSupportFragmentManager(),"Login Window");
        } else if (isAuthenticated){
            new Thread(() -> {
                if (newLoginUtils.anmelden(sharedPref.getString(getString(R.string.saved_Name), ""), sharedPref.getString(getString(R.string.saved_Password), ""))) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                        afterStart();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                        new LoginAlert().show(getSupportFragmentManager(),"Login Window");
                    });
                }
            }).start();
        }



        tvRueckgabe = findViewById(R.id.tvRueckgabe);
        tvLimit = findViewById(R.id.tvLimit);
        tvNote = findViewById(R.id.tvNote);
        ivSmiley = findViewById(R.id.ivSmiley);
        disableSSLCertificateChecking();

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void afterStart() {
        neueUtils = new Utils(this,newLoginUtils);
        neueUtils.start();
        neueUtils.getSpeed().observe(this, (value) -> {
            runOnUiThread(() -> {
                        int l_Limit = neueUtils.getCurrLimit();
                        tvRueckgabe.setText(String.format("%d km/h", value));
                        setLimitIcon(l_Limit);
                        setSpeedIcon(value,l_Limit);
                        setHighscore();
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

    public void setHighscore() {
        tvNote.setText(String.format(
                "1st: %s %d \n" + "2nd: %s %d \n" + "3rd: %s %d \n" + getString(R.string.score) + " %d",
                newLoginUtils.getFirstName(),newLoginUtils.getFirstScore(),
                newLoginUtils.getSecondName(),newLoginUtils.getSecondScore(),
                newLoginUtils.getThirdName(),newLoginUtils.getThirdScore(),
                newLoginUtils.getScore()));
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

        if (i_Speed < 3) {
            tvLimit.setVisibility(View.INVISIBLE);
            tvRueckgabe.setVisibility(View.INVISIBLE);
            tvNote.setVisibility(View.VISIBLE);
        } else {
            tvLimit.setVisibility(View.VISIBLE);
            tvRueckgabe.setVisibility(View.VISIBLE);
            tvNote.setVisibility(View.INVISIBLE);
        }

        if ((i_Speed != 69) && (i_Speed > 3) ){
            int l_istToFast = isToFast(i_Speed, i_Limit);
            if (l_istToFast == 100) {
                ivSmiley.setImageResource(R.drawable.smileybad);
            } else if (l_istToFast == 10) {
                ivSmiley.setImageResource(R.drawable.smileysad);
            } else if (l_istToFast == 0) {
                ivSmiley.setImageResource(R.drawable.smileyhappy);
            }
        } else if (i_Speed < 3) {
            ivSmiley.setImageResource(R.drawable.smileysleepy);
        } else if (i_Speed == 69) {
            ivSmiley.setImageResource(R.drawable.smileywink);
        } else {
            ivSmiley.setImageResource(R.drawable.smileyok);
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


    @Override
    public void onRegListenerClicked(DialogFragment dialog) {
        final EditText l_Name = dialog.getDialog().findViewById(R.id.edtName);
        final EditText l_Pass = dialog.getDialog().findViewById(R.id.edtPass);
        final EditText l_PassConf = dialog.getDialog().findViewById(R.id.edtPassConf);

        if (!l_Name.getText().toString().isEmpty()
                    && !l_Pass.getText().toString().isEmpty()
                    && l_Pass.getText().toString().equals(l_PassConf.getText().toString())) {
            new Thread(() -> {
                if (newLoginUtils.registrieren(l_Name.getText().toString(), l_Pass.getText().toString())) {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, getString(R.string.regist_success), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, getString(R.string.regist_failed), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } else {
            Toast.makeText(MainActivity.this, getString(R.string.regist_failed), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoginRegisterListenerClicked(DialogFragment dialog) {
        new RegistrationAlert().show(getSupportFragmentManager(),"Login Window");
    }

    @Override
    public void onLoginListenerClicked(DialogFragment dialog) {

        //final LoginUtils newLoginUtils = new LoginUtils();
        final EditText l_Name = dialog.getDialog().findViewById(R.id.edtName);
        final EditText l_Pass = dialog.getDialog().findViewById(R.id.edtPass);

        if (!l_Name.getText().toString().isEmpty() && !l_Pass.getText().toString().isEmpty()) {
            new Thread(() -> {
                if (newLoginUtils.anmelden(l_Name.getText().toString(), l_Pass.getText().toString())) {
                    runOnUiThread(() -> {

                        //Speichert Name und Passwort
                        //--------------------------------------------------------------------------
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putBoolean(getString(R.string.saved_Login), true);
                        editor.putString(getString(R.string.saved_Name), l_Name.getText().toString());
                        editor.putString(getString(R.string.saved_Password), l_Pass.getText().toString());
                        editor.commit();
                        //--------------------------------------------------------------------------

                        Toast.makeText(MainActivity.this, getString(R.string.login_success), Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        afterStart();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(MainActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
                    });
                }
            }).start();
        } else {
                Toast.makeText(MainActivity.this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show();
        }
    }
}