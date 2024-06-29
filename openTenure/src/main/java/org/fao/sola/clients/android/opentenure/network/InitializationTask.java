package org.fao.sola.clients.android.opentenure.network;

import android.os.AsyncTask;
import android.util.Log;

import org.fao.sola.clients.android.opentenure.OpenTenureApplication;
import org.fao.sola.clients.android.opentenure.form.server.FormRetriever;
import org.fao.sola.clients.android.opentenure.model.Boundary;
import org.fao.sola.clients.android.opentenure.model.BoundaryStatus;
import org.fao.sola.clients.android.opentenure.model.BoundaryType;
import org.fao.sola.clients.android.opentenure.model.ClaimStatus;
import org.fao.sola.clients.android.opentenure.model.ClaimType;
import org.fao.sola.clients.android.opentenure.model.DocumentType;
import org.fao.sola.clients.android.opentenure.model.IdType;
import org.fao.sola.clients.android.opentenure.model.LandUse;
import org.fao.sola.clients.android.opentenure.model.Language;
import org.fao.sola.clients.android.opentenure.model.Project;
import org.fao.sola.clients.android.opentenure.network.API.CommunityServerAPI;
import org.fao.sola.clients.android.opentenure.network.response.BoundaryResponse;
import org.fao.sola.clients.android.opentenure.network.response.BoundaryStatusResponse;
import org.fao.sola.clients.android.opentenure.network.response.BoundaryTypeResponse;
import org.fao.sola.clients.android.opentenure.network.response.ClaimTypeResponse;
import org.fao.sola.clients.android.opentenure.network.response.IdTypeResponse;
import org.fao.sola.clients.android.opentenure.network.response.LandUseResponse;
import org.fao.sola.clients.android.opentenure.network.response.LanguageResponse;
import org.fao.sola.clients.android.opentenure.network.response.ProjectResponse;
import org.fao.sola.clients.android.opentenure.network.response.RefDataResponse;

import java.util.List;

public class InitializationTask extends AsyncTask<Integer, Void, Boolean> {
    public interface InitializationResponseHandler {
        void onInit(boolean result);
    }

    private InitializationResponseHandler handler = null;

    public InitializationTask(InitializationResponseHandler handler){
        this.handler = handler;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        boolean isInitialized = true;

        try {
            // Languages
            List<LanguageResponse> languages = CommunityServerAPI.getLanguages();
            if(languages == null){
                throw new Exception("No languages found");
            }

            // Claim types
            List<ClaimTypeResponse> claimTypes = CommunityServerAPI.getClaimTypes();
            if(claimTypes == null){
                throw new Exception("No claim types found");
            }

            // Doc types
            List<RefDataResponse> docTypes = CommunityServerAPI.getDocumentTypes();
            if(docTypes == null){
                throw new Exception("No document types found");
            }

            // ID types
            List<IdTypeResponse> idTypes = CommunityServerAPI.getIdTypes();
            if(idTypes == null){
                throw new Exception("No ID types found");
            }

            // Land use types
            List<LandUseResponse> landUseTypes = CommunityServerAPI.getLandUses();
            if(landUseTypes == null){
                throw new Exception("No land use types found");
            }

            // Boundary status
            List<BoundaryStatusResponse> boundaryStatuses = CommunityServerAPI.getBoundaryStatuses();
            if(boundaryStatuses == null){
                throw new Exception("No boundary statuses found");
            }

            // Boundary type
            List<BoundaryTypeResponse> boundaryTypes = CommunityServerAPI.getBoundaryTypes();
            if(boundaryTypes == null){
                throw new Exception("No boundary types found");
            }

            // Boundaries
            List<BoundaryResponse> boundaries = CommunityServerAPI.getBoundaries();

            // Projects
            List<ProjectResponse> projects = CommunityServerAPI.getProjects();
            if(projects == null){
                throw new Exception("No projects found");
            }

            // Dynamic forms
            FormRetriever formRetriever = new FormRetriever(OpenTenureApplication.getContext());
            formRetriever.retrieve();

            // Make DB updates
            Language.update(languages);
            ClaimType.update(claimTypes);
            DocumentType.update(docTypes);
            IdType.update(idTypes);
            LandUse.update(landUseTypes);
            BoundaryStatus.update(boundaryStatuses);
            BoundaryType.update(boundaryTypes);
            Project.update(projects);
            Boundary.updateBoundariesFromResponse(boundaries);

            // Set default project
            Project project = OpenTenureApplication.getInstance().getProject();
            boolean projectFound = false;

            if(project != null){
                for(ProjectResponse p : projects){
                    if(p.getId().equals(project.getId())){
                        projectFound = true;
                        // Refresh existing project
                        OpenTenureApplication.getInstance().setProject(p.getId());
                        break;
                    }
                }
            }
            if(!projectFound){
                // Select first project in the list
                OpenTenureApplication.getInstance().setProject(projects.get(0).getId());
            }
        } catch (Exception ex){
            Log.d("ApplicationInit","Failed to initialize. " + ex.getMessage());
            ex.printStackTrace();
            isInitialized = false;
        }

        return isInitialized;
    }

    @Override
    protected void onPostExecute(final Boolean isInitialized) {
        if(handler != null){
            handler.onInit(isInitialized);
        }
    }
}
