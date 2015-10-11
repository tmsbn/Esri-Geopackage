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

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;

import com.esri.android.map.Layer;
import com.esri.core.geometry.GeometryEngine;
import com.esri.core.geometry.Polygon;

import gistec.geopackage.R;

/**
 * A dialog that allows the user to turn layer visibility on and off and also zoom to layers.
 */
public class LayersDialogFragment extends DialogFragment {

    private static final String TAG = LayersDialogFragment.class.getSimpleName();

    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_layers, container, false);
        getDialog().setTitle(R.string.layers_dialog_title);

        if (getActivity() instanceof MapActivity) {
            final ListView listView_layers = (ListView) v.findViewById(R.id.listView_layers);
            final MapActivity mapActivity = (MapActivity) getActivity();

            listView_layers.setAdapter(new BaseAdapter() {
                @Override
                public int getCount() {
                    return mapActivity.getMapView().getLayers().length;
                }

                @Override
                public Object getItem(int position) {
                    return getLayer(position);
                }

                private Layer getLayer(int position) {
                    Layer[] layers = mapActivity.getMapView().getLayers();
                    return layers[layers.length - (position + 1)];
                }

                @Override
                public long getItemId(int position) {
                    return getLayer(position).getID();
                }

                @Override
                public View getView(final int position, View convertView, ViewGroup parent) {
                    View v = View.inflate(getActivity(), R.layout.layer_list_item, null);
                    CheckBox checkbox = (CheckBox) v.findViewById(R.id.checkbox_visible);
                    checkbox.setText(getLayer(position).getName());
                    checkbox.setChecked(getLayer(position).isVisible());
                    checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            getLayer(position).setVisible(isChecked);
                        }
                    });
                    Button zoomToButton = (Button) v.findViewById(R.id.button_zoomTo);
                    zoomToButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Polygon extent = getLayer(position).getExtent();
                            String json = GeometryEngine.geometryToJson(mapActivity.getMapView().getSpatialReference(), extent);

                            mapActivity.getMapView().setExtent(getLayer(position).getFullExtent());
                        }
                    });
                    return v;
                }
            });
        }

        return v;
    }

}
