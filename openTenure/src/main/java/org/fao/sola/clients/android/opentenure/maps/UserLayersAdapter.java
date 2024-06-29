package org.fao.sola.clients.android.opentenure.maps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.fao.sola.clients.android.opentenure.R;
import org.fao.sola.clients.android.opentenure.model.UserLayer;

import java.util.List;

public class UserLayersAdapter extends ArrayAdapter<UserLayer> {
    public interface UserListAdapterHandler {
        void onDelete(int position);
        void onEdit(int position);
        void onMoveUp(int position);
        void onMoveDown(int position);
        void onCheckboxChange(int position);
    }

    private UserListAdapterHandler handler;
    private LayoutInflater inflater;
    private List<UserLayer> layers;
    private ArrayAdapter<UserLayer> that;
    private Context context;

    public UserLayersAdapter(Context context, List<UserLayer> layers, UserListAdapterHandler handler) {
        super(context, R.layout.user_layers_list_item, layers);
        this.context = context;
        this.handler = handler;
        this.layers = layers;
        this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        that = this;
    }

    @Override
    public int getCount() {
        return layers.size();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.user_layers_list_item, parent, false);
        }
        TextView lblLayerName = convertView.findViewById(R.id.lblLayerName);
        CheckBox chbxLayerEnabled = convertView.findViewById(R.id.chbxLayerEnabled);
        ImageView btnOptions = convertView.findViewById(R.id.btnOptions);

        chbxLayerEnabled.setChecked(layers.get(position).getEnabled());
        lblLayerName.setText(layers.get(position).getDisplayName());

        btnOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popup = new PopupMenu(context, v);
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.user_layer, popup.getMenu());
                Menu menuItems = popup.getMenu();

                if(position == 0){
                    // Hide move up
                    menuItems.findItem(R.id.action_up).setVisible(false);
                }

                if(position == getCount() - 1){
                    // Hide move down
                    menuItems.findItem(R.id.action_down).setVisible(false);
                }

                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()) {
                            case R.id.action_edit:
                                handler.onEdit(position);
                                return true;
                            case R.id.action_up:
                                handler.onMoveUp(position);
                                return true;
                            case R.id.action_down:
                                handler.onMoveDown(position);
                                return true;
                            case R.id.action_delete:
                                handler.onDelete(position);
                                return true;
                            default:
                                return false;
                        }
                    }
                });
                popup.show();
            }
        });

        chbxLayerEnabled.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                layers.get(position).setEnabled(((CheckBox)view).isChecked());
                layers.get(position).update();
                that.notifyDataSetChanged();
                handler.onCheckboxChange(position);
            }
        });
        return  convertView;
    }
}
