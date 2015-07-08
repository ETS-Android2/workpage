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
    private ActionMode actionMode;

    private ApplicationLogic applicationLogic;
    private TaskContext currentTaskContext;
    private List<TaskTag> contextTags;

    private Bundle savedInstanceState;
    private boolean interfaceReady;

    private EditTaskTagDialogFragment.OnTaskTagSavedListener saveTaskTagListener;
    private DeleteTaskTagDialogFragment.OnDeleteListener deleteTaskTagListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.edit_task_tags);

        listView = getListView();
        emptyTextView = (TextView) findViewById(android.R.id.empty);
        actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionMode = null;

        createContextualActionBar();
        interfaceReady = false;

        saveTaskTagListener = new EditTaskTagDialogFragment.OnTaskTagSavedListener() {
            public void onTaskTagSaved() {
                // Close the contextual action bar.
                if (EditTaskTagsActivity.this.actionMode != null) EditTaskTagsActivity.this.actionMode.finish();

                // Update the list view.
                EditTaskTagsActivity.this.updateInterface();
            }
        };

        deleteTaskTagListener = new DeleteTaskTagDialogFragment.OnDeleteListener() {
            public void onDelete() {
                // Close the context action bar.
                if (EditTaskTagsActivity.this.actionMode != null) EditTaskTagsActivity.this.actionMode.finish();

                // Update the list view.
                EditTaskTagsActivity.this.updateInterface();
            }
        };

        this.savedInstanceState = savedInstanceState;

        if (savedInstanceState != null) {
            EditTaskTagDialogFragment editFragment = (EditTaskTagDialogFragment) (getFragmentManager()).findFragmentByTag("edit_task_tag");
            if (editFragment != null) editFragment.setOnTaskTagSavedListener(saveTaskTagListener);

            DeleteTaskTagDialogFragment deleteFragment = (DeleteTaskTagDialogFragment) (getFragmentManager()).findFragmentByTag("delete_task_tag");
            if (deleteFragment != null) deleteFragment.setOnDeleteListener(deleteTaskTagListener);
        }

        applicationLogic = new ApplicationLogic(this);
        currentTaskContext = applicationLogic.getCurrentTaskContext();
        contextTags = new LinkedList<TaskTag>();
    }

    private void createContextualActionBar() {
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        listView.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                EditTaskTagsActivity.this.actionMode = mode;

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.edit_task_tags_contextual, menu);

                return true;
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                EditTaskTagsActivity.this.actionMode = null;
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
                        editFragment.setOnTaskTagSavedListener(EditTaskTagsActivity.this.saveTaskTagListener);
                        editFragment.show(getFragmentManager(), "edit_task_tag");

                        eventHandled = true;
                        break;

                    case R.id.editTaskTagsContextualMenu_delete:
                        // Show a deletion confirmation dialog.
                        DeleteTaskTagDialogFragment deleteFragment = new DeleteTaskTagDialogFragment(selectedTags);
                        deleteFragment.setOnDeleteListener(EditTaskTagsActivity.this.deleteTaskTagListener);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_task_tags, menu);
        this.menu = menu;

        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateInterface();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        List<Integer> selectedItems = getSelectedItems();
        int selectedItemCount = selectedItems.size();
        int[] selected = new int[selectedItemCount];

        for (int i = 0; i < selectedItemCount; i++) selected[i] = selectedItems.get(i);
        outState.putIntArray("selected_items", selected);

        if (actionMode != null) actionMode.finish();

        super.onSaveInstanceState(outState);
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

        if (adapter.isEmpty()) {
            emptyTextView.setText(R.string.no_tags);
        } else if (savedInstanceState != null) {
            int[] selectedItems = savedInstanceState.getIntArray("selected_items");

            if (selectedItems != null) {
                for (int position : selectedItems) listView.setItemChecked(position, true);
                savedInstanceState.remove("selected_items");
            }
        }
    }

    private List<Integer> getSelectedItems() {
        List<Integer> selectedItems = new LinkedList<Integer>();
        
        SparseBooleanArray itemSelectedStates = listView.getCheckedItemPositions();
        int itemCount = listView.getCount();

        for (int i = 0; i < itemCount; i++) {
            if (itemSelectedStates.get(i)) {
                // The item with position "i" is selected.
                selectedItems.add(i);
            }
        }

        return selectedItems;
    }

    private List<TaskTag> getSelectedTaskTags() {
        List<TaskTag> selectedTags = new LinkedList<TaskTag>();

        TaskTagAdapter adapter = (TaskTagAdapter) getListAdapter();
        List<Integer> selectedItems = getSelectedItems();
        
        for (int position : selectedItems) {
            TaskTag tag = adapter.getItem(position);
            selectedTags.add(tag);
        }

        return selectedTags;
    }

    public void onNewTaskTagItemSelected(MenuItem item) {
        if (!interfaceReady) return;

        TaskTag newTag = new TaskTag();
        newTag.setContextId(currentTaskContext.getId());

        // Show an edition dialog.
        EditTaskTagDialogFragment editFragment = new EditTaskTagDialogFragment(newTag, EditTaskTagsActivity.this.contextTags);
        editFragment.setOnTaskTagSavedListener(saveTaskTagListener);
        editFragment.show(getFragmentManager(), "edit_task_tag");
    }

    private class LoadTaskTagsDBTask extends AsyncTask<Void, Void, List<TaskTag>> {
        protected void onPreExecute() {
            EditTaskTagsActivity.this.interfaceReady = false;

            EditTaskTagsActivity.this.setProgressBarIndeterminateVisibility(true);
            EditTaskTagsActivity.this.listView.setEnabled(false);
        }

        protected List<TaskTag> doInBackground(Void... parameters) {
            return EditTaskTagsActivity.this.applicationLogic.getAllTaskTags(EditTaskTagsActivity.this.currentTaskContext);
        }

        protected void onPostExecute(List<TaskTag> tags) {
            EditTaskTagsActivity.this.updateTaskTagListInterface(tags);

            EditTaskTagsActivity.this.listView.setEnabled(true);
            EditTaskTagsActivity.this.setProgressBarIndeterminateVisibility(false);

            EditTaskTagsActivity.this.interfaceReady = true;
        }
    }
}
