package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.File;
import java.io.IOException;

import android.util.SparseBooleanArray;
import android.app.ListActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Environment;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.Window;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.drawable.Drawable;

import jajimenez.workpage.logic.ApplicationLogic;

public class FileBrowserActivity extends ListActivity implements DataChangeReceiverActivity {
    private Menu menu;
    private ListView listView;
    private LinearLayout fileLinearLayout;
    private EditText fileNameEditText;
    private Button saveButton;
    private TextView errorTextView;
    private MenuItem goUpMenuItem;

    private OverwriteFileConfirmationDialogFragment.OnOverwriteConfirmationListener onOverwriteConfirmationListener;
    private DataImportConfirmationDialogFragment.OnDataImportConfirmationListener onDataImportConfirmationListener;

    private ApplicationLogic applicationLogic;
    private File initialFile;
    private File currentFile;

    private Bundle savedInstanceState;

    private boolean interfaceReady;
    private String mode;
    private boolean storageAvailable;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.file_browser);

        listView = getListView();
        fileLinearLayout = (LinearLayout) findViewById(R.id.fileBrowser_file);
        fileNameEditText = (EditText) findViewById(R.id.fileBrowser_file_name);
        saveButton = (Button) findViewById(R.id.fileBrowser_save);
        errorTextView = (TextView) findViewById(R.id.fileBrowser_error);
        goUpMenuItem = null;

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        mode = intent.getStringExtra("mode");

        if (mode != null && mode.equals("export")) {
            setTitle(R.string.export_data);
            fileLinearLayout.setVisibility(View.VISIBLE);
        }
        else if (mode != null && mode.equals("import")) {
            setTitle(R.string.import_data);
        }

        fileNameEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Nothing to do.
            }

            public void afterTextChanged(Editable s) {
                String text = (s.toString()).trim();
                boolean enabled = (text.length() > 0);

                FileBrowserActivity.this.saveButton.setEnabled(enabled);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Nothing to do.
            }
        });

        saveButton.setEnabled(false);
        interfaceReady = false;
        storageAvailable = false;

        onOverwriteConfirmationListener = new OverwriteFileConfirmationDialogFragment.OnOverwriteConfirmationListener() {
            public void onConfirmation() {
                FileBrowserActivity.this.startDataExport();
            }
        };

        onDataImportConfirmationListener = new DataImportConfirmationDialogFragment.OnDataImportConfirmationListener() {
            public void onConfirmation(File from) {
                FileBrowserActivity.this.startDataImport(from);
            }
        };

        initialFile = Environment.getExternalStorageDirectory();

        this.savedInstanceState = savedInstanceState;

        if (savedInstanceState == null) {
            currentFile = initialFile;
        }
        else {
            String currentFileAbsolutePath = savedInstanceState.getString("current_file");
            currentFile = new File(currentFileAbsolutePath);

            OverwriteFileConfirmationDialogFragment overwriteFragment = (OverwriteFileConfirmationDialogFragment) (getFragmentManager()).findFragmentByTag("overwrite_file_confirmation");
            if (overwriteFragment != null) overwriteFragment.setOnOverwriteConfirmationListener(onOverwriteConfirmationListener);

            DataImportConfirmationDialogFragment importFragment = (DataImportConfirmationDialogFragment) (getFragmentManager()).findFragmentByTag("data_import_confirmation");
            if (importFragment != null) importFragment.setOnDataImportConfirmationListener(onDataImportConfirmationListener);
        }

        DataExportBroadcastReceiver exportReceiver = new DataExportBroadcastReceiver(this);
        IntentFilter exportFilter = new IntentFilter(ApplicationConstants.DATA_EXPORT_ACTION);
        registerReceiver(exportReceiver, exportFilter);

        DataImportBroadcastReceiver importReceiver = new DataImportBroadcastReceiver(this);
        IntentFilter importFilter = new IntentFilter(ApplicationConstants.DATA_IMPORT_ACTION);
        registerReceiver(importReceiver, importFilter);

        applicationLogic = new ApplicationLogic(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.file_browser, menu);

        goUpMenuItem = menu.findItem(R.id.fileBrowserMenu_goUp);

        if (currentFile.equals(initialFile)) {
            goUpMenuItem.setEnabled(false);
            
            Drawable goUpItemIcon = goUpMenuItem.getIcon();
            goUpItemIcon.setAlpha(127);

            goUpMenuItem.setVisible(storageAvailable);
        }

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (ImportDataService.getStatus() == ImportDataService.STATUS_NOT_RUNNING && ExportDataService.getStatus() == ExportDataService.STATUS_NOT_RUNNING) {
            updateInterface();
        }
        else {
            disableInterface();
        }
    }

    public void enableInterface() {
        listView.setEnabled(true);
        setProgressBarIndeterminateVisibility(false);
        interfaceReady = true;
    }

    public void disableInterface() {
        interfaceReady = false;
        setProgressBarIndeterminateVisibility(true);
        listView.setEnabled(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("current_file", currentFile.getAbsolutePath());
        super.onSaveInstanceState(outState);
    }

    public EditText getFileNameEditText() {
        return fileNameEditText;
    }

    public void updateInterface() {
        String storageState = Environment.getExternalStorageState();

        boolean storageForExportingReady = storageState.equals(Environment.MEDIA_MOUNTED);
        boolean storageForImportingReady = (storageState.equals(Environment.MEDIA_MOUNTED) || storageState.equals(Environment.MEDIA_MOUNTED_READ_ONLY));

        if (mode != null && ( (mode.equals("export") && storageForExportingReady) || (mode.equals("import") && storageForImportingReady) )) {
            // Show files.
            storageAvailable = true;

            listView.setVisibility(View.VISIBLE);
            if (mode.equals("export")) fileLinearLayout.setVisibility(View.VISIBLE);
            errorTextView.setVisibility(View.GONE);

            (new LoadFilesTask()).execute();
        }
        else {
            // Show error message.
            storageAvailable = false;

            listView.setVisibility(View.GONE);
            if (mode.equals("export")) fileLinearLayout.setVisibility(View.GONE);
            errorTextView.setVisibility(View.VISIBLE);
        }

        if (goUpMenuItem != null) goUpMenuItem.setVisible(storageAvailable);
    }

    private void updateFileListInterface(List<File> subfiles) {
        if (subfiles == null) subfiles = new LinkedList<File>();

        FileAdapter adapter = new FileAdapter(this, R.layout.file_list_item, subfiles);
        setListAdapter(adapter);

        if (goUpMenuItem != null) {
            Drawable goUpItemIcon = goUpMenuItem.getIcon();

            if (currentFile.equals(initialFile)) {
                goUpMenuItem.setEnabled(false);
                goUpItemIcon.setAlpha(127);
            }
            else {
                goUpMenuItem.setEnabled(true);
                goUpItemIcon.setAlpha(255);
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        File selectedFile = (File) l.getItemAtPosition(position);

        if (selectedFile.isDirectory()) {
            currentFile = selectedFile;
            updateInterface();
        }
        else {
            if (mode != null && mode.equals("export")) {
                String selectedFileName = selectedFile.getName();
                int extensionPosition = selectedFileName.lastIndexOf(".workpage");

                String nameNoExtension = selectedFileName.substring(0, extensionPosition); 
                fileNameEditText.setText(nameNoExtension);
            }
            else if (mode != null && mode.equals("import")) {
                DataImportConfirmationDialogFragment importFragment = new DataImportConfirmationDialogFragment(selectedFile);
                importFragment.setOnDataImportConfirmationListener(onDataImportConfirmationListener);

                importFragment.show(getFragmentManager(), "data_import_confirmation");
            }
        }
    }

    public void onGoUpItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        if (currentFile != null) {
            File parent = currentFile.getParentFile();

            if (parent != null) {
                currentFile = parent;
                updateInterface();
            }
        }
    }

    public void onSaveButtonClicked(View view) {
        File to = getToFile();

        if (to.exists()) {
            OverwriteFileConfirmationDialogFragment overwriteFragment = new OverwriteFileConfirmationDialogFragment(to.getName());
            overwriteFragment.setOnOverwriteConfirmationListener(onOverwriteConfirmationListener);

            overwriteFragment.show(getFragmentManager(), "overwrite_file_confirmation");
        }
        else {
            startDataExport();
        }
    }

    private void startDataExport() {
        Intent intent = new Intent(this, ExportDataService.class);
        intent.putExtra("file_path", (getToFile()).getAbsolutePath());

        startService(intent);
    }

    private void startDataImport(File from) {
        disableInterface();

        Intent intent = new Intent(this, ImportDataService.class);
        intent.putExtra("file_path", from.getAbsolutePath());

        startService(intent);
    }

    private File getToFile() {
        String fileName = ((FileBrowserActivity.this.fileNameEditText.getText()).toString()).trim() + ".workpage";

        // "currentFile" is a directory.
        return new File(currentFile, fileName);
    }

    private class LoadFilesTask extends AsyncTask<Void, Void, List<File>> {
        protected void onPreExecute() {
            FileBrowserActivity.this.disableInterface();
        }

        protected List<File> doInBackground(Void... parameters) {
            List<File> subfiles = new LinkedList<File>();

            SortedSet<File> subfileFolders = new TreeSet<File>(new FileComparator());
            SortedSet<File> subfileFiles = new TreeSet<File>(new FileComparator());

            File[] sf = FileBrowserActivity.this.currentFile.listFiles();

            if (sf != null) {
                for (File f : sf) {
                    if (!f.isHidden()) {
                        if (f.isDirectory()) {
                            subfileFolders.add(f);
                        }
                        else {
                            String name = f.getName();
                            if (name != null && name.endsWith(".workpage")) subfileFiles.add(f);
                        }
                    }
                }
            }

            subfiles.addAll(subfileFolders);
            subfiles.addAll(subfileFiles);

            return subfiles;
        }

        protected void onPostExecute(List<File> subfiles) {
            FileBrowserActivity.this.updateFileListInterface(subfiles);
            FileBrowserActivity.this.enableInterface();
        }
    }
}
