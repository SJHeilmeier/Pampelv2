package de.javaalberto.berufsschulefreising.pampelv2;

import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Locale;

public class LoginUtils {

    private String name;
    private Integer id;
    private Integer score;
    private String firstName;
    private String secondName;
    private String thirdName;
    private Integer firstScore;
    private Integer secondScore;
    private Integer thirdScore;

    public Integer handleScore(Integer currSpeed, Integer currLimit) {
        try {
            String scoreUrl = String.format(
                    Locale.ROOT,
                    "https://pampelv2.dev-juicy-roots.com/api/users/score.php?username=%s&currSpeed=%d&currLimit=%d",
                    name,
                    currSpeed,
                    currLimit
            );

            HttpURLConnection connScore = (HttpURLConnection) new URL(scoreUrl).openConnection();
            BufferedReader readerScore = null;

            readerScore = new BufferedReader(new InputStreamReader(connScore.getInputStream()));

            StringBuilder sbScore = new StringBuilder();
            String lineScore;

            while ((lineScore = readerScore.readLine()) != null) {
                sbScore.append(lineScore);
            }

            readerScore.close();
            connScore.disconnect();

            JSONObject jsonObjectScore = new JSONObject(sbScore.toString());

            final String loginMessage = jsonObjectScore.getString("message");
            final String loginStatus = jsonObjectScore.getString("status");

            score = jsonObjectScore.getInt("score");

            //erster Platz
            firstName = jsonObjectScore
                    .getJSONObject("first")
                    .getString("username");

            firstScore = jsonObjectScore
                    .getJSONObject("first")
                    .getInt("score");

            //zweiter Platz
            secondName = jsonObjectScore
                    .getJSONObject("second")
                    .getString("username");

            secondScore = jsonObjectScore
                    .getJSONObject("second")
                    .getInt("score");

            //dritter Platz
            thirdName = jsonObjectScore
                    .getJSONObject("third")
                    .getString("username");

            thirdScore = jsonObjectScore
                    .getJSONObject("third")
                    .getInt("score");

            if (loginStatus.equals("true")) {
                return score;
            } else return -1;

        } catch (IOException e) {
            e.printStackTrace();
            return -1;
        } catch (JSONException e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean anmelden(String username, String password) {
        try {
            String loginUrl = String.format(
                Locale.ROOT,
                "https://pampelv2.dev-juicy-roots.com/api/users/login.php?username=%s&password=%s",
                username,
                password
            );

            HttpURLConnection connLogin = (HttpURLConnection) new URL(loginUrl).openConnection();
            BufferedReader readerLogin = null;

            readerLogin = new BufferedReader(new InputStreamReader(connLogin.getInputStream()));

            StringBuilder sbLogin = new StringBuilder();
            String lineLogin;

            while ((lineLogin = readerLogin.readLine()) != null) {
                sbLogin.append(lineLogin);
            }

            readerLogin.close();
            connLogin.disconnect();

            JSONObject jsonObjectLogin = new JSONObject(sbLogin.toString());

            final String loginMessage = jsonObjectLogin.getString("message");
            final String loginStatus = jsonObjectLogin.getString("status");

            name = jsonObjectLogin.getString("username");
            id = jsonObjectLogin.getInt("id");

            if (loginStatus.equals("true")) {
                return true;
            } else return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }


    public boolean registrieren(String username, String password) {
        try {
            String loginUrl = String.format(
                    Locale.ROOT,
                    "https://pampelv2.dev-juicy-roots.com/api/users/signup.php?username=%s&password=%s",
                    username,
                    password
            );

            HttpURLConnection connLogin = (HttpURLConnection) new URL(loginUrl).openConnection();
            BufferedReader readerLogin = null;

            readerLogin = new BufferedReader(new InputStreamReader(connLogin.getInputStream()));

            StringBuilder sbLogin = new StringBuilder();
            String lineLogin;

            while ((lineLogin = readerLogin.readLine()) != null) {
                sbLogin.append(lineLogin);
            }

            readerLogin.close();
            connLogin.disconnect();

            JSONObject jsonObjectLogin = new JSONObject(sbLogin.toString());

            final String loginMessage = jsonObjectLogin.getString("message");
            final String loginStatus = jsonObjectLogin.getString("status");

            name = jsonObjectLogin.getString("username");
            id = jsonObjectLogin.getInt("id");

            if (loginStatus.equals("true")) {
                return true;
            } else return false;

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getName() {
        return name;
    }

    public Integer getScore() {
        return score;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public String getThirdName() {
        return thirdName;
    }

    public Integer getFirstScore() {
        return firstScore;
    }

    public Integer getSecondScore() { return secondScore; }

    public Integer getThirdScore() { return thirdScore; }
}
