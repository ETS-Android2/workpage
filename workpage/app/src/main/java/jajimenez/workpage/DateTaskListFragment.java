package jajimenez.workpage;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;

import java.util.LinkedList;
import java.util.List;

import jajimenez.workpage.data.model.Task;

public class DateTaskListFragment extends Fragment implements TaskContainerFragment {
    private ListView list;

    private Bundle savedInstanceState;
    private TaskListHostActivity activity;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            activity = (TaskListHostActivity) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() +
                    " must implement TaskListHostActivity");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.savedInstanceState = savedInstanceState;
        View view = inflater.inflate(R.layout.date_task_list, container, false);

        list = view.findViewById(R.id.date_task_list_list);
        list.setVisibility(View.INVISIBLE);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView l, View v, int position, long id) {
                Task task = (Task) l.getItemAtPosition(position);
                if (DateTaskListFragment.this.activity != null) DateTaskListFragment.this.activity.onTaskClicked(task);
            }
        });

        createContextualActionBar();
        setSelectedTasks();

        return view;
    }

    private void createContextualActionBar() {
        list.clearChoices();
        list.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        list.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                DateTaskListFragment.this.activity.setActionMode(mode);

                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.task, menu);

                return true;
            }

            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            public void onDestroyActionMode(ActionMode mode) {
                DateTaskListFragment.this.activity.setActionMode(null);
            }

            // Returns "true" if this callback handled the event, "false"
            // if the standard "MenuItem" invocation should continue.
            public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
                boolean eventHandled = false;
                final List<Task> selectedTasks = getSelectedTasks();

                switch (item.getItemId()) {
                    case R.id.task_menu_status:
                        if (DateTaskListFragment.this.activity != null)  {
                            DateTaskListFragment.this.activity.showChangeTaskStatusDialog(selectedTasks);
                        }

                        eventHandled = true;
                        break;

                    case R.id.task_menu_edit:
                        // Open the task edition activity
                        if (DateTaskListFragment.this.activity != null)  {
                            DateTaskListFragment.this.activity.showEditActivity(selectedTasks.get(0));
                        }

                        // Close the context action bar
                        mode.finish();

                        eventHandled = true;
                        break;

                    case R.id.task_menu_delete:
                        // Show a deletion confirmation dialog
                        if (DateTaskListFragment.this.activity != null)  {
                            DateTaskListFragment.this.activity.showDeleteTaskDialog(selectedTasks);
                        }

                        eventHandled = true;
                        break;
                }

                return eventHandled;
            }

            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                int selectedTaskCount = list.getCheckedItemCount();
                if (selectedTaskCount > 0) mode.setTitle((DateTaskListFragment.this.getActivity()).getString(R.string.selected, selectedTaskCount));

                MenuItem editItem = (mode.getMenu()).findItem(R.id.task_menu_edit);
                Drawable editItemIcon = editItem.getIcon();

                MenuItem deleteItem = (mode.getMenu()).findItem(R.id.task_menu_delete);
                Drawable deleteItemIcon = deleteItem.getIcon();

                if (selectedTaskCount == 1) {
                    editItem.setEnabled(true);
                    editItemIcon.setAlpha(255);
                }
                else {
                    editItem.setEnabled(false);
                    editItemIcon.setAlpha(127);
                }

                deleteItem.setEnabled(true);
                deleteItemIcon.setAlpha(255);
            }
        });
    }

    public void setTasks(List<Task> tasks) {
        updateInterface(tasks);
    }

    private void updateInterface(List<Task> tasks) {
        if (tasks == null) tasks = new LinkedList<>();

        TaskAdapter adapter = new TaskAdapter(getActivity(), R.layout.task_list_item, tasks);
        list.setAdapter(adapter);

        if (adapter.isEmpty()) {
            list.setVisibility(View.GONE);
        }
        else {
            // Re-select items
            if (savedInstanceState != null) {
                int[] selectedItems = savedInstanceState.getIntArray("selected_items");

                if (selectedItems != null) {
                    for (int position : selectedItems) list.setItemChecked(position, true);
                    savedInstanceState.remove("selected_items");
                }
            }

            list.setVisibility(View.VISIBLE);
        }

        setListHeight();
    }

    private void setSelectedTasks() {
        ListAdapter adapter = list.getAdapter();

        if (adapter != null && !adapter.isEmpty() && savedInstanceState != null) {
            int[] selectedItems = savedInstanceState.getIntArray("selected_items");

            if (selectedItems != null) {
                for (int position : selectedItems) list.setItemChecked(position, true);
                savedInstanceState.remove("selected_items");
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        List<Integer> selectedItems = getSelectedItems();
        int selectedItemCount = selectedItems.size();
        int[] selected = new int[selectedItemCount];

        for (int i = 0; i < selectedItemCount; i++) selected[i] = selectedItems.get(i);
        outState.putIntArray("selected_items", selected);

        ActionMode mode = activity.getActionMode();
        if (mode != null) mode.finish();

        super.onSaveInstanceState(outState);
    }

    private List<Integer> getSelectedItems() {
        List<Integer> selectedItems = new LinkedList<>();

        SparseBooleanArray itemSelectedStates = list.getCheckedItemPositions();
        int itemCount = list.getCount();

        for (int i = 0; i < itemCount; i++) {
            if (itemSelectedStates.get(i)) {
                // The item with position "i" is selected.
                selectedItems.add(i);
            }
        }

        return selectedItems;
    }

    private List<Task> getSelectedTasks() {
        List<Task> selectedTasks = new LinkedList<>();

        TaskAdapter adapter = (TaskAdapter) list.getAdapter();
        List<Integer> selectedItems = getSelectedItems();

        for (int position : selectedItems) {
            Task task = adapter.getItem(position);
            selectedTasks.add(task);
        }

        return selectedTasks;
    }

    @Override
    public void setVisible(boolean visible) {
        View root = getView();

        if (visible) root.setVisibility(View.VISIBLE);
        else root.setVisibility(View.GONE);
    }

    // Method to set the height of a list view when it's inside a scroll view
    private void setListHeight() {
        ListAdapter adapter = list.getAdapter();
        if (adapter == null) return;

        int width = View.MeasureSpec.makeMeasureSpec(list.getWidth(), View.MeasureSpec.UNSPECIFIED);

        int height = 0;
        int itemCount = adapter.getCount();
        View view = null;

        for (int i = 0; i < itemCount; i++) {
            view = adapter.getView(i, view, list);

            if (i == 0) view.setLayoutParams(new ViewGroup.LayoutParams(width, AbsListView.LayoutParams.MATCH_PARENT));
            view.measure(width, View.MeasureSpec.UNSPECIFIED);

            height += view.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = list.getLayoutParams();
        params.height = height + (list.getDividerHeight() * (adapter.getCount() - 1));

        list.setLayoutParams(params);
    }
}
