package Lop48K14_1.group2.brainnote.ui.Home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.adapters.NoteAdapter;
import Lop48K14_1.group2.brainnote.ui.adapters.NotebookAdapter;
import Lop48K14_1.group2.brainnote.ui.adapters.TaskAdapter;
import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.models.Task;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;
import Lop48K14_1.group2.brainnote.sync.JsonSyncManager;

public class HomeFragment extends Fragment{

    private TextView dateTextView, notesHeader, notebooksHeader, tasksHeader;
    private EditText searchEditText;
    private RelativeLayout btnNewNote, btnNewTask, btnNewNotebook;

    private NavController navController;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Khởi tạo views
        dateTextView = view.findViewById(R.id.dateTextView);
        searchEditText = view.findViewById(R.id.searchEditText);
        btnNewNote = view.findViewById(R.id.home_new_note);
        btnNewTask = view.findViewById(R.id.home_new_task);
        btnNewNotebook = view.findViewById(R.id.home_new_notebook);

        // Thiết lập NavController
        navController = NavHostFragment.findNavController(this);

        // Format ngày
        String pattern = "EEEE, 'Ngày' dd 'Tháng' MM, yyyy";
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, new Locale("vi", "VN"));
        String formattedDate = sdf.format(new Date());
        formattedDate = formattedDate.substring(0, 1).toUpperCase() + formattedDate.substring(1);
        dateTextView.setText(formattedDate);




        // Thiết lập click listeners cho các nút
        btnNewNote.setOnClickListener(v -> navController.navigate(R.id.nav_new_note));
        btnNewTask.setOnClickListener(v -> navController.navigate(R.id.addTaskFragment));
        btnNewNotebook.setOnClickListener(v -> navController.navigate(R.id.nav_newnotebook));

        // Thêm HeaderFragment
        if (savedInstanceState == null) {
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.headerContainer, new HeaderFragment())
                    .commit();
        }

        return view;
    }


}