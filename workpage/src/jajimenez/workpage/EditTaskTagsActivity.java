package jajimenez.workpage;

import java.util.List;
import java.util.LinkedList;

import android.util.SparseBooleanArray;
import android.app.ListActivity;
import android.app.ActionBar;
import android.os.Bundle;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.view.ActionMode;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;
import android.graphics.drawable.Drawable;

import jajimenez.workpage.logic.ApplicationLogic;
import jajimenez.workpage.data.model.TaskContext;
import jajimenez.workpage.data.model.TaskTag;

public class EditTaskTagsActivity extends ListActivity {
    private Menu menu;
    private ListView listView;
    private TextView emptyTextView;
    private ActionBar actionBar;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;
    private List<TaskTag> contextTags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.edit_task_tags);

        listView = getListView();
        emptyTextView = (TextView) findViewById(android.R.id.empty);
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        createContextualActionBar();

        applicationLogic = new ApplicationLogic(this);
        currentTaskContext = applicationLogic.getCurrentTaskContext();
        contextTags = new LinkedList<TaskTag>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_task_tags, menu);

        this.menu = menu;
        updateInterface();

        return true;
    }

    private void updateInterface() {
        // Information about the current task context.
        actionBar.setSubtitle(currentTaskContext.getName());

        emptyTextView.setText("");

        // Show tags.
        (new LoadTaskTagsDBTask()).execute();
    }

    private void updateTaskTagListInterface(List<TaskTag> tags) {
        if (tags == null) tags = new LinkedList<TaskTag>();

        contextTags = tags;
        TaskTagAdapter adapter = new TaskTagAdapter(this, R.layout.task_tag_list_item, tags);
        setListAdapter(adapter);

        checkNoDataText(adapter);
    }

    private void updateTaskTagListInterfaceByRemoving(List<TaskTag> tagsToRemove) {
        TaskTagAdapter adapter = (TaskTagAdapter) getListAdapter();
        for (TaskTag tag : tagsToRemove) adapter.remove(tag);

        checkNoDataText(adapter);
    }

    private void checkNoDataText(TaskTagAdapter adapter) {
        if (adapter.isEmpty()) emptyTextView.setText(R.string.no_tags);
    }

    private List<TaskTag> getSelectedTaskTags() {
        List<TaskTag> selectedTags = new LinkedList<TaskTag>();
        
        TaskTagAdapter adapter = (TaskTagAdapter) getListAdapter();
        SparseBooleanArray itemSelectedStates = listView.getCheckedItemPositions();
        int itemCount = listView.getCount();

        for (int i = 0; i < itemCount; i++) {
            if (itemSelectedStates.get(i)) {
                // The tag with position "i" is selected.
                TaskTag tag = adapter.getItem(i);
                selectedTags.add(tag);
            }
        }

        return selectedTags;
    }

    private void createContextualActionBar() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.edit_task_tags_contextual, menu);

                return true;
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                // Do nothing
            }

            // Returns "true" if this callback handled the event, "false"
            // if the standard "MenuItem" invocation should continue.
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                boolean eventHandled = false;
                final List<TaskTag> selectedTags = EditTaskTagsActivity.this.getSelectedTaskTags();

                switch (item.getItemId()) {
                    case R.id.editTaskTagsContextualMenu_edit:
                        // Show an edition dialog.
                        EditTaskTagDialogFragment editFragment = new EditTaskTagDialogFragment(selectedTags.get(0), EditTaskTagsActivity.this.contextTags);

                        editFragment.setOnTaskTagSavedListener(new EditTaskTagDialogFragment.OnTaskTagSavedListener() {
                            public void onTaskTagSaved() {
                                EditTaskTagsActivity.this.setResult(RESULT_OK);

                                // Close the contextual action bar.
                                mode.finish();

                                // Update the list view.
                                EditTaskTagsActivity.this.updateInterface();
                            }
                        });

                        editFragment.show(getFragmentManager(), "edit_task_tag");
                        eventHandled = true;
                        break;

                    case R.id.editTaskTagsContextualMenu_delete:
                        // Show a deletion confirmation dialog.
                        DeleteTaskTagDialogFragment deleteFragment = new DeleteTaskTagDialogFragment(selectedTags);

                        deleteFragment.setOnDeleteListener(new DeleteTaskTagDialogFragment.OnDeleteListener() {
                            public void onDelete() {
                                EditTaskTagsActivity.this.setResult(RESULT_OK);

                                // Close the contextual action bar.
                                mode.finish();

                                // Update the list view.
                                EditTaskTagsActivity.this.updateTaskTagListInterfaceByRemoving(selectedTags);
                            }
                        });

                        deleteFragment.show(getFragmentManager(), "delete_task_tag");
                        eventHandled = true;
                        break;
                }

                return eventHandled;
            }

            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectedTagCount = EditTaskTagsActivity.this.listView.getCheckedItemCount();
                mode.setTitle(EditTaskTagsActivity.this.getString(R.string.selected, selectedTagCount));

                MenuItem editItem = (mode.getMenu()).findItem(R.id.editTaskTagsContextualMenu_edit);
                Drawable editItemIcon = editItem.getIcon();

                if (selectedTagCount == 1) {
                    editItem.setEnabled(true);
                    editItemIcon.setAlpha(255);
                }
                else {
                    editItem.setEnabled(false);
                    editItemIcon.setAlpha(127);
                }
            }
        });
    }

    public void onNewTaskTagItemSelected(MenuItem item) {
        TaskTag newTag = new TaskTag();
        newTag.setContextId(currentTaskContext.getId());

        // Show an edition dialog.
        EditTaskTagDialogFragment editFragment = new EditTaskTagDialogFragment(newTag, EditTaskTagsActivity.this.contextTags);

        editFragment.setOnTaskTagSavedListener(new EditTaskTagDialogFragment.OnTaskTagSavedListener() {
            public void onTaskTagSaved() {
                EditTaskTagsActivity.this.setResult(RESULT_OK);

                // Update the list view.
                EditTaskTagsActivity.this.updateInterface();
            }
        });

        editFragment.show(getFragmentManager(), "edit_task_tag");
    }

    private class LoadTaskTagsDBTask extends AsyncTask<Void, Void, List<TaskTag>> {
        protected void onPreExecute() {
            EditTaskTagsActivity.this.setProgressBarIndeterminateVisibility(true);

            EditTaskTagsActivity.this.menu.setGroupEnabled(0, false);
            EditTaskTagsActivity.this.listView.setEnabled(false);
        }

        protected List<TaskTag> doInBackground(Void... parameters) {
            return EditTaskTagsActivity.this.applicationLogic.getAllTaskTags(EditTaskTagsActivity.this.currentTaskContext);
        }

        protected void onPostExecute(List<TaskTag> tags) {
            EditTaskTagsActivity.this.updateTaskTagListInterface(tags);

            EditTaskTagsActivity.this.menu.setGroupEnabled(0, true);
            EditTaskTagsActivity.this.listView.setEnabled(true);

            EditTaskTagsActivity.this.setProgressBarIndeterminateVisibility(false);
        }
    }
}
