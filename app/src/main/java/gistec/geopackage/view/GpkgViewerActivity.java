/*******************************************************************************
 * Copyright 2015 Esri
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package gistec.geopackage.view;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.esri.android.map.FeatureLayer;
import com.esri.android.map.GroupLayer;
import com.esri.android.map.Layer;
import com.esri.android.map.MapView;
import com.esri.android.map.RasterLayer;
import com.esri.android.map.event.OnStatusChangedListener;
import com.esri.android.runtime.ArcGISRuntime;
import com.esri.core.geodatabase.Geopackage;
import com.esri.core.geodatabase.GeopackageFeatureTable;
import com.esri.core.geodatabase.ShapefileFeatureTable;
import com.esri.core.geometry.Geometry;
import com.esri.core.raster.FileRasterSource;
import com.esri.core.raster.RasterSource;
import com.esri.core.renderer.RGBRenderer;
import com.esri.core.renderer.Renderer;
import com.esri.core.renderer.SimpleRenderer;
import com.esri.core.symbol.SimpleFillSymbol;
import com.esri.core.symbol.SimpleLineSymbol;
import com.esri.core.symbol.SimpleMarkerSymbol;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import gistec.geopackage.R;
import gistec.geopackage.util.FileUtils;
import gistec.geopackage.util.Utilities;
/**
 * Main activity for the app that opens Geopackages.
 */
public class GpkgViewerActivity extends AppCompatActivity implements MapActivity {

    public static final String SHAPEFILE_NAME_EXTRA = "shapefile name";

    private static final String TAG = GpkgViewerActivity.class.getSimpleName();
    private static final int[] LAYER_COLORS = new int[]{
            Color.rgb(255, 0, 0),
            Color.rgb(0, 255, 0),
            Color.rgb(0, 0, 255),
            Color.rgb(255, 255, 0),
            Color.rgb(255, 0, 255),
            Color.rgb(0, 255, 255),
    };
    private static final int REQUEST_CODE_RASTER = 1;
    private static final int REQUEST_CODE_VECTOR = 2;
    private static final RGBRenderer RGB_RENDERER = new RGBRenderer();
    private static final SimpleRenderer FILL_RENDERER = new SimpleRenderer(new SimpleFillSymbol(Color.RED));
    private static final SimpleRenderer LINE_RENDERER = new SimpleRenderer(new SimpleLineSymbol(Color.CYAN, 5f));
    private static final SimpleRenderer POINT_RENDERER = new SimpleRenderer(new SimpleMarkerSymbol(Color.BLUE, 10, SimpleMarkerSymbol.STYLE.CIRCLE));

    private GroupLayer exampleLayer = null;
    private ShapefileFeatureTable shpTable = null;
    private HashSet<RasterSource> rasterSources = new HashSet<>();
    private HashSet<Geopackage> geopackages = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpkg_viewer);

        final MapView map = getMapView();

        ArcGISRuntime.setClientId("Hu5bsEz4UDdDnsFp");

        AssetManager assets = getAssets();
        try {
            String[] strs = assets.list("");
            String shapefileName = null;
            if (null != getIntent()) {
                shapefileName = getIntent().getStringExtra(SHAPEFILE_NAME_EXTRA);
            }
            if (null == shapefileName) {
                shapefileName = getString(R.string.default_shapefile_name);
            }
            for (String str : strs) {
                if (str.startsWith(shapefileName)) {
                    Utilities.copyAsset(getAssets(), str, getFilesDir(), false);
                }
            }

            File shapefile = new File(getFilesDir(), shapefileName + ".shp");
            shpTable = new ShapefileFeatureTable(shapefile.getAbsolutePath());
            FeatureLayer shpLayer = new FeatureLayer(shpTable);
            shpLayer.setRenderer(new SimpleRenderer(new SimpleFillSymbol(Color.GREEN)));
            shpLayer.setOpacity(0.1f);
            shpLayer.setName(shapefileName + " (shapefile basemap)");
            map.addLayer(shpLayer);
        } catch (IOException e) {
            Log.e(TAG, null, e);
        }

        map.setOnStatusChangedListener(new OnStatusChangedListener() {
            @Override
            public void onStatusChanged(Object o, STATUS status) {
                if (STATUS.INITIALIZED.equals(status)) {
                    ((TextView) findViewById(R.id.textView_spatialReference)).setText("Spatial reference: " + map.getSpatialReference().getText());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (null != shpTable) {
            shpTable.dispose();
        }

        Iterator<RasterSource> rsIter = rasterSources.iterator();
        while (rsIter.hasNext()) {
            rsIter.next().dispose();
        }
        rasterSources.clear();

        Iterator<Geopackage> gpkgIter = geopackages.iterator();
        while (gpkgIter.hasNext()) {
            gpkgIter.next().dispose();
        }
        geopackages.clear();

        getMapView().removeAll();

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_gpk_viewer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id) {
            case R.id.action_layers:
                new LayersDialogFragment().show(getFragmentManager(), "LayersDialogFragment");
                return true;

            case R.id.action_addRasterGpkg:
            case R.id.action_addVectorGpkg:
                Intent intent = Intent.createChooser(FileUtils.createGetContentIntent(), "Select a Geopackage");
                startActivityForResult(intent, R.id.action_addRasterGpkg == id ? REQUEST_CODE_RASTER : REQUEST_CODE_VECTOR);
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_RASTER:
            case REQUEST_CODE_VECTOR:
                if (RESULT_OK == resultCode) {
                    Uri uri = data.getData();
                    String path = FileUtils.getPath(this, uri);

                    if (REQUEST_CODE_RASTER == requestCode) {
                        addRasterGpkg(path);
                    } else {
                        addVectorGpkg(path);
                    }
                }
        }
    }

    private void addRasterGpkg(String path) {
        try {
            MapView map = getMapView();
            FileRasterSource src = new FileRasterSource(path);
            rasterSources.add(src);
            src.project(map.getSpatialReference());
            RasterLayer layer = new RasterLayer(src);
            layer.setRenderer(RGB_RENDERER);
            layer.setName((path.contains("/") ? path.substring(path.lastIndexOf("/") + 1) : path) + " (raster)");
            map.addLayer(layer);
        } catch (FileNotFoundException e) {
            Log.d(TAG, null, e);
        }
    }

    private void addVectorGpkg(String path) {
        try {
            Geopackage gpkg = new Geopackage(path);
            geopackages.add(gpkg);
            GroupLayer groupLayer = createGroupLayerFromGeopackageFeatureClasses(gpkg);
            MapView map = getMapView();
            for (Layer layer : groupLayer.getLayers()) {
                map.addLayer(layer);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public MapView getMapView() {
        return (MapView) findViewById(R.id.map);
    }

    private static GroupLayer createGroupLayerFromGeopackageFeatureClasses(Geopackage gpkg) {
        GroupLayer groupLayer = new GroupLayer(false);
        List<GeopackageFeatureTable> tables = gpkg.getGeopackageFeatureTables();

        //First pass: polygons and unknowns
        HashSet<Geometry.Type> types = new HashSet<>();
        types.add(Geometry.Type.ENVELOPE);
        types.add(Geometry.Type.POLYGON);
        types.add(Geometry.Type.UNKNOWN);
        addTables(groupLayer, tables, types, FILL_RENDERER);

        //Second pass: lines
        types.clear();
        types.add(Geometry.Type.LINE);
        types.add(Geometry.Type.POLYLINE);
        addTables(groupLayer, tables, types, LINE_RENDERER);

        //Third pass: points
        types.clear();
        types.add(Geometry.Type.MULTIPOINT);
        types.add(Geometry.Type.POINT);
        addTables(groupLayer, tables, types, POINT_RENDERER);

        return groupLayer;
    }

    private static void addTables(GroupLayer groupLayer, List<GeopackageFeatureTable> tables, Set<Geometry.Type> types, Renderer renderer) {
        for (GeopackageFeatureTable table : tables) {
            if (types.contains(table.getGeometryType())) {
                final FeatureLayer layer = new FeatureLayer(table);
                layer.setRenderer(renderer);
                layer.setName(table.getTableName());
                groupLayer.addLayer(layer);
            }
        }
    }

}
