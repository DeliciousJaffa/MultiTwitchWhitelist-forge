package cat.jaffa.multitwitchwhitelist.forge;

import com.google.gson.Gson;
import net.minecraft.entity.player.EntityPlayerMP;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Jordan on 05/07/2017.
 */
public class WhitelistDataCreator {
    private static WhitelistData fromURL(URL url) throws IOException {
        Gson gson = new Gson();
        /*
        InputStreamReader reader = new InputStreamReader(url.openStream());
        return gson.fromJson(reader,WhitelistData.class);
        */


        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "MTWL-Forge-Mod V"+MultiTwitchWhitelist.VERSION);
        con.setRequestProperty("api-client", MultiTwitchWhitelist.ClientID);
        con.setRequestProperty("api-secret", MultiTwitchWhitelist.ClientSecret);

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";
        //wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();
        int statusCode = con.getResponseCode();
        BufferedReader in;
        if (statusCode >= 200 && statusCode < 400) {
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        } else {
            in = new BufferedReader(new InputStreamReader(con.getErrorStream()));
        }
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        WhitelistData data = gson.fromJson(response.toString(),WhitelistData.class);
        data.setStatusCode(statusCode);
        return data;

    }
    public static WhitelistData fromUser(EntityPlayerMP p) {
        try {
            return fromURL(new URL(MultiTwitchWhitelist.getApiURL()+"/login/"+p.getUniqueID()));
        } catch (IOException e) {
            MultiTwitchWhitelist.log.warn(String.format(
                    "Error getting details for user %s(%s): %s (%s)",
                    p.getName(),p.getUniqueID(),e.getClass().getName(),e.getMessage())
            );
            return null;
        }
    }
}
