package Lop48K14_1.group2.brainnote.ui.Home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Note;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class NoteDetailFragment extends Fragment {

    private TextView titleTextView;
    private TextView contentTextView;
    private TextView infoTextView;
    private ImageButton backButton;
    private ImageButton filterButton;
    private ImageButton menuButton;
    private Notebook notebook;
    private Note note;
    private String notebookId;
    private String noteId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_detail, container, false);

        // Lấy ID sổ tay và ghi chú từ arguments
        Bundle args = getArguments();
        if (args != null) {
            notebookId = args.getString("NOTEBOOK_ID");
            noteId = args.getString("NOTE_ID");
        }

        notebook = DataProvider.getNotebookById(notebookId);
        if (notebook != null) {
            note = notebook.getNoteById(noteId);
        }

        if (notebook == null || note == null) {
            // Quay lại fragment trước đó nếu không tìm thấy sổ tay hoặc ghi chú
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
            return view;
        }

        // Khởi tạo views
        titleTextView = view.findViewById(R.id.titleTextView);
        contentTextView = view.findViewById(R.id.contentTextView);
        infoTextView = view.findViewById(R.id.infoTextView);
        backButton = view.findViewById(R.id.backButton);
        filterButton = view.findViewById(R.id.filterButton);

        // Thiết lập dữ liệu
        titleTextView.setText(note.getTitle());
        contentTextView.setText(note.getContent());
        infoTextView.setText(notebook.getName() + " • " + note.getDate());

        // Thiết lập nút quay lại
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
            }
        });

        return view;
    }
}
