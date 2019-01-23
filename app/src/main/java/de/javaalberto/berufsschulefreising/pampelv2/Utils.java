package de.javaalberto.berufsschulefreising.pampelv2;

import android.app.Activity;
import android.arch.lifecycle.MutableLiveData;
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
    private LoginUtils mLoginUtils;
    private double currSpeed;
    private int currLimit;
    private Location currLocation;
    private GpsTracker gpsTracker;

    public MutableLiveData<Integer> getSpeed() {
        return m_speed;
    }

    private MutableLiveData<Integer> m_speed;

    public Utils(Activity activity,LoginUtils i_LoginUtils) {
        this.mActivity = activity;
        this.mLoginUtils = i_LoginUtils;
        gpsTracker = new GpsTracker(mActivity.getApplicationContext());
        enableGps();
        m_speed = new MutableLiveData<>();
        m_speed.setValue(0);
        currSpeed = 0.0;
        currLimit = 0;
    }

    @Override
    public void run() {
        int counter = 4;
        super.run();
        mLoginUtils.handleScore(0,0);
        while (true) {
            try {
                if (setCurrLocation()) {
                    counter++;

                    mActivity.runOnUiThread(() -> m_speed.setValue((int) Math.floor(currSpeed * 3.6)));

                    if (counter == 5) {
                        String tileUrl = String.format(
                                Locale.ROOT,
                                "https://reverse.geocoder.api.here.com/6.2/reversegeocode.json?prox=%f,%f,50&mode=retrieveAddresses&locationAttributes=linkInfo&gen=9&app_id=%s&app_code=%s",
                                currLocation.getLatitude(),
                                currLocation.getLongitude(),
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
                        final Integer tileY = (int) ((currLocation.getLatitude() + 90) / tileSize);
                        final Integer tileX = (int) ((currLocation.getLongitude() + 180) / tileSize);

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
                            if (jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("LINK_ID").equals(referenceId)){
                                int test = 0;
                                if (!(jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("FROM_REF_SPEED_LIMIT").equals("null")) &&
                                        (Integer.parseInt(jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("FROM_REF_SPEED_LIMIT")) > 0)) {

                                    currLimit = Integer.parseInt(jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("FROM_REF_SPEED_LIMIT"));
                                    break;
                                } else if (!(jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("TO_REF_SPEED_LIMIT").equals("null")) &&
                                        (Integer.parseInt(jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("TO_REF_SPEED_LIMIT")) > 0)) {

                                    currLimit = Integer.parseInt(jsonObjectSpeed.getJSONArray("Rows").getJSONObject(i).getString("TO_REF_SPEED_LIMIT"));
                                    break;
                                }
                            } //else if (currLimit != 0) {
                              //  break;
                             else {
                                currLimit = 0;
                            }
                        }
                        counter = 0;
                        mLoginUtils.handleScore((int) Math.floor(currSpeed * 3.6),currLimit);
                    }
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

    public boolean setCurrLocation() {
        if (gpsTracker.canGetLocation() && (gpsTracker.getLocation()!= null)) {
            currLocation = gpsTracker.getLocation();
            if (currLocation.hasSpeed()) {
                currSpeed = currLocation.getSpeed();
            }
            return true;
        } else {
            //mActivity.runOnUiThread(() -> gpsTracker.showSettingsAlert(mActivity));
            return false;
        }
    }

    public void enableGps() {
        if (!gpsTracker.canGetLocation()) {
            mActivity.runOnUiThread(() -> gpsTracker.showSettingsAlert(mActivity));
        }
    }
}
