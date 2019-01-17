package de.javaalberto.berufsschulefreising.pampelv2;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.location.Location;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import static de.javaalberto.berufsschulefreising.pampelv2.Constants.s_appCode;
import static de.javaalberto.berufsschulefreising.pampelv2.Constants.s_appID;
import static java.lang.Math.pow;

public class Utils extends Thread{
    private Activity mActivity;
    private double currSpeed;
    private int currLimit;
    private Location lastLocation;
    private Location currLocation;
    private double currLatitude;
    private double currLongitude;
    private GpsTracker gpsTracker;

    public MutableLiveData<Integer> getSpeed() {
        return m_speed;
    }

    private MutableLiveData<Integer> m_speed;

    public Utils(Activity activity) {
        this.mActivity = activity;
        gpsTracker = new GpsTracker(mActivity.getApplicationContext());
        m_speed = new MutableLiveData<>();
        m_speed.setValue(0);
        currSpeed = 0.0;
        currLimit = 0;
        currLatitude = 0.0;
        currLongitude =  0.0;
    }

    @Override
    public void run() {
        int counter = 4;
        super.run();
        while (true) {
            try {
                counter++;
                setCurrLocation();
                if ( lastLocation != null) {
                    currSpeed = Math.sqrt(
                            Math.pow(currLocation.getLongitude() - lastLocation.getLongitude(), 2)
                                    + Math.pow(currLocation.getLatitude() - lastLocation.getLatitude(), 2)
                    ) / (currLocation.getTime() - lastLocation.getTime());
                }

                lastLocation = currLocation;
                mActivity.runOnUiThread(() -> m_speed.setValue((int) Math.floor(currSpeed * 3.6)));

                if (counter == 5) {
                    String tileUrl = String.format(
                            Locale.ROOT,
                            "https://reverse.geocoder.api.here.com/6.2/reversegeocode.json?prox=%f,%f,50&mode=retrieveAddresses&locationAttributes=linkInfo&gen=9&app_id=%s&app_code=%s",
                            currLatitude,
                            currLongitude,
                            s_appID,
                            s_appCode
                    );

                    HttpURLConnection connTile = (HttpURLConnection) new URL(tileUrl).openConnection();
                    BufferedReader readerTile = new BufferedReader(new InputStreamReader(connTile.getInputStream()));

                    StringBuilder sb = new StringBuilder();
                    String line;

                    while ((line = readerTile.readLine()) != null) {
                        sb.append(line);
                    }

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
                    final Integer tileY = (int) ((currLatitude + 90) / tileSize);
                    final Integer tileX = (int) ((currLongitude + 180) / tileSize);

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

                    while ((line = readerSpeed.readLine()) != null) {
                        sb.append(line);
                    }
                    //final long timeEnd = System.currentTimeMillis();

                    String jsonTextSpeed = sb.toString();

                    readerSpeed.close();
                    connSpeed.disconnect();

                    JSONObject jsonObjectSpeed = new JSONObject(jsonTextSpeed);

                    final Integer pdeLength = jsonObjectSpeed.getJSONArray("Rows").length();

                    for (int i = 1; i < pdeLength; i++) {
                        if (jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("LINK_ID").equals(referenceId)) {

                            String Value1 = jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("FROM_REF_SPEED_LIMIT");
                            String Value2 = jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("TO_REF_SPEED_LIMIT");

                            if (Value1 != null) {
                                if (Integer.parseInt(Value1) > 0) {
                                    currLimit = Integer.parseInt(jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("FROM_REF_SPEED_LIMIT"));
                                    break;
                                }
                            } else if (Value2 != null) {
                                if (Integer.parseInt(Value2) > 0) {
                                    currLimit = Integer.parseInt(jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("TO_REF_SPEED_LIMIT"));
                                    break;
                                }
                            }
                        } else {
                            currLimit = 0;
                        }
                        ;
                    }
                    counter = 0;
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public int getCurrLimit() {
        return currLimit;
    }

    public void setCurrLocation() {
        currLocation = gpsTracker.getLocation();
       if (gpsTracker.canGetLocation()) {

           currLatitude = currLocation.getLatitude();
           currLongitude = currLocation.getLongitude();
        } else {
            gpsTracker.showSettingsAlert();
        }
    }
}
