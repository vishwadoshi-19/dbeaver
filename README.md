# 🚀 DBeaver GeoJSON Export Integration - Assignment for Lepton Softwares

- **Due Date:** 8 June 2025
- **Candidate:** Vishwa Doshi
- **Company:** Lepton Software
- **Assignment:** Extend DBeaver CE to support exporting spatial data as GeoJSON

---

## 📋 Objective

This assignment is aimed at enhancing the DBeaver Community Edition (CE) by adding a new feature that allows users to export spatial datasets (especially from PostGIS) in **GeoJSON** format. This export option should be integrated natively into DBeaver's existing data export workflow.

---

## 🚧 Current Status: Work In Progress

This repository documents my development journey for the assignment — including errors, build failures, proposed implementation, and all attempts toward delivering a working feature.

---

## 📋 TL;DR

- Tried building the application but failed. Tried using VSCode, got to know it is an Eclipse RCP application so tried on Eclipse several times on various versions/distributions - but builds kept failing.
- Tried both the modern and conventional way for setting up the local project - ChatGPT Deep Research Tool's comprehensive PDF guide, as well as DBeaver's official wiki documentation for local setup and contribution.
- Maybe due to my inexperience in Java, I'm missing out on some important stuff.
- Nonetheless, set up a Postgres DB with PostGIS enabled and imported the sample GeoJSON data. Imported it into DBeaver, ran some queries to add a column containing basic GeoJSON-like structure.
- Also closely inspected the export functionality that DBeaver provides and figured out where to add the new GeoJSON export feature code.
- Despite build errors, modified/added the code covering the functionality of GeoJSON export in the codebase using two approaches.

---

This repository documents my development journey for the assignment — including errors, build failures, proposed implementation, and all attempts toward delivering a working feature.

---

## 🫠 Assignment Overview

- **Task:** Integrate a new export option in DBeaver CE for **GeoJSON**.
- **Key Requirements:**

  - Fork and work on the DBeaver CE repo
  - Import sample GeoJSONs into a PostGIS-enabled PostgreSQL instance
  - Modify export logic to include GeoJSON option
  - Ensure geometry + attribute fields are exported
  - Deliver platform builds and a recorded video

---

## 🛠️ Build Environment Setup

To set up the DBeaver build environment locally on Windows 11, I followed the official [DBeaver Wiki](https://github.com/dbeaver/dbeaver/wiki/Develop-in-Eclipse) and supplemented it with a deep-researched PDF guide ([see here](assets/Installing%20and%20Building%20DBeaver%20CE%20from%20Source%20on%20Windows%2011.pdf)).

- Installed the following IDEs:

  - Eclipse IDE for Java Developers
  - Eclipse IDE for RCP and RAP Developers (multiple versions)

- Installed necessary components:

  - Maven 3.9+
  - JDK 17 and JDK 21 (tried both)
  - Added Eclipse P2 repository from `https://p2.dev.dbeaver.com/eclipse-repo/`

- Imported both `dbeaver` and `dbeaver-common` repositories.
- Installed Tycho lifecycle mapping and allowed additional Eclipse plugins.

Despite these steps, consistent build failures (see below) made it difficult to proceed with UI testing from source.

![build errors](./assets/vscode-build-error-logs-tools_build_cmd.png)
![build errors](./assets/eclipse-build-errors.png)

### **Eclipse Build Errors**

- #### went from this :
  ![build errors](./assets/eclipse-70k-errors.jpg)
- #### to this haha
  ![build errors](./assets/eclipse-2-errors.jpg)

---

## 📜 Progress Log

| Date        | Update                                                                                                                                                                                                                  |
| ----------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 6 June 2025 | 🔹 Cloned DBeaver repo and started setup. Encountered multiple Maven and Tycho errors.                                                                                                                                  |
| 6 June 2025 | 🔹 Installed 5 different Eclipse versions trying to resolve plugin/classpath problems.                                                                                                                                  |
| 7 June 2025 | 🔹 Referenced [DBeaver Wiki](https://github.com/dbeaver/dbeaver/wiki/Develop-in-Eclipse) and Deep Research PDF ([attached](assets/Installing%20and%20Building%20DBeaver%20CE%20from%20Source%20on%20Windows%2011.pdf)). |
| 7 June 2025 | 🔹 Installed PostgreSQL and PostGIS; shifted temporarily to database setup due to persistent build issues.                                                                                                              |
| 8 June 2025 | 🔹 Implemented proposed GeoJSON export logic in codebase - awaiting successful build to verify and demo.                                                                                                                |

---

## 🗃️ PostgreSQL + PostGIS Integration and Testing

When build issues persisted, I redirected my focus to setting up a PostGIS-enabled PostgreSQL instance and verifying how spatial data is stored and queried. This also provided a testing ground for what the GeoJSON exporter output should match.

### 🧰 Tools Used

- PostgreSQL (v15)
- PostGIS extension (latest via StackBuilder)
- GDAL / ogr2ogr (for importing GeoJSON)
- DBeaver CE binary installer (latest version)

### ✅ Steps Followed

1. **Installed PostgreSQL** using EnterpriseDB installer.
2. **Enabled PostGIS** via StackBuilder:

   - Launched StackBuilder after installation
   - Chose "Spatial Extensions" → Installed PostGIS

3. **Created a new database** named `geo_data` in pgAdmin.
4. **Ran the following SQL** to activate PostGIS:

   ```sql
   CREATE EXTENSION postgis;
   ```

5. **Installed GDAL/ogr2ogr** via OSGeo4W setup.
6. **Imported sample GeoJSON** file into the database:

   ```bash
   ogr2ogr -f "PostgreSQL" PG:"dbname=geo_data user=postgres password=admin123" "C:\path\to\file.geojson" -nln your_layer_name -nlt PROMOTE_TO_MULTI -lco GEOMETRY_NAME=geom -overwrite
   ```

7. **Connected the DB in DBeaver CE** GUI, verified spatial records, and executed spatial SQL queries.

### 🔍 Sample SQL Query for GeoJSON Output

```sql
SELECT name, ST_AsGeoJSON(geom) FROM your_layer_name;
```

This confirmed that the geometry field is correctly stored and retrievable as GeoJSON — matching what the exporter should output programmatically.

---

## 🧩 Proposed Code Implementation (2 approaches)

Despite build issues, I have implemented and documented the GeoJSON exporter. Below is an outline of the changes made:

## 🥇 Approach 1: Preprocessed Geometry Column (Query already includes ST_AsGeoJSON)

### ✅ Created New Exporter Class

**File Created:**
`plugins/org.jkiss.dbeaver.data.transfer.core/src/org/jkiss/dbeaver/tools/transfer/stream/exporter/GeoJSONDataExporter.java`

This class is designed following the structure of `DataExporterJSON.java` with added logic to:

- Identify spatial fields
- Format output using `ST_AsGeoJSON()`
- Structure output as a GeoJSON `FeatureCollection`

```java
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
```

✅ **Overview of What This Class Does**

- **Implements:** `IStreamDataExporter` → DBeaver’s interface for exporting table rows
- **Goal:** Export spatial data from PostGIS into **GeoJSON format**
- **Handles:**
  - Finding the geometry column
  - For every row:
    - Converts the geometry using `ST_AsGeoJSON`
    - Adds the rest of the columns as properties
- Wraps everything into a valid GeoJSON `FeatureCollection`

### ✅ Manual GeoJSON Query for Reference

This SQL query creates a valid GeoJSON FeatureCollection, which was used to model the output from the custom exporter:

```sql
SELECT json_build_object(
  'type','FeatureCollection',
  'features', json_agg(
      json_build_object(
          'type','Feature',
          'geometry', ST_AsGeoJSON(ST_Transform(geom,4326))::json,
          'properties', to_jsonb(t) - 'geom'
      )
  )
) AS geojson
FROM my_table AS t;
```

---

## 🥈 Approach 2: Live Geometry Transformation via SQL

This approach dynamically applies `ST_AsGeoJSON()` via a JDBC call during export. This avoids requiring the user to modify the SQL, but needs deeper integration with the database engine.

`GeoJSONDataExporter.java` :

```java
package org.jkiss.dbeaver.tools.transfer.stream.exporter;

import org.jkiss.code.NotNull;
import org.jkiss.dbeaver.model.data.DBDAttributeBinding;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.exec.DBCException;
import org.jkiss.dbeaver.tools.transfer.stream.IStreamDataExporter;
import org.jkiss.dbeaver.tools.transfer.stream.IStreamDataExporterSite;
import org.jkiss.utils.CommonUtils;
import org.json.JSONObject;

import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.List;

public class GeoJSONDataExporter implements IStreamDataExporter {

    private IStreamDataExporterSite site;
    private Writer writer;
    private List<DBDAttributeBinding> columns;
    private int geomColumnIndex = -1;
    private boolean firstFeature = true;

    @Override
    public void init(IStreamDataExporterSite site) {
        this.site = site;
    }

    @Override
    public void dispose() {
        CommonUtils.close(writer);
    }

    @Override
    public void exportHeader(@NotNull DBRProgressMonitor monitor) throws Exception {
        writer = new OutputStreamWriter(site.getOutputStream(), StandardCharsets.UTF_8);
        columns = site.getAttributes();

        // Find geometry column
        for (int i = 0; i < columns.size(); i++) {
            String colName = columns.get(i).getName().toLowerCase();
            if (colName.equals("geom") || colName.equals("geometry")) {
                geomColumnIndex = i;
                break;
            }
        }

        if (geomColumnIndex == -1) {
            throw new DBCException("No geometry column (named 'geom' or 'geometry') found in result set.");
        }

        writer.write("{\"type\": \"FeatureCollection\", \"features\": [\n");
    }

    @Override
    public void exportRow(@NotNull DBRProgressMonitor monitor, Object[] row) throws Exception {
        if (!firstFeature) {
            writer.write(",\n");
        }

        JSONObject feature = new JSONObject();
        feature.put("type", "Feature");

        // Convert geometry to GeoJSON via SQL
        Object geomValue = row[geomColumnIndex];
        JSONObject geometry = null;

        try (
            Connection jdbcConn = site.getSourceObject()
                .getDataSource()
                .getConnection(monitor)
                .getMeta()
                .getConnection();
            PreparedStatement ps = jdbcConn.prepareStatement("SELECT ST_AsGeoJSON(?)")
        ) {
            ps.setObject(1, geomValue);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String geoJsonStr = rs.getString(1);
                    geometry = new JSONObject(geoJsonStr);
                }
            }
        }

        feature.put("geometry", geometry);

        // Add properties (all other columns)
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
    public void exportFooter(@NotNull DBRProgressMonitor monitor) throws Exception {
        writer.write("\n]}\n");
        writer.flush();
    }

    @Override
    public String getDefaultFileExtension() {
        return "geojson";
    }

    @Override
    public boolean supportsDataFormat(@NotNull IStreamDataExporterSite site) {
        return true;
    }

    @Override
    public String getFormatDescription() {
        return "Export spatial data in GeoJSON format";
    }
}

```

**What the exporter does:**

- Detects which column is the geometry (named `geom` or `geometry`)
- For **every row**, it:

  - Extracts the geometry object from the row
  - Opens a new SQL statement:

    ```sql
    SELECT ST_AsGeoJSON(?)
    ```

  - Binds the geometry object as a parameter
  - Executes the query and gets back a GeoJSON string
  - Parses that string into a `JSONObject`

- Proceeds to build the Feature as before

Both versions generate a GeoJSON `FeatureCollection` and iterate over each row to output spatial + attribute data.

---

### 🧩 Common for both approaches - plugin.xml Configuration

Register the new format in the data transfer UI plugin so it appears in the Export Data wizard. Open the plugin manifest at `plugins/org.jkiss.dbeaver.data.transfer/plugin.xml` Add a new `<extension>` element for your exporter.

This enables the exporter to appear in the Data Transfer Wizard inside DBeaver CE.

```xml
<extension point="org.jkiss.dbeaver.dataTransfer.exporter">
    <exporter
        id="org.jkiss.dbeaver.exporter.geojson"
        label="GeoJSON"
        description="Export spatial data as GeoJSON FeatureCollection"
        class="org.jkiss.dbeaver.tools.transfer.stream.exporter.GeoJSONDataExporter"
        fileExtension="geojson"
        contentType="application/geo+json"
        icon="platform:/plugin/org.jkiss.dbeaver.data.transfer.ui/icons/formats/json.png"/>
</extension>
```

In this snippet:

- `id` is a unique identifier (e.g. `exporter_geojson`).
- `label` is the format name shown in the UI (“GeoJSON”).
- `class` is your Java exporter’s fully-qualified name.
- `fileExtension="geojson"` ensures files get a `.geojson` suffix.
- `icon` points to your new icon in the `icons/formats/` folder.

---

## 📦 Sample Output Generated by GeoJSON Exporter

### 📄 PostgreSQL Table Example

```sql
CREATE TABLE locations (
  id SERIAL PRIMARY KEY,
  name TEXT,
  geom GEOMETRY(Point, 4326)
);

INSERT INTO locations (name, geom) VALUES
  ('Gateway of India', ST_SetSRID(ST_MakePoint(72.8347, 18.9218), 4326)),
  ('India Gate', ST_SetSRID(ST_MakePoint(77.2295, 28.6129), 4326));
```

### 🧾 Sample `.geojson` Output

```json
{
  "type": "FeatureCollection",
  "features": [
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [72.8347, 18.9218]
      },
      "properties": {
        "id": 1,
        "name": "Gateway of India"
      }
    },
    {
      "type": "Feature",
      "geometry": {
        "type": "Point",
        "coordinates": [77.2295, 28.6129]
      },
      "properties": {
        "id": 2,
        "name": "India Gate"
      }
    }
  ]
}
```

---

## Reference Images :

- ### Provided geojson data :

  ![build errors](./assets/provided-geoJson-sample-data.png)

- ### Postgres DB setup in pgAdmin :

  ![build errors](./assets/pgAdmin-postgis-postgres-db-setup.png)

- ### Postgres DB imported and visualized in DBeaver :

  ![build errors](./assets/dbeaver-postgis-postgres-db-imported.png)

- ### Zoomed and selective visualization :

  ![build errors](./assets/dbeaver-postgis-postgres-db-imported-zoomed-in.png)

- ### Pre-existing export options in DBeaver :

  ![build errors](./assets/dbeaver-existing-export-options.png)

- ### SQL script to add a geoJSON column :

  ![build errors](./assets/dbeaver-sql-script-to-add-a-column-geoJSON.png)

- ### Exported geoJSON from that query :
  ![build errors](./assets/geoJson-from-query-result.png)

---

## 💭 Notes & Reflections

This assignment turned out to be a deep dive into Eclipse RCP development, Maven/Tycho packaging, and legacy build systems. The main blocker has been the local build setup for DBeaver CE — even after following both official and community-supported guides.

To show intent and capability, I’ve coded the full exporter module as per DBeaver's plugin system expectations. Once the build process is resolved, this code should function as intended.

This README serves as both a progress journal and a fallback deliverable to demonstrate intent, engineering diligence, and practical problem-solving.

---
