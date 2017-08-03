package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.TextTool;
import jajimenez.workpage.data.model.TaskReminder;

public class TaskReminderPickerDialogFragment extends DialogFragment {
    private TaskReminder currentTaskReminder;
    private OnTaskReminderSetListener onTaskReminderSetListener;

    public TaskReminderPickerDialogFragment() {
        onTaskReminderSetListener = null;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        ApplicationLogic applicationLogic = new ApplicationLogic(activity);

        // By default, the selected item is the task reminder with ID "4" (15 min.).
        // Note: Database IDs start at 1.
        long reminderId = (getArguments()).getLong("reminder_id", 4);
        currentTaskReminder = applicationLogic.getTaskReminder(reminderId);

        TextTool tool = new TextTool();

        // By default, the selected item is the task reminder with ID "4" (15 min.).
        // Note: Database IDs start at 1.
        int selectedItem = ((int) currentTaskReminder.getId()) - 1;;

        final List<TaskReminder> taskReminders = applicationLogic.getAllTaskReminders();
        int taskReminderCount = taskReminders.size();
        String[] taskReminderNames = new String[taskReminderCount];

        for (int i = 0; i < taskReminderCount; i++) {
            taskReminderNames[i] = tool.getTaskReminderText(activity, taskReminders.get(i));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.reminder);

        builder.setSingleChoiceItems(taskReminderNames, selectedItem, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                // "which" is the index position of the selected item.
                TaskReminder selectedTaskReminder = taskReminders.get(which);

                if (selectedTaskReminder.getId() != TaskReminderPickerDialogFragment.this.currentTaskReminder.getId()) { 
                    if (TaskReminderPickerDialogFragment.this.onTaskReminderSetListener != null) {
                        TaskReminderPickerDialogFragment.this.onTaskReminderSetListener.onTaskReminderSet(selectedTaskReminder);
                    }
                }

                // Close the dialog.
                TaskReminderPickerDialogFragment.this.dismiss();
            }
        });

        builder.setNeutralButton(R.string.no_reminder, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (TaskReminderPickerDialogFragment.this.onTaskReminderSetListener != null) {
                    TaskReminderPickerDialogFragment.this.onTaskReminderSetListener.onTaskReminderSet(null);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        return builder.create();
    }

    public void setOnTaskReminderSetListener(OnTaskReminderSetListener listener) {
        onTaskReminderSetListener = listener;
    }

    public static interface OnTaskReminderSetListener {
        void onTaskReminderSet(TaskReminder reminder);
    }
}