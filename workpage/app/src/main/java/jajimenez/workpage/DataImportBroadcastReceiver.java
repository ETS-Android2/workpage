package jajimenez.workpage;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import jajimenez.workpage.logic.ApplicationLogic;

public class DataImportBroadcastReceiver extends BroadcastReceiver {
    private DataChangeReceiverActivity receiverActivity;

    public DataImportBroadcastReceiver(DataChangeReceiverActivity receiverActivity) {
        this.receiverActivity = receiverActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        receiverActivity.enableInterface();

        int result = intent.getIntExtra(ApplicationConstants.DATA_IMPORT_RESULT, -1);

        if (receiverActivity instanceof MainActivity) {
            MainActivity mainActivity = (MainActivity) receiverActivity;
            mainActivity.updateInterface();
        }
        else if (receiverActivity instanceof FileBrowserActivity) {
            Activity activity = (Activity) receiverActivity;

            switch (result) {
                case ApplicationLogic.IMPORT_SUCCESS:
                    (Toast.makeText(activity, R.string.import_success, Toast.LENGTH_SHORT)).show();
                    activity.finish();
                    break;
                case ApplicationLogic.IMPORT_ERROR_OPENING_FILE:
                    (Toast.makeText(activity, R.string.import_error_opening_file, Toast.LENGTH_SHORT)).show();
                    break;

                case ApplicationLogic.IMPORT_ERROR_FILE_NOT_COMPATIBLE:
                    (Toast.makeText(activity, R.string.import_error_file_not_compatible, Toast.LENGTH_SHORT)).show();
                    break;

                case ApplicationLogic.IMPORT_ERROR_DATA_NOT_VALID:
                    (Toast.makeText(activity, R.string.import_error_data_not_valid, Toast.LENGTH_SHORT)).show();
                    break;

                default:
                    (Toast.makeText(activity, R.string.import_error_importing_data, Toast.LENGTH_SHORT)).show();
                    break;
            }
        }
    }
}