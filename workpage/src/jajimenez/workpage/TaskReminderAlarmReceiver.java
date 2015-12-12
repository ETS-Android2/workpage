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
import android.content.res.Resources;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.Task;

public class TaskReminderAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Resources resources = context.getResources();
        ApplicationLogic applicationLogic = new ApplicationLogic(context);

        int reminderId = intent.getIntExtra("reminder_id", -1);
        String reminderIdStr = String.valueOf(reminderId);
        int length = reminderIdStr.length();

        long taskId = Long.parseLong(reminderIdStr.substring(0, length - 1));
        Task task = applicationLogic.getTask(taskId);
        if (task == null) return;

        String reminderType = reminderIdStr.substring(length - 1);
        Calendar calendar = null;

        if (reminderType.equals("0")) {
            // Type is "When".
            calendar = task.getWhen();
        }
        else if (reminderType.equals("1")) {
            // Type is "Start".
            calendar = task.getStart();
        }
        else if (reminderType.equals("2")) {
            // Type is "Deadline".
            calendar = task.getDeadline();
        }
        else {
            return;
        }

        if (calendar == null) return;

        String title = "";
        String text = task.getTitle();

        Calendar now = Calendar.getInstance();

        int minute = 60000; // Number of milliseconds in a minute (60*1000 = 60000).
        int hour = 3600000; // Number of milliseconds in an hour (60*60*1000 = 3600000).
        int day = 86400000; // Number of milliseconds in a day (24*60*60*1000 = 86400000).

        long difference = calendar.getTimeInMillis() - now.getTimeInMillis();

        long absDifference = difference;
        if (absDifference < 0) absDifference = absDifference * (-1);

        if (absDifference < minute) {
            title = context.getString(R.string.now);
        }
        else if (absDifference >= minute && absDifference < hour) {
            int numMinutes = (int) (absDifference / ((long) minute));

            if (difference < 0) title = resources.getQuantityString(R.plurals.x_minutes_ago, numMinutes, numMinutes);
            else title = resources.getQuantityString(R.plurals.due_in_x_minutes, numMinutes, numMinutes);
        }
        else if (absDifference >= hour && absDifference < day) {
            int numHours = (int) (absDifference / ((long) hour));

            if (difference < 0) title = resources.getQuantityString(R.plurals.x_hours_ago, numHours, numHours);
            else title = resources.getQuantityString(R.plurals.due_in_x_hours, numHours, numHours);
        }
        else { // (absDifference >= day)
            int numDays = (int) (absDifference / ((long) day));

            if (difference < 0) title = resources.getQuantityString(R.plurals.x_days_ago, numDays, numDays);
            else title = resources.getQuantityString(R.plurals.due_in_x_days, numDays, numDays);
        }

        Intent taskIntent = new Intent(context, TaskActivity.class);
        taskIntent.putExtra("task_id", taskId);

        Intent dismissIntent = new Intent(context, TaskReminderNotificationService.class);
        dismissIntent.putExtra("reminder_id", reminderId);
        dismissIntent.setAction(ApplicationConstants.TASK_REMINDER_DISMISS_ACTION);
        PendingIntent dismissPendingIntent = PendingIntent.getService(context, reminderId, dismissIntent, 0);

        Intent snoozeIntent = new Intent(context, TaskReminderNotificationService.class);
        snoozeIntent.putExtra("reminder_id", reminderId);
        snoozeIntent.setAction(ApplicationConstants.TASK_REMINDER_SNOOZE_ACTION);
        PendingIntent snoozePendingIntent = PendingIntent.getService(context, reminderId, snoozeIntent, 0);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(TaskActivity.class);
        stackBuilder.addNextIntent(taskIntent);
        PendingIntent taskPendingIntent = stackBuilder.getPendingIntent((int) taskId, PendingIntent.FLAG_CANCEL_CURRENT);

        Builder builder = new Builder(context);
        builder.setSmallIcon(R.drawable.notification_workpage);
        builder.setContentTitle(title);
        builder.setContentText(text);
        builder.setContentIntent(taskPendingIntent);
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        builder.addAction(R.drawable.notification_dismiss, context.getString(R.string.dismiss), dismissPendingIntent);
        builder.addAction(R.drawable.notification_snooze, context.getString(R.string.snooze), snoozePendingIntent);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(reminderId, builder.build());
    }
}
