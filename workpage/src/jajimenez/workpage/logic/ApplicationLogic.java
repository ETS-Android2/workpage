package jajimenez.workpage.logic;

import java.util.List;

import android.content.Context;

import jajimenez.workpage.data.DataManager;
import jajimenez.workpage.data.model.Workspace;

public class ApplicationLogic {
    private Context context;

    public ApplicationLogic(Context context) {
        this.context = context;
    }

    public List<Workspace> getAllWorspaces() {
        return (new DataManager(context)).getAllWorkspaces();
    }
}
