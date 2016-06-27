package jajimenez.workpage;

import java.util.Calendar;

import android.app.Notification;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.TaskStackBuilder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.logic.TextTool;
import jajimenez.workpage.data.model.Task;

public class TaskReminderAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PreferenceManager.setDefaultValues(context, R.xml.preferences, false);

        ApplicationLogic applicationLogic = new ApplicationLogic(context);
        TextTool textTool = new TextTool();

        // The Reminder ID contains the task ID and the reminder type.
        int reminderId = intent.getIntExtra("task_reminder_id", -1);
        String reminderIdStr = String.valueOf(reminderId);
        int length = reminderIdStr.length();

        long taskId = Long.parseLong(reminderIdStr.substring(0, length - 1));
        Task task = applicationLogic.getTask(taskId);
        if (task == null) return;

        String reminderType = reminderIdStr.substring(length - 1);

        // Text for the notification.
        String title = task.getTitle();
        String text = null;

        Calendar calendar = null;

        if (reminderType.equals("0")) {
            // Type is "When".
            task.setWhenReminder(null);

            calendar = task.getWhen();
            text = textTool.getTaskDateText(context, task, false, TextTool.WHEN);
        }
        else if (reminderType.equals("1")) {
            // Type is "Start".
            task.setStartReminder(null);

            calendar = task.getStart();
            text = textTool.getTaskDateText(context, task, true, TextTool.START);
        }
        else if (reminderType.equals("2")) {
            // Type is "Deadline".
            task.setDeadlineReminder(null);

            calendar = task.getDeadline();
            text = textTool.getTaskDateText(context, task, true, TextTool.DEADLINE);
        }
        else {
            return;
        }

        if (calendar == null) return;

        applicationLogic.saveTask(task);

        Intent taskIntent = new Intent(context, TaskActivity.class);
        taskIntent.putExtra("task_id", taskId);

        Intent dismissIntent = new Intent(context, TaskReminderAlarmService.class);
        dismissIntent.putExtra("task_reminder_id", reminderId);
        dismissIntent.setAction(ApplicationConstants.TASK_REMINDER_DISMISS_ACTION);
        PendingIntent dismissPendingIntent = PendingIntent.getService(context, reminderId, dismissIntent, 0);

        Intent snoozeIntent = new Intent(context, TaskReminderAlarmService.class);
        snoozeIntent.putExtra("task_reminder_id", reminderId);
        snoozeIntent.setAction(ApplicationConstants.TASK_REMINDER_SNOOZE_ACTION);
        PendingIntent snoozePendingIntent = PendingIntent.getService(context, reminderId, snoozeIntent, 0);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(TaskActivity.class);
        stackBuilder.addNextIntent(taskIntent);
        PendingIntent taskPendingIntent = stackBuilder.getPendingIntent((int) taskId, PendingIntent.FLAG_CANCEL_CURRENT);

        // Get the reminder settings.
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        boolean sound = preferences.getBoolean("notifications_sound", true);
        boolean vibrate = preferences.getBoolean("notifications_vibrate", true);
        boolean light = preferences.getBoolean("notifications_light", true);

        int notificationFlags = 0;
        
        if (sound) notificationFlags = notificationFlags | Notification.DEFAULT_SOUND;
        if (vibrate) notificationFlags = notificationFlags | Notification.DEFAULT_VIBRATE;
        if (light) notificationFlags = notificationFlags | Notification.DEFAULT_LIGHTS;

        Builder builder = new Builder(context);
        builder.setSmallIcon(R.drawable.notification_workpage);
        builder.setContentTitle(title);
        builder.setTicker(title);
        builder.setContentText(text);
        builder.setContentIntent(taskPendingIntent);
        builder.setDefaults(notificationFlags);
        builder.setOngoing(true);
        builder.addAction(R.drawable.notification_dismiss, context.getString(R.string.dismiss), dismissPendingIntent);
        builder.addAction(R.drawable.notification_snooze, context.getString(R.string.snooze), snoozePendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(reminderId, builder.build());
    }
}
