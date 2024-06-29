package org.fao.sola.clients.android.opentenure.maps;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.tools.StringUtility;

import androidx.fragment.app.FragmentActivity;

public class MapLayerNameActivity extends FragmentActivity {
    public static final int REQUEST_CODE = 123;
    public static final int REQUEST_CODE_EDITING = 321;
    public static final int RESPONSE_CODE = 226;
    private EditText txtLayerName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.layer_name);
        setContentView(R.layout.activity_map_layer_name);

        Button btnOk = findViewById(R.id.btnOk);
        txtLayerName = findViewById(R.id.txtLayerName);

        String layerName = getIntent().getStringExtra("FILE_NAME");
        if(layerName != null){
            txtLayerName.setText(layerName);
        }

        btnOk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if(StringUtility.empty(txtLayerName.getText().toString()).trim().equals("")){
                    txtLayerName.setError(getResources().getString(R.string.enter_layer_name));
                    return;
                }
                Intent data = new Intent();
                data.setData(Uri.parse(txtLayerName.getText().toString()));
                setResult(RESPONSE_CODE, data);
                finish();
            }
        });
    }
}
