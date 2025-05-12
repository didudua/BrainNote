package Lop48K14_1.group2.brainnote.ui.Notebook;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;

public class MoreOptionsBottomSheetNoteBook extends BottomSheetDialogFragment {
    private TextView tittleMainNote;
    private Notebook notebookDefault;
    private List<Notebook> notebooks;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notebooks_default, container, false);
        tittleMainNote = view.findViewById(R.id.tittleMainNote);

        // Lấy UID người dùng hiện tại từ FirebaseAuth
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Lấy dữ liệu sổ tay từ Firebase
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("backup_json");

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Kiểm tra nếu dữ liệu tồn tại và chứa "notebooks"
                for (DataSnapshot notebookSnapshot : snapshot.child("notebooks").getChildren()) {
                    // Lấy thông tin sổ tay
                    boolean isDefault = notebookSnapshot.child("Default").getValue(Boolean.class);
                    String name = notebookSnapshot.child("name").getValue(String.class);

                    // Kiểm tra nếu sổ tay là mặc định
                    if (isDefault) {
                        tittleMainNote.setText(name); // Hiển thị tên sổ tay mặc định
                        break; // Không cần kiểm tra tiếp
                    } else {
                        tittleMainNote.setText("Không có sổ tay mặc định");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi trong quá trình lấy dữ liệu
                tittleMainNote.setText("Lỗi khi lấy dữ liệu");
            }
        });

        // Get the list of notebooks from DataProvider
        notebooks = DataProvider.getNotebooks();
        for (Notebook notebook : notebooks) {
            if (notebook.getDefault()) {
                notebookDefault = notebook;
                Log.d("NotebookInfo", "Sổ tay mặc định: " + notebookDefault.getName());
                break;
            }
        }

        tittleMainNote.setOnClickListener(v -> {
            // Prepare the names array to show in the dialog
            String[] names = new String[notebooks.size()];
            for (int i = 0; i < notebooks.size(); i++) {
                names[i] = notebooks.get(i).getName();
            }
            new AlertDialog.Builder(requireContext())
                    .setTitle("Cài sổ tay mặc định")
                    .setItems(names, (dialog, which) -> {
                        Notebook selectedNotebook = notebooks.get(which);
                        tittleMainNote.setText(selectedNotebook.getName());

//                        // Update the local notebook data
//                        notebookDefault.setDefault(false);
//                        selectedNotebook.setDefault(true);
//                        DataProvider.updateNotebook(notebookDefault);
//                        DataProvider.updateNotebook(selectedNotebook);

                    })
                    .show();
        });

        return view;
    }

}
