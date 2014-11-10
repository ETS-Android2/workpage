package jajimenez.workpage.logic;

import java.util.List;

import android.content.Context;

import jajimenez.workpage.data.DataManager;
import jajimenez.workpage.data.model.TaskContext;

public class ApplicationLogic {
    private Context context;

    public ApplicationLogic(Context context) {
        this.context = context;
    }

    public List<TaskContext> getAllTaskContexts() {
        return (new DataManager(context)).getAllTaskContexts();
    }
}
