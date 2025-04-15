package infrastructure;

import domain.GameMap;
import domain.MapCell;
import org.json.JSONArray;
import org.json.JSONObject;

public class GeoJsonExporter {

    public static JSONObject export(GameMap map) {
        JSONObject geoJson = new JSONObject();
        geoJson.put("type", "FeatureCollection");

        JSONArray features = new JSONArray();

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                MapCell cell = map.getCell(x, y);
                features.put(createFeature(x, y, cell));
            }
        }

        geoJson.put("features", features);
        return geoJson;
    }

    private static JSONObject createFeature(int x, int y, MapCell cell) {
        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");

        JSONObject geometry = new JSONObject();
        geometry.put("type", "Point");
        geometry.put("coordinates", new JSONArray().put(x).put(y));
        feature.put("geometry", geometry);

        JSONObject properties = new JSONObject();
        properties.put("type", cell.getType().name());
        properties.put("x", x);
        properties.put("y", y);
        feature.put("properties", properties);

        return feature;
    }
}