package de.javaalberto.berufsschulefreising.pampelv2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;


public class MainActivity extends AppCompatActivity {

    TextView tvRueckgabe;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvRueckgabe = (TextView)findViewById(R.id.tvRueckgabe);
        Button btnAbfrageServer = (Button)findViewById(R.id.btnAbfrageServer);
        btnAbfrageServer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String s_latitude = "30.192327";
                            String s_longitude = "-81.727523";
                            //https://pde.api.here.com/1/tile.json?layer=SPEED_LIMITS_FC" . (string)$functionClass . "&tilex=" . $tileX . "&tiley=" . $tileY . "&level=9&app_id={YOUR_APP_ID}&app_code={YOUR_APP_CODE}");
                            HttpURLConnection conn = (HttpURLConnection) new URL("https://reverse.geocoder.api.here.com/6.2/reversegeocode.json?prox=" + s_latitude + "," + s_longitude + ",50&mode=retrieveAddresses&locationAttributes=linkInfo&gen=9&app_id=" + Constants.s_appID +"&app_code=" + Constants.s_appCode).openConnection();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                            //final String line = reader.readLine();
                            String jsonText = reader.readLine();

                            JSONObject jsonObject = new JSONObject(jsonText);

                            final String line = jsonObject.getJSONObject("Response").getJSONObject("MetaInfo").getString("Timestamp");

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvRueckgabe.setText(line);

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
