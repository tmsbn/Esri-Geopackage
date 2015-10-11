/*******************************************************************************
 * Copyright 2015 Esri
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 ******************************************************************************/
package gistec.geopackage.view;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.View;

import gistec.geopackage.R;


/**
 * An Activity that lets the user choose a spatial reference and then launches GpkgViewerActivity.
 */
public class SpatialReferenceChooserActivity extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spatial_reference_chooser);
    }

    public void launchWgs1984(View view) {
        launchGpkgViewerActivity("continent");
    }

    public void launchWebMercator(View view) {
        launchGpkgViewerActivity("continent_3857");
    }

    private void launchGpkgViewerActivity(String shapefileName) {
        Intent intent = new Intent(this, GpkgViewerActivity.class);
        intent.putExtra(GpkgViewerActivity.SHAPEFILE_NAME_EXTRA, shapefileName);
        startActivity(intent);
    }

}
