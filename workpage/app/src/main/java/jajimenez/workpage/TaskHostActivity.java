package jajimenez.workpage;

import android.view.ActionMode;

import java.util.List;

import jajimenez.workpage.data.model.Task;

public interface TaskHostActivity {
    void setActionMode(ActionMode mode);
    void onTaskClicked(Task task);

    void showChangeTaskStatusDialog(List<Task> tasks);
    void showEditActivity(Task task);
    void showDeleteTaskDialog(List<Task> tasks);
}
