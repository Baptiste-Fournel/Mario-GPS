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

                JSONObject feature = new JSONObject();
                feature.put("type", "Feature");

                JSONObject geometry = new JSONObject();
                geometry.put("type", "Point");
                geometry.put("coordinates", new JSONArray().put(x).put(y));
                feature.put("geometry", geometry);

                JSONObject properties = new JSONObject();
                properties.put("type", cell.getType().name());
                feature.put("properties", properties);

                features.put(feature);
            }
        }

        geoJson.put("features", features);
        return geoJson;
    }
}
