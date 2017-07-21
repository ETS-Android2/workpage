package jajimenez.workpage;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class DataExportBroadcastReceiver extends BroadcastReceiver {
    private DataChangeReceiverActivity receiverActivity;

    public DataExportBroadcastReceiver(DataChangeReceiverActivity receiverActivity) {
        this.receiverActivity = receiverActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // "result" will be "false" if the operation was successful
        // or "true" if there was any error.
        boolean result = intent.getBooleanExtra(ApplicationConstants.DATA_EXPORT_RESULT, false);

        if (receiverActivity instanceof MainActivity) {
            receiverActivity.enableInterface();

            MainActivity mainActivity = (MainActivity) receiverActivity;
            //if (mainActivity.isInFront()) mainActivity.updateInterface();
            mainActivity.updateInterface();
        }
        else if (receiverActivity instanceof FileBrowserActivity) {
            Activity activity = (Activity) receiverActivity;

            if (result) {
                receiverActivity.enableInterface();

                (Toast.makeText(activity, R.string.export_error, Toast.LENGTH_SHORT)).show();
                (((FileBrowserActivity) activity).getFileNameEditText()).setText("");
            }
            else {
                (Toast.makeText(activity, R.string.export_success, Toast.LENGTH_SHORT)).show();

                // Close activity.
                activity.finish();
            }
        }
    }
}