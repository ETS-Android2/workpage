package jajimenez.workpage;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.logic.ApplicationLogic;

public class ExportDataSettingsActivity extends AppCompatActivity {
    private ListView list;
    private FileReplacementConfirmationDialogFragment.OnFileReplacementConfirmationListener fileReplacementConfirmationListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.export_data_settings);

        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setSubtitle(R.string.select_contexts);

        list = findViewById(R.id.export_data_settings_list);
        list.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                CheckBox check = view.findViewById(R.id.export_data_settings_context_item_check);
                check.toggle();
            }
        });

        fileReplacementConfirmationListener = new FileReplacementConfirmationDialogFragment.OnFileReplacementConfirmationListener() {
            @Override
            public void onConfirmed(Uri output) {
                ExportDataSettingsActivity.this.exportData(output);
            }
        };

        if (savedInstanceState != null) {
            // Dialog listener
            FileReplacementConfirmationDialogFragment confirmationFragment = (FileReplacementConfirmationDialogFragment) (getFragmentManager()).findFragmentByTag("file_replacement_confirmation");
            if (confirmationFragment != null) confirmationFragment.setOnFileReplacementConfirmationListener(fileReplacementConfirmationListener);
        }

        updateInterface();
    }

    private void updateInterface() {
        ApplicationLogic logic = new ApplicationLogic(this);
        List<TaskContext> contexts = logic.getAllTaskContexts();

        ExportDataSettingsContextAdapter adapter = new ExportDataSettingsContextAdapter(this,
                R.layout.export_data_settings_context_item,
                contexts);

        list.setAdapter(adapter);
    }

    public void onExportClicked(View view) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType(ApplicationLogic.APP_MIME_TYPE);
        intent.putExtra(Intent.EXTRA_TITLE, ApplicationLogic.getProposedExportDataFileName());

        startActivityForResult(intent, ApplicationLogic.EXPORT_DATA);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (resultCode == RESULT_OK && requestCode == ApplicationLogic.EXPORT_DATA) {
            Uri outputFile = resultData.getData();

            if (fileHasData(outputFile)) {
                FileReplacementConfirmationDialogFragment fragment = new FileReplacementConfirmationDialogFragment();

                Bundle arguments = new Bundle();
                arguments.putString("output_uri", outputFile.toString());

                fragment.setArguments(arguments);
                fragment.setOnFileReplacementConfirmationListener(fileReplacementConfirmationListener);

                fragment.show(getFragmentManager(), "file_replacement_confirmation");
            } else {
                // Export data
                exportData(outputFile);
            }
        }
    }

    private boolean fileHasData(Uri resource) {
        boolean data = true;

        try {
            InputStream s = (getContentResolver()).openInputStream(resource);
            data = (s != null && s.read() != -1);
        } catch (IOException e) {
            // Nothing to do
        }

        return data;
    }

    private void exportData(Uri output) {
        (new ExportDataTask()).execute(output);
    }

    private class ExportDataTask extends AsyncTask<Uri, Void, Boolean> {
        protected void onPreExecute() {
            // Nothing to do
        }

        protected Boolean doInBackground(Uri... parameters) {
            // The returned value will be "false" if the operation
            // was successful or "true" if there was any error.

            Uri output = parameters[0];
            ApplicationLogic logic = new ApplicationLogic(ExportDataSettingsActivity.this);

            // "exportData" returns "true" if there was an error or "false" otherwise.
            return logic.exportData(output);
        }

        protected void onPostExecute(Boolean error) {
            if (error) {
                (Toast.makeText(ExportDataSettingsActivity.this, R.string.data_export_error, Toast.LENGTH_SHORT)).show();
            }
            else {
                (Toast.makeText(ExportDataSettingsActivity.this, R.string.data_export_success, Toast.LENGTH_SHORT)).show();
                ExportDataSettingsActivity.this.finish();
            }
        }
    }
}
