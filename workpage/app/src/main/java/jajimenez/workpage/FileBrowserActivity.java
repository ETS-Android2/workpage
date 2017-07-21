package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;
import java.util.SortedSet;
import java.util.TreeSet;
import java.io.File;

import android.support.v7.app.AppCompatActivity;
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
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.graphics.drawable.Drawable;

import jajimenez.workpage.logic.ApplicationLogic;

public class FileBrowserActivity extends AppCompatActivity implements DataChangeReceiverActivity {
    private LinearLayout exportingOptionsLinearLayout;
    private Button formatButton;
    private ImageButton formatOptionsImageButton;
    private ListView listView;
    private LinearLayout fileLinearLayout;
    private EditText fileNameEditText;
    private TextView fileExtensionTextView;
    private Button saveButton;
    private TextView errorTextView;
    private MenuItem goUpMenuItem;

    private SelectFormatDialogFragment.OnNewFormatSelectedListener onNewFormatSelectedListener;

    private OverwriteFileConfirmationDialogFragment.OnOverwriteConfirmationListener onOverwriteConfirmationListener;
    private DataImportConfirmationDialogFragment.OnDataImportConfirmationListener onDataImportConfirmationListener;

    private DataExportBroadcastReceiver exportReceiver;
    private DataImportBroadcastReceiver importReceiver;

    private File initialFile;
    private File currentFile;

    private boolean interfaceReady;
    private String mode;
    private boolean storageAvailable;

    private int selectedExportingFormat;

    private String workpageDataExtension;
    private String csvExtension;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        workpageDataExtension = FileBrowserActivity.this.getString(R.string.workpage_data_extension);
        csvExtension = FileBrowserActivity.this.getString(R.string.csv_extension);

        setContentView(R.layout.file_browser);

        listView = (ListView) findViewById(android.R.id.list);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File selectedFile = (File) FileBrowserActivity.this.listView.getItemAtPosition(position);

                if (selectedFile.isDirectory()) {
                    currentFile = selectedFile;
                    updateInterface();
                }
                else {
                    if (mode != null && mode.equals("export")) {
                        String selectedFileName = selectedFile.getName();

                        int extensionPosition = -1;

                        if (selectedExportingFormat == ApplicationLogic.WORKPAGE_DATA) extensionPosition = selectedFileName.lastIndexOf(workpageDataExtension);
                        else extensionPosition = selectedFileName.lastIndexOf(csvExtension);

                        String nameNoExtension = selectedFileName.substring(0, extensionPosition);
                        fileNameEditText.setText(nameNoExtension);
                    }
                    else if (mode != null && mode.equals("import")) {
                        DataImportConfirmationDialogFragment importFragment = new DataImportConfirmationDialogFragment();
                        importFragment.setOnDataImportConfirmationListener(onDataImportConfirmationListener);

                        Bundle arguments = new Bundle();
                        arguments.putString("file_path", selectedFile.getAbsolutePath());
                        importFragment.setArguments(arguments);

                        importFragment.show(getFragmentManager(), "data_import_confirmation");
                    }
                }
            }
        });


        exportingOptionsLinearLayout = (LinearLayout) findViewById(R.id.file_browser_exporting_options);
        formatButton = (Button) findViewById(R.id.file_browser_format);
        formatOptionsImageButton = (ImageButton) findViewById(R.id.file_browser_format_options);
        fileLinearLayout = (LinearLayout) findViewById(R.id.file_browser_file);
        fileNameEditText = (EditText) findViewById(R.id.file_browser_file_name);
        fileExtensionTextView = (TextView) findViewById(R.id.file_browser_file_extension);
        saveButton = (Button) findViewById(R.id.file_browser_save);
        errorTextView = (TextView) findViewById(R.id.file_browser_error);
        goUpMenuItem = null;

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

        onNewFormatSelectedListener = new SelectFormatDialogFragment.OnNewFormatSelectedListener() {
            public void onNewFormatSelected(int format) {
                FileBrowserActivity.this.selectedExportingFormat = format;
                FileBrowserActivity.this.fileNameEditText.setText("");

                updateInterface();
            }
        };

        onOverwriteConfirmationListener = new OverwriteFileConfirmationDialogFragment.OnOverwriteConfirmationListener() {
            public void onConfirmation() {
                FileBrowserActivity.this.startDataExport();
             }
        };

        onDataImportConfirmationListener = new DataImportConfirmationDialogFragment.OnDataImportConfirmationListener() {
            public void onConfirmation(File from) {
                if (from != null) FileBrowserActivity.this.startDataImport(from);
            }
        };

        initialFile = Environment.getExternalStorageDirectory();
        currentFile = null;

        Intent intent = getIntent();

        if (savedInstanceState == null) {
            mode = intent.getStringExtra("mode");
            selectedExportingFormat = ApplicationLogic.WORKPAGE_DATA;
            currentFile = initialFile;
        }
        else {
            mode = savedInstanceState.getString("mode");
            selectedExportingFormat = savedInstanceState.getInt("selected_exporting_format");
            String currentFileAbsolutePath = savedInstanceState.getString("current_file");

            if (currentFileAbsolutePath != null && !currentFileAbsolutePath.equals("")) currentFile = new File(currentFileAbsolutePath);

            SelectFormatDialogFragment formatFragment = (SelectFormatDialogFragment) (getFragmentManager()).findFragmentByTag("select_format");
            if (formatFragment != null) formatFragment.setOnNewFormatSelectedListener(onNewFormatSelectedListener);

            OverwriteFileConfirmationDialogFragment overwriteFragment = (OverwriteFileConfirmationDialogFragment) (getFragmentManager()).findFragmentByTag("overwrite_file_confirmation");
            if (overwriteFragment != null) overwriteFragment.setOnOverwriteConfirmationListener(onOverwriteConfirmationListener);

            DataImportConfirmationDialogFragment importFragment = (DataImportConfirmationDialogFragment) (getFragmentManager()).findFragmentByTag("data_import_confirmation");
            if (importFragment != null) importFragment.setOnDataImportConfirmationListener(onDataImportConfirmationListener);
        }

        if (mode == null || mode.equals("")) mode = "export";

        if (mode.equals("export")) setTitle(R.string.export_data);
        else setTitle(R.string.import_data);

        saveButton.setEnabled(false);
        interfaceReady = false;
        storageAvailable = false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.file_browser, menu);

        goUpMenuItem = menu.findItem(R.id.file_browser_menu_go_up);

        if (currentFile.equals(initialFile)) {
            goUpMenuItem.setEnabled(false);

            Drawable goUpItemIcon = goUpMenuItem.getIcon();
            goUpItemIcon.setAlpha(127);

            goUpMenuItem.setVisible(storageAvailable);
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        exportReceiver = new DataExportBroadcastReceiver(this);
        IntentFilter exportFilter = new IntentFilter(ApplicationConstants.DATA_EXPORT_ACTION);
        registerReceiver(exportReceiver, exportFilter);

        importReceiver = new DataImportBroadcastReceiver(this);
        IntentFilter importFilter = new IntentFilter(ApplicationConstants.DATA_IMPORT_ACTION);
        registerReceiver(importReceiver, importFilter);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (DataImportService.getStatus() == DataImportService.STATUS_NOT_RUNNING && DataExportService.getStatus() == DataExportService.STATUS_NOT_RUNNING) {
            updateInterface();
        }
        else {
            disableInterface();
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        unregisterReceiver(exportReceiver);
        unregisterReceiver(importReceiver);
    }

    public void enableInterface() {
        if (mode.equals("export")) {
            formatButton.setEnabled(true);
            formatOptionsImageButton.setEnabled(selectedExportingFormat == ApplicationLogic.CSV);
        }

        listView.setEnabled(true);
        interfaceReady = true;
    }

    public void disableInterface() {
        interfaceReady = false;

        if (mode.equals("export")) {
            formatButton.setEnabled(false);
            formatOptionsImageButton.setEnabled(false);
        }

        listView.setEnabled(false);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("mode", mode);
        outState.putInt("selected_exporting_format", selectedExportingFormat);
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

            if (mode.equals("export")) {
                if (selectedExportingFormat == ApplicationLogic.WORKPAGE_DATA) {
                    formatButton.setText(R.string.workpage_data);
                    fileExtensionTextView.setText(workpageDataExtension);
                }
                else {
                    formatButton.setText(R.string.csv);
                    fileExtensionTextView.setText(csvExtension);
                }

                formatOptionsImageButton.setEnabled(selectedExportingFormat == ApplicationLogic.CSV);

                exportingOptionsLinearLayout.setVisibility(View.VISIBLE);
                fileLinearLayout.setVisibility(View.VISIBLE);
            }

            errorTextView.setVisibility(View.GONE);
            (new LoadFilesTask()).execute();
        }
        else {
            // Show error message.
            storageAvailable = false;
            listView.setVisibility(View.GONE);

            if (mode.equals("export")) {
                exportingOptionsLinearLayout.setVisibility(View.GONE);
                fileLinearLayout.setVisibility(View.GONE);
            }

            errorTextView.setVisibility(View.VISIBLE);
        }

        if (goUpMenuItem != null) goUpMenuItem.setVisible(storageAvailable);
    }

    private void updateFileListInterface(List<File> subfiles) {
        if (subfiles == null) subfiles = new LinkedList<File>();

        FileAdapter adapter = new FileAdapter(this, R.layout.file_list_item, subfiles);
        listView.setAdapter(adapter);

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

    public void onFormatButtonClicked(View view) {
        SelectFormatDialogFragment formatFragment = new SelectFormatDialogFragment();

        Bundle arguments = new Bundle();
        arguments.putInt("select_format", selectedExportingFormat);

        formatFragment.setArguments(arguments);
        formatFragment.setOnNewFormatSelectedListener(onNewFormatSelectedListener);

        formatFragment.show(getFragmentManager(), "select_format");
    }

    public void onFormatOptionsImageButtonClicked(View view) {
        Intent intent = new Intent(this, CsvSettingsActivity.class);
        startActivity(intent);
    }

    public void onSaveButtonClicked(View view) {
        File to = getToFile();

        if (to.exists()) {
            OverwriteFileConfirmationDialogFragment overwriteFragment = new OverwriteFileConfirmationDialogFragment();
            overwriteFragment.setOnOverwriteConfirmationListener(onOverwriteConfirmationListener);

            Bundle arguments = new Bundle();
            arguments.putString("file_name", to.getName());
            overwriteFragment.setArguments(arguments);

            overwriteFragment.show(getFragmentManager(), "overwrite_file_confirmation");
        }
        else {
            startDataExport();
        }
    }

    private void startDataExport() {
        disableInterface();

        Intent intent = new Intent(this, DataExportService.class);
        intent.putExtra("file_path", (getToFile()).getAbsolutePath());
        intent.putExtra("file_format", selectedExportingFormat);

        startService(intent);
    }

    private void startDataImport(File from) {
        disableInterface();

        Intent intent = new Intent(this, DataImportService.class);
        intent.putExtra("file_path", from.getAbsolutePath());

        startService(intent);
    }

    private File getToFile() {
        String fileName = ((FileBrowserActivity.this.fileNameEditText.getText()).toString()).trim();

        if (selectedExportingFormat == ApplicationLogic.WORKPAGE_DATA) fileName += workpageDataExtension;
        else fileName += csvExtension;

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

                            if ( (selectedExportingFormat == ApplicationLogic.WORKPAGE_DATA && name.endsWith(workpageDataExtension))
                                    || (selectedExportingFormat == ApplicationLogic.CSV && name.endsWith(csvExtension)) ) {
                                subfileFiles.add(f);
                            }
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