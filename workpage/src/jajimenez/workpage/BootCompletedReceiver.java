package jajimenez.workpage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import jajimenez.workpage.logic.ApplicationLogic;

public class BootCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if ((intent.getAction()).equals("android.intent.action.BOOT_COMPLETED")) {
            // To avoid that MainActivity sets the reminder
            // alarms again when opening the application
            // after booting the device.
            ApplicationLogic.setApplicationFirstExecution(false);

            ApplicationLogic applicationLogic = new ApplicationLogic(context);
            applicationLogic.updateAllOpenTaskReminderAlarms(false);
        }
    }
}
