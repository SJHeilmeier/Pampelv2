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

            final String loginMessage = jsonObjectLogin
                    .getString("message");
            System.out.println(loginMessage);

            final String loginStatus = jsonObjectLogin
                    .getString("status");
            System.out.println(loginStatus);

            name = jsonObjectLogin
                    .getString("username");
            System.out.println(name);

            id = jsonObjectLogin
                    .getInt("id");
            System.out.println(id);

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

            final String loginMessage = jsonObjectLogin
                    .getString("message");
            System.out.println(loginMessage);

            final String loginStatus = jsonObjectLogin
                    .getString("status");
            System.out.println(loginStatus);

            name = jsonObjectLogin
                    .getString("username");
            System.out.println(name);

            id = jsonObjectLogin
                    .getInt("id");
            System.out.println(id);

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

    public Integer getId() {
        return id;
    }
}
