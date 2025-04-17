package Lop48K14_1.group2.brainnote.ui.Notebook;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import Lop48K14_1.group2.brainnote.MainActivity;
import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.adapters.NotebookAdapter;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class NotebooksFragment extends Fragment implements NotebookAdapter.OnNotebookClickListener {

    private RecyclerView recyclerView;
    private NotebookAdapter adapter;
    private List<Notebook> notebooks;
    private List<Notebook> filteredNotebooks;
    private EditText searchEditText;
    private TextView notebookCountTextView;
    private FloatingActionButton addButton;

    private FloatingActionButton addNoteBookBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notebooks, container, false);

        // Khởi tạo views
        recyclerView = view.findViewById(R.id.recyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
        notebookCountTextView = view.findViewById(R.id.notebookCountTextView);
        addNoteBookBtn = view.findViewById(R.id.addButton);
        addButton = view.findViewById(R.id.addButton);
        // Thiết lập RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Lấy dữ liệu
        notebooks = DataProvider.getNotebooks();
        filteredNotebooks = new ArrayList<>(notebooks);

        // Thiết lập adapter
        adapter = new NotebookAdapter(filteredNotebooks, this);
        recyclerView.setAdapter(adapter);

        // Cập nhật số lượng sổ tay
        updateNotebookCount();

        // Thiết lập tìm kiếm
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterNotebooks(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Thiết lập nút thêm mới
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = NavHostFragment.findNavController(NotebooksFragment.this);
                navController.navigate(R.id.action_currentFragment_to_newNoteFragment);
            }
        });

        return view;
    }

    private void filterNotebooks(String query) {
        filteredNotebooks.clear();

        if (query.isEmpty()) {
            filteredNotebooks.addAll(notebooks);
        } else {
            query = query.toLowerCase();
            for (Notebook notebook : notebooks) {
                if (notebook.getName().toLowerCase().contains(query)) {
                    filteredNotebooks.add(notebook);
                }
            }
        }

        adapter.notifyDataSetChanged();
        updateNotebookCount();
    }

    private void updateNotebookCount() {
        notebookCountTextView.setText(filteredNotebooks.size() + " sổ tay");
    }

    @Override
    public void onNotebookClick(int position) {
        Notebook selectedNotebook = filteredNotebooks.get(position);
        if (selectedNotebook == null) return;

        // Sử dụng Navigation Component để điều hướng
        Bundle args = new Bundle();
        args.putString("NOTEBOOK_ID", selectedNotebook.getId());


        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_notebooksFragment_to_notebookDetailFragment, args);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Cập nhật lại dữ liệu khi quay lại fragment
        notebooks = DataProvider.getNotebooks();
        filterNotebooks(searchEditText.getText().toString());
    }
}
