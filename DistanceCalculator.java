import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class DistanceCalculator {
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // Validate that coordinates are not at (0, 0)
        if ((lat1 == 0.0 && lon1 == 0.0) || (lat2 == 0.0 && lon2 == 0.0)) {
            System.out.println("Error: Invalid coordinates provided for distance calculation.");
            return -1;
        }
        try {
            // OSRM API URL with coordinates for start and destination points
            String urlString = String.format(
                "http://router.project-osrm.org/route/v1/driving/%f,%f;%f,%f?overview=false",
                lon1, lat1, lon2, lat2
            );

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String inputLine;

            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();

            // Parse the JSON response to get the distance in meters
            JSONObject jsonResponse = new JSONObject(response.toString());
            JSONArray routes = jsonResponse.getJSONArray("routes");
            double distanceInMeters = routes.getJSONObject(0).getDouble("distance");
            return distanceInMeters / 1000.0; // Convert to kilometers

        } catch (Exception e) {
            e.printStackTrace();
            return -1; // Return -1 in case of error
        }
    }
}
