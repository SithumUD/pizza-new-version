package com.sithum.pizzaapp.activities.utils;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

public class OpenRouteServiceAPI {
    private static final String TAG = "OpenRouteServiceAPI";

    // You need to get your free API key from https://openrouteservice.org/
    private static final String API_KEY = "eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImNlMjRlNTJhODFmNjQ0NTZhNWQ2YWY3ZjlmMDY3Yjc3IiwiaCI6Im11cm11cjY0In0=";

    // OpenRouteService endpoints
    private static final String GEOCODE_URL = "https://api.openrouteservice.org/geocode/search";
    private static final String DIRECTIONS_URL = "https://api.openrouteservice.org/v2/directions/driving-car";

    // Fallback to Nominatim (no API key required) if OpenRouteService fails
    private static final String NOMINATIM_URL = "https://nominatim.openstreetmap.org/search";

    public interface GeocodeCallback {
        void onSuccess(double latitude, double longitude);
        void onError(String error);
    }

    public interface RouteCallback {
        void onSuccess(List<GeoPoint> routePoints, double distanceKm, int durationMinutes);
        void onError(String error);
    }

    public void geocodeAddress(String address, GeocodeCallback callback) {
        new GeocodeTask(callback).execute(address);
    }

    public void getRoute(GeoPoint start, GeoPoint end, RouteCallback callback) {
        new RouteTask(callback).execute(start, end);
    }

    private class GeocodeTask extends AsyncTask<String, Void, GeocodeResult> {
        private GeocodeCallback callback;

        public GeocodeTask(GeocodeCallback callback) {
            this.callback = callback;
        }

        @Override
        protected GeocodeResult doInBackground(String... addresses) {
            String address = addresses[0];

            // Try OpenRouteService first if API key is available
            if (!API_KEY.equals("eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImNlMjRlNTJhODFmNjQ0NTZhNWQ2YWY3ZjlmMDY3Yjc3IiwiaCI6Im11cm11cjY0In0=")) {
                GeocodeResult result = geocodeWithOpenRouteService(address);
                if (result != null && result.success) {
                    return result;
                }
            }

            // Fallback to Nominatim
            return geocodeWithNominatim(address);
        }

        @Override
        protected void onPostExecute(GeocodeResult result) {
            if (result != null && result.success) {
                callback.onSuccess(result.latitude, result.longitude);
            } else {
                String error = result != null ? result.error : "Unknown geocoding error";
                callback.onError(error);
            }
        }
    }

    private class RouteTask extends AsyncTask<GeoPoint, Void, RouteResult> {
        private RouteCallback callback;

        public RouteTask(RouteCallback callback) {
            this.callback = callback;
        }

        @Override
        protected RouteResult doInBackground(GeoPoint... points) {
            GeoPoint start = points[0];
            GeoPoint end = points[1];

            // Try OpenRouteService if API key is available
            if (!API_KEY.equals("eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImNlMjRlNTJhODFmNjQ0NTZhNWQ2YWY3ZjlmMDY3Yjc3IiwiaCI6Im11cm11cjY0In0=")) {
                RouteResult result = getRouteFromOpenRouteService(start, end);
                if (result != null && result.success) {
                    return result;
                }
            }

            // Fallback: create simple direct route
            return createDirectRoute(start, end);
        }

        @Override
        protected void onPostExecute(RouteResult result) {
            if (result != null && result.success) {
                callback.onSuccess(result.routePoints, result.distanceKm, result.durationMinutes);
            } else {
                String error = result != null ? result.error : "Unknown routing error";
                callback.onError(error);
            }
        }
    }

    private GeocodeResult geocodeWithOpenRouteService(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, "UTF-8");
            String urlString = GEOCODE_URL + "?api_key=" + API_KEY + "&text=" + encodedAddress + "&boundary.country=LK&size=1";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray features = jsonResponse.getJSONArray("features");

                if (features.length() > 0) {
                    JSONObject firstFeature = features.getJSONObject(0);
                    JSONObject geometry = firstFeature.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");

                    double longitude = coordinates.getDouble(0);
                    double latitude = coordinates.getDouble(1);

                    Log.d(TAG, "OpenRouteService geocoded: " + address + " -> " + latitude + ", " + longitude);
                    return new GeocodeResult(true, latitude, longitude, null);
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "OpenRouteService geocoding error", e);
        }

        return new GeocodeResult(false, 0, 0, "OpenRouteService geocoding failed");
    }

    private GeocodeResult geocodeWithNominatim(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address + ", Sri Lanka", "UTF-8");
            String urlString = NOMINATIM_URL + "?q=" + encodedAddress + "&format=json&limit=1";

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "PizzaApp/1.0");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    JSONObject firstResult = jsonArray.getJSONObject(0);
                    double latitude = firstResult.getDouble("lat");
                    double longitude = firstResult.getDouble("lon");

                    Log.d(TAG, "Nominatim geocoded: " + address + " -> " + latitude + ", " + longitude);
                    return new GeocodeResult(true, latitude, longitude, null);
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Nominatim geocoding error", e);
        }

        return new GeocodeResult(false, 0, 0, "Geocoding failed");
    }

    private RouteResult getRouteFromOpenRouteService(GeoPoint start, GeoPoint end) {
        try {
            String urlString = DIRECTIONS_URL + "?api_key=" + API_KEY +
                    "&start=" + start.getLongitude() + "," + start.getLatitude() +
                    "&end=" + end.getLongitude() + "," + end.getLatitude();

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonResponse = new JSONObject(response.toString());
                JSONArray features = jsonResponse.getJSONArray("features");

                if (features.length() > 0) {
                    JSONObject firstFeature = features.getJSONObject(0);
                    JSONObject geometry = firstFeature.getJSONObject("geometry");
                    JSONArray coordinates = geometry.getJSONArray("coordinates");

                    List<GeoPoint> routePoints = new ArrayList<>();
                    for (int i = 0; i < coordinates.length(); i++) {
                        JSONArray point = coordinates.getJSONArray(i);
                        double longitude = point.getDouble(0);
                        double latitude = point.getDouble(1);
                        routePoints.add(new GeoPoint(latitude, longitude));
                    }

                    // Get route summary
                    JSONObject properties = firstFeature.getJSONObject("properties");
                    JSONArray segments = properties.getJSONArray("segments");
                    JSONObject summary = segments.getJSONObject(0).getJSONObject("summary");

                    double distanceKm = summary.getDouble("distance") / 1000.0;
                    int durationMinutes = (int) (summary.getDouble("duration") / 60.0);

                    Log.d(TAG, "OpenRouteService route: " + routePoints.size() + " points, " +
                            distanceKm + "km, " + durationMinutes + " minutes");

                    return new RouteResult(true, routePoints, distanceKm, durationMinutes, null);
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "OpenRouteService routing error", e);
        }

        return new RouteResult(false, null, 0, 0, "OpenRouteService routing failed");
    }

    private RouteResult createDirectRoute(GeoPoint start, GeoPoint end) {
        try {
            List<GeoPoint> routePoints = new ArrayList<>();
            routePoints.add(start);
            routePoints.add(end);

            // Calculate approximate distance and duration
            double distanceKm = start.distanceToAsDouble(end) / 1000.0;
            int durationMinutes = (int) (distanceKm / 40.0 * 60); // Assume 40 km/h average speed

            Log.d(TAG, "Direct route: " + distanceKm + "km, " + durationMinutes + " minutes");

            return new RouteResult(true, routePoints, distanceKm, durationMinutes, null);
        } catch (Exception e) {
            Log.e(TAG, "Direct route creation error", e);
            return new RouteResult(false, null, 0, 0, "Failed to create direct route");
        }
    }

    // Helper classes
    private static class GeocodeResult {
        boolean success;
        double latitude;
        double longitude;
        String error;

        GeocodeResult(boolean success, double latitude, double longitude, String error) {
            this.success = success;
            this.latitude = latitude;
            this.longitude = longitude;
            this.error = error;
        }
    }

    private static class RouteResult {
        boolean success;
        List<GeoPoint> routePoints;
        double distanceKm;
        int durationMinutes;
        String error;

        RouteResult(boolean success, List<GeoPoint> routePoints, double distanceKm, int durationMinutes, String error) {
            this.success = success;
            this.routePoints = routePoints;
            this.distanceKm = distanceKm;
            this.durationMinutes = durationMinutes;
            this.error = error;
        }
    }
}