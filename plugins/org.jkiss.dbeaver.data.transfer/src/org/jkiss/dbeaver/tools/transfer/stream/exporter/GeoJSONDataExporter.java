package org.jkiss.dbeaver.tools.transfer.stream.exporter;

import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.model.data.DBDAttributeType;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.tools.transfer.DTUtils;
import org.jkiss.dbeaver.tools.transfer.stream.IStreamDataExporter;
import org.jkiss.dbeaver.tools.transfer.stream.IStreamDataExporterSite;
import org.jkiss.utils.CommonUtils;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class GeoJSONDataExporter implements IStreamDataExporter {

    private IStreamDataExporterSite site;
    private Writer writer;
    private boolean firstFeature = true;
    private int geomColumnIndex = -1;
    private List<DBDAttributeBinding> columns;

    @Override
    public void init(IStreamDataExporterSite site) {
        this.site = site;
    }

    @Override
    public void dispose() {
        CommonUtils.close(writer);
    }

    @Override
    public void exportHeader(DBRProgressMonitor monitor) throws Exception {
        writer = new OutputStreamWriter(site.getOutputStream(), StandardCharsets.UTF_8);
        columns = site.getAttributes();

        // Find geometry column (named "geom" or "geometry")
        for (int i = 0; i < columns.size(); i++) {
            String name = columns.get(i).getName().toLowerCase();
            if (name.equals("geom") || name.equals("geometry")) {
                geomColumnIndex = i;
                break;
            }
        }

        writer.write("{\"type\": \"FeatureCollection\", \"features\": [\n");
    }

    @Override
    public void exportRow(DBRProgressMonitor monitor, Object[] row) throws Exception {
        if (!firstFeature) {
            writer.write(",\n");
        }

        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");

        // Get geometry JSON string
        Object geomValue = row[geomColumnIndex];
        JSONObject geometry = new JSONObject(geomValue.toString());
        feature.put("geometry", geometry);

        // Add properties
        JSONObject properties = new JSONObject();
        for (int i = 0; i < columns.size(); i++) {
            if (i == geomColumnIndex) continue;
            String colName = columns.get(i).getName();
            Object val = row[i];
            properties.put(colName, val != null ? val : JSONObject.NULL);
        }

        feature.put("properties", properties);

        writer.write(feature.toString());

        firstFeature = false;
    }

    @Override
    public void exportFooter(DBRProgressMonitor monitor) throws Exception {
        writer.write("\n]}\n");
        writer.flush();
    }

    @Override
    public String getDefaultFileExtension() {
        return "geojson";
    }

    @Override
    public boolean supportsDataFormat(IStreamDataExporterSite site) {
        return true; // supports all text output
    }

    @Override
    public String getFormatDescription() {
        return "Export spatial data in GeoJSON format";
    }
}


// This assumes the input query already calls ST_AsGeoJSON(geom) AS geom so that the geometry comes as a ready-to-parse JSON string.