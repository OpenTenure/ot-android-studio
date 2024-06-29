package org.fao.sola.clients.android.opentenure.maps;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.ipaulpro.afilechooser.FileChooserActivity;
import com.ipaulpro.afilechooser.utils.FileUtils;

import org.fao.sola.clients.android.opentenure.BuildConfig;
import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.filesystem.FileSystemUtilities;
import org.fao.sola.clients.android.opentenure.model.Configuration;
import org.fao.sola.clients.android.opentenure.model.UserLayer;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import static android.os.Build.VERSION.SDK_INT;

public class MapLayersActivity extends FragmentActivity {
    public static final int REQUEST_CODE = 13;
    public static final int RESPONSE_CODE = 26;
    private static final int REQUEST_SELECT_FILE = 655;
    private final static int REQUEST_PERMISSION_READ_EXTERNAL = 2;
    private Map<Integer,String> layerTypes;
    private String layerPath;
    private int editedLayerIndex;
    private ArrayAdapter<UserLayer> userLayersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_layers);

        layerTypes = new HashMap<Integer, String>();
        layerTypes.put(R.id.map_provider_google_normal, MainMapFragment.MapType.map_provider_google_normal.toString());
        layerTypes.put(R.id.map_provider_google_satellite, MainMapFragment.MapType.map_provider_google_satellite.toString());
        layerTypes.put(R.id.map_provider_google_hybrid, MainMapFragment.MapType.map_provider_google_hybrid.toString());
        layerTypes.put(R.id.map_provider_google_terrain, MainMapFragment.MapType.map_provider_google_terrain.toString());
        layerTypes.put(R.id.map_provider_osm_mapnik, MainMapFragment.MapType.map_provider_osm_mapnik.toString());
        layerTypes.put(R.id.map_provider_osm_mapquest, MainMapFragment.MapType.map_provider_osm_mapquest.toString());
        layerTypes.put(R.id.map_provider_local_tiles, MainMapFragment.MapType.map_provider_local_tiles.toString());
        layerTypes.put(R.id.map_provider_geoserver, MainMapFragment.MapType.map_provider_geoserver.toString());
        layerTypes.put(R.id.map_provider_empty, MainMapFragment.MapType.map_provider_empty.toString());

        Button btnAddLayer = findViewById(R.id.btnAddLayer);
        RadioGroup rbgProjects = findViewById(R.id.rbgProjects);
        ListView lstLayers = findViewById(R.id.lstLayers);

        Configuration layerTypeConf = Configuration.getConfigurationByName(MainMapFragment.MAIN_MAP_TYPE);
        int layerTypeId = R.id.map_provider_empty;;

        if(layerTypeConf != null && !StringUtility.isEmpty(layerTypeConf.getValue())){
            for (Map.Entry<Integer, String> entry: layerTypes.entrySet()) {
                if (layerTypeConf.getValue().equals(entry.getValue())) {
                    layerTypeId = entry.getKey();
                    break;
                }
            }
        }

        rbgProjects.check(layerTypeId);
        rbgProjects.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String layerType = layerTypes.get(checkedId);
                if(StringUtility.isEmpty(layerType)) {
                    layerType = MainMapFragment.MapType.map_provider_empty.toString();
                }
                setBackgroundLayer(layerType);
            }
        });

        btnAddLayer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                openFileSelection();
            }
        });

        userLayersAdapter = new UserLayersAdapter(this, UserLayer.getUserLayers(true), new UserLayersAdapter.UserListAdapterHandler(){
            @Override
            public void onDelete(int position) {
                delete(position);
            }

            @Override
            public void onEdit(int position) {
                edit(position);
            }

            @Override
            public void onMoveUp(int position) {
                moveUpDown(position, true);
            }

            @Override
            public void onMoveDown(int position) {
                moveUpDown(position, false);
            }

            @Override
            public void onCheckboxChange(int position) {
                setResult(MapLayersActivity.RESPONSE_CODE);
            }
        });

        lstLayers.setAdapter(userLayersAdapter);
    }

    private void setBackgroundLayer(String layerType) {
        Configuration mapType = Configuration.getConfigurationByName(MainMapFragment.MAIN_MAP_TYPE);
        if (mapType != null) {
            mapType.setValue(layerType);
            mapType.update();
        } else {
            mapType = new Configuration();
            mapType.setName(MainMapFragment.MAIN_MAP_TYPE);
            mapType.setValue(layerType);
            mapType.create();
        }
        setResult(MapLayersActivity.RESPONSE_CODE);
    }

    private void moveUpDown(int position, boolean up){
        UserLayer currentLayer = userLayersAdapter.getItem(position);
        UserLayer layerToSwitch = null;

        if(up && position > 0) {
            layerToSwitch = userLayersAdapter.getItem(position - 1);
        }
        if(!up && position < userLayersAdapter.getCount() - 1) {
            layerToSwitch = userLayersAdapter.getItem(position + 1);
        }

        // Switch layers
        if(layerToSwitch != null) {
            int currentOrder = currentLayer.getOrder();
            currentLayer.setOrder(layerToSwitch.getOrder());
            layerToSwitch.setOrder(currentOrder);

            if(currentLayer.update() > 0 && layerToSwitch.update() > 0){
                // Switch in the list
                int newPosition = position + 1;
                if(up){
                    newPosition = position - 1;
                }
                userLayersAdapter.remove(currentLayer);
                userLayersAdapter.insert(currentLayer, newPosition);
                userLayersAdapter.notifyDataSetChanged();
                setResult(MapLayersActivity.RESPONSE_CODE);
            }
        }
    }

    private void edit(int position){
        editedLayerIndex = position;
        Intent layerNameIntent = new Intent(getApplicationContext(), MapLayerNameActivity.class);
        layerNameIntent.putExtra("FILE_NAME", userLayersAdapter.getItem(position).getDisplayName());
        startActivityForResult(layerNameIntent, MapLayerNameActivity.REQUEST_CODE_EDITING);
    }

    private void delete(int position){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(R.string.message_confirm_delete_title);
        alertDialog.setMessage(R.string.message_confirm_delete);

        alertDialog.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                return;
            }
        });

        alertDialog.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
                UserLayer userLayer = userLayersAdapter.getItem(position);
                if(UserLayer.delete(userLayer.getId()) > 0){
                    File f = new File(userLayer.getFilePath());
                    if(f.exists()) {
                        f.delete();
                    }
                    userLayersAdapter.remove(userLayersAdapter.getItem(position));
                    userLayersAdapter.notifyDataSetChanged();
                    setResult(MapLayersActivity.RESPONSE_CODE);
                }
                return;
            }
        });
        alertDialog.show();
    }

    private void openFileSelection() {
        Intent getContentIntent = FileUtils.createGetContentIntent();
        Intent intent = Intent.createChooser(getContentIntent, getResources().getString(R.string.choose_file));
        try {
            startActivityForResult(intent, REQUEST_SELECT_FILE);
        } catch (Exception e) {
            Log.d(this.getClass().getName(), "Unable to start file chooser intent due to " + e.getMessage());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SELECT_FILE && resultCode == FileChooserActivity.RESULT_OK) {
            Uri uri = data.getData();
            try {
                // This option for copying to application's folder
                Cursor cursor =  getApplicationContext().getContentResolver().query(uri, null, null, null, null);
                int indxName = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                cursor.moveToFirst();
                String layerName = cursor.getString(indxName);
                cursor.close();

                ContentResolver contentResolver = getContentResolver();
                File folder = FileSystemUtilities.getMbTilesFolder();

                final File file = new File(folder, layerName);
                try (final InputStream inputStream = contentResolver.openInputStream(uri); OutputStream output = new FileOutputStream(file)) {
                    final byte[] buffer = new byte[4 * 1024]; // or other buffer size
                    int read;
                    while ((read = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, read);
                    }
                    output.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                layerPath = file.getAbsolutePath();

                if (layerName.lastIndexOf(".") > 0) {
                    layerName = layerName.substring(0, layerName.lastIndexOf("."));
                }

                // Assign layer name
                Intent layerNameIntent = new Intent(getApplicationContext(), MapLayerNameActivity.class);
                layerNameIntent.putExtra("FILE_NAME", layerName);
                startActivityForResult(layerNameIntent, MapLayerNameActivity.REQUEST_CODE);
            } catch (Exception ex) {
                Toast.makeText(this, R.string.message_file_selection_failed, Toast.LENGTH_LONG).show();
                Log.d("FileSelection", "Failed to select a file with the following exception: " + ex.getMessage(), ex);
                ex.printStackTrace();
            }
        }

        if (requestCode == MapLayerNameActivity.REQUEST_CODE && resultCode == MapLayerNameActivity.RESPONSE_CODE) {
            String layerName = data.getData().toString();

            // Add to DB
            UserLayer layer = new UserLayer();
            layer.setId(UUID.randomUUID().toString());
            layer.setFilePath(layerPath);
            layer.setDisplayName(layerName);
            layer.setOrder(UserLayer.getLastOrder() + 1);
            layer.setEnabled(true);
            if(layer.insert() > 0) {
                userLayersAdapter.insert(layer, userLayersAdapter.getCount());
                userLayersAdapter.notifyDataSetChanged();
                setResult(MapLayersActivity.RESPONSE_CODE);
            }
        }

        if (requestCode == MapLayerNameActivity.REQUEST_CODE_EDITING && resultCode == MapLayerNameActivity.RESPONSE_CODE) {
            String layerName = data.getData().toString();
            UserLayer layer = userLayersAdapter.getItem(editedLayerIndex);

            if(!StringUtility.empty(layerName).equals(layer.getDisplayName())){
                // Update DB
                layer.setDisplayName(layerName);
                if(layer.update() > 0){
                    userLayersAdapter.notifyDataSetChanged();
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
