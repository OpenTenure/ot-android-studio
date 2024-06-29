package org.fao.sola.clients.android.opentenure;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.fao.sola.clients.android.opentenure.model.Project;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.fragment.app.FragmentActivity;

public class SwitchProjectActivity extends FragmentActivity {
    public static final int REQUEST_CODE = 120;
    public static final int RESPONSE_CODE = 110;

    private Map<Integer,String> keyValueMap;
    private int selectedProjectKey = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_swith_projects);

        RadioGroup rbgProjects = findViewById(R.id.rbgProjects);
        Button btnOk = findViewById(R.id.btnOk);

        List<Project> projectList = Project.getProjects(true);
        Project currentProject = OpenTenureApplication.getInstance().getProject();

        if(projectList != null){
            keyValueMap = new HashMap<Integer,String>();
            int i = 1;

            for(Project project : projectList) {
                RadioButton rd = new RadioButton(this);
                rd.setText(project.getDisplayName());
                rd.setId(i);
                if(currentProject != null && currentProject.getId().equalsIgnoreCase(project.getId())) {
                    rd.setChecked(true);
                } else {
                    rd.setChecked(false);
                }
                rbgProjects.addView(rd);
                keyValueMap.put(i, project.getId());
                i+=1;
            }

            rbgProjects.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    selectedProjectKey = checkedId;
                }
            });
        }

        btnOk.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                // If project is selected and different from current one, trigger project change event
                if(selectedProjectKey > 0 && !keyValueMap.get(selectedProjectKey).equalsIgnoreCase(OpenTenureApplication.getInstance().getProject().getId())){
                    OpenTenureApplication.getInstance().setProject(keyValueMap.get(selectedProjectKey));
                    setResult(RESPONSE_CODE);
                }
                finish();
            }
        });
    }
}
