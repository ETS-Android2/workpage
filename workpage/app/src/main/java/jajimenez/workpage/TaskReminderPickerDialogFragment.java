package jajimenez.workpage;

import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.app.Dialog;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.TextTool;
import jajimenez.workpage.data.model.TaskReminder;

public class TaskReminderPickerDialogFragment extends DialogFragment {
    private TaskReminder currentTaskReminder;
    private OnTaskReminderSetListener onTaskReminderSetListener;

    private Activity activity;
    private ApplicationLogic applicationLogic;

    public TaskReminderPickerDialogFragment() {
        //currentTaskReminder = null;
        onTaskReminderSetListener = null;

        activity = getActivity();
        applicationLogic = new ApplicationLogic(activity);
    }

    /*public TaskReminderPickerDialogFragment(TaskReminder currentTaskReminder) {
        this.currentTaskReminder = currentTaskReminder;
        onTaskReminderSetListener = null;
    }*/

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // By default, the selected item is the task reminder with ID "4" (15 min.).
        // Note: Database IDs start at 1.
        long reminderId = (getArguments()).getLong("reminder_id", 4);
        currentTaskReminder = applicationLogic.getTaskReminder(reminderId);

        TextTool tool = new TextTool();

        /*if (savedInstanceState != null) {
            long currentTaskReminderId = savedInstanceState.getLong("current_task_reminder_id");
            currentTaskReminder = applicationLogic.getTaskReminder(currentTaskReminderId);
        }

        // By default, the selected item is the task reminder with ID "4" (15 min.).
        // Note: Database IDs start at 1.
        int selectedItem = 3;
        if (currentTaskReminder != null) selectedItem = ((int) currentTaskReminder.getId()) - 1;*/
        int selectedItem = ((int) currentTaskReminder.getId()) - 1;;

        final List<TaskReminder> taskReminders = applicationLogic.getAllTaskReminders();
        int taskReminderCount = taskReminders.size();
        String[] taskReminderNames = new String[taskReminderCount];

        for (int i = 0; i < taskReminderCount; i++) {
            taskReminderNames[i] = tool.getTaskReminderText(activity, taskReminders.get(i));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.reminder_2);
        builder.setNegativeButton(R.string.cancel, null);
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

        return builder.create();
    }

    /*@Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        
        outState.putLong("current_task_reminder_id", currentTaskReminder.getId());
    }*/

    public void setOnTaskReminderSetListener(OnTaskReminderSetListener listener) {
        onTaskReminderSetListener = listener;
    }

    public static interface OnTaskReminderSetListener {
        void onTaskReminderSet(TaskReminder reminder);
    }
}