package Lop48K14_1.group2.brainnote.ui.Notebook;

import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.core.Context;

import java.util.ArrayList;
import java.util.List;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.adapters.NotebookAdapter;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;
import Lop48K14_1.group2.brainnote.ui.utils.DataProvider;
import Lop48K14_1.group2.brainnote.sync.JsonSyncManager;

public class NotebooksFragment extends Fragment implements NotebookAdapter.OnNotebookClickListener {

    private RecyclerView recyclerView;
    private NotebookAdapter adapter;
    private List<Notebook> notebooks;
    private List<Notebook> filteredNotebooks;
    private EditText searchEditText;
    private TextView notebookCountTextView;
    private FloatingActionButton addButton;
    private int swipedPosition = -1;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notebooks, container, false);

        // Khởi tạo views
        recyclerView = view.findViewById(R.id.recyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
        notebookCountTextView = view.findViewById(R.id.notebookCountTextView);
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
        addButton.setOnClickListener(v -> {
            NavController navController = NavHostFragment.findNavController(NotebooksFragment.this);
            navController.navigate(R.id.action_currentFragment_to_newNoteFragment);
        });

        // Thiết lập ItemTouchHelper cho vuốt trái và phải
        setupItemTouchHelper();

        // Đóng mục khi chạm ngoài
        recyclerView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_DOWN && swipedPosition != -1) {
                RecyclerView.ViewHolder holder = recyclerView.findViewHolderForAdapterPosition(swipedPosition);
                if (holder != null) {
                    float x = event.getX();
                    float y = event.getY();
                    View itemView = holder.itemView;

                    // Kiểm tra chạm vào nút Xóa
                    float buttonWidth = itemView.getWidth() * 0.15f;
                    float deleteLeft = itemView.getRight() - buttonWidth;
                    if (x >= deleteLeft && x <= itemView.getRight() && y >= itemView.getTop() && y <= itemView.getBottom()) {
                        onDeleteNotebook(filteredNotebooks.get(swipedPosition), swipedPosition);
                        swipedPosition = -1;
                        return true;
                    }

                    // Kiểm tra chạm vào nút Sửa
                    float editLeft = itemView.getRight() - 2 * buttonWidth;
                    float editRight = deleteLeft;
                    if (x >= editLeft && x <= editRight && y >= itemView.getTop() && y <= itemView.getBottom()) {
                        onEditNotebook(filteredNotebooks.get(swipedPosition), swipedPosition);
                        swipedPosition = -1;
                        return true;
                    }

                    // Đóng mục nếu chạm ngoài
                    itemView.animate().translationX(0).setDuration(100).start();
                    swipedPosition = -1;
                }
            }
            return false;
        });

        return view;
    }

    private void setupItemTouchHelper() {
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Không xử lý onSwiped để giữ mục mở
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                View itemView = viewHolder.itemView;
                float buttonWidth = itemView.getWidth() * 0.15f; // Mỗi nút chiếm 15% chiều rộng
                float maxDx = 2 * buttonWidth; // Tổng 30% chiều rộng mục

                // Xác định translationX dựa trên hướng vuốt
                float translationX;
                if (dX < 0) { // Vuốt trái
                    translationX = Math.max(dX, -maxDx);
                } else { // Vuốt phải
                    translationX = Math.min(dX, 0); // Không cho phép dịch chuyển sang trái quá 0
                }

                // Dịch chuyển itemView
                if (isCurrentlyActive) {
                    itemView.setTranslationX(translationX);
                } else {
                    itemView.animate().translationX(translationX).setDuration(100).start();
                }

                // Chỉ vẽ nút nếu đang vuốt trái (translationX < 0)
                if (translationX < 0) {
                    // Vẽ nền trắng cho vùng hành động (phía bên phải)
                    ColorDrawable background = new ColorDrawable(Color.WHITE);
                    background.setBounds((int) (itemView.getRight() - maxDx), itemView.getTop(), itemView.getRight(), itemView.getBottom());
                    background.draw(c);

                    // Vẽ nút Xóa (màu đỏ, bên phải)
                    Drawable deleteIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_delete);
                    ColorDrawable deleteBackground = new ColorDrawable(Color.parseColor("#FF0000"));
                    int deleteIntrinsicWidth = deleteIcon.getIntrinsicWidth();
                    int deleteIntrinsicHeight = deleteIcon.getIntrinsicHeight();

                    int deleteLeft = (int) (itemView.getRight() - buttonWidth);
                    int deleteRight = itemView.getRight();
                    int deleteTop = itemView.getTop();
                    int deleteBottom = itemView.getBottom();

                    deleteBackground.setBounds(deleteLeft, deleteTop, deleteRight, deleteBottom);
                    deleteBackground.draw(c);

                    int deleteIconMargin = (deleteBottom - deleteTop - deleteIntrinsicHeight) / 2;
                    int deleteIconTop = deleteTop + deleteIconMargin;
                    int deleteIconBottom = deleteIconTop + deleteIntrinsicHeight;
                    int deleteIconRight = deleteRight - deleteIconMargin;
                    int deleteIconLeft = deleteIconRight - deleteIntrinsicWidth;

                    deleteIcon.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
                    deleteIcon.draw(c);

                    // Vẽ nút Sửa (màu xanh, bên trái của nút Xóa)
                    Drawable editIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_edit);
                    ColorDrawable editBackground = new ColorDrawable(Color.parseColor("#4CAF50"));
                    int editIntrinsicWidth = editIcon.getIntrinsicWidth();
                    int editIntrinsicHeight = editIcon.getIntrinsicHeight();

                    int editLeft = (int) (itemView.getRight() - 2 * buttonWidth);
                    int editRight = deleteLeft;
                    int editTop = itemView.getTop();
                    int editBottom = itemView.getBottom();

                    editBackground.setBounds(editLeft, editTop, editRight, editBottom);
                    editBackground.draw(c);

                    int editIconMargin = (editBottom - editTop - editIntrinsicHeight) / 2;
                    int editIconTop = editTop + editIconMargin;
                    int editIconBottom = editIconTop + editIntrinsicHeight;
                    int editIconRight = editRight - editIconMargin;
                    int editIconLeft = editIconRight - editIntrinsicWidth;

                    editIcon.setBounds(editIconLeft, editIconTop, editIconRight, editIconBottom);
                    editIcon.draw(c);
                }

                super.onChildDraw(c, recyclerView, viewHolder, translationX, dY, actionState, isCurrentlyActive);

                // Lưu vị trí mục đang mở
                if (translationX < 0 && !isCurrentlyActive) {
                    swipedPosition = viewHolder.getAdapterPosition();
                } else if (translationX == 0) {
                    swipedPosition = -1;
                }
            }

            @Override
            public float getSwipeThreshold(@NonNull RecyclerView.ViewHolder viewHolder) {
                return 0.5f;
            }

            @Override
            public float getSwipeEscapeVelocity(float defaultValue) {
                return defaultValue * 0.5f;
            }

            @Override
            public float getSwipeVelocityThreshold(float defaultValue) {
                return defaultValue * 0.5f;
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
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

        Bundle args = new Bundle();
        args.putString("NOTEBOOK_ID", selectedNotebook.getId());
        NavController navController = NavHostFragment.findNavController(this);
        navController.navigate(R.id.action_notebooksFragment_to_notebookDetailFragment, args);
    }

    @Override
    public void onEditNotebook(Notebook notebook, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Chỉnh sửa sổ tay");

        final EditText input = new EditText(requireContext());
        input.setText(notebook.getName());
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (TextUtils.isEmpty(newName)) {
                Toast.makeText(getContext(), "Tên sổ tay không được để trống", Toast.LENGTH_SHORT).show();
                return;
            }

            notebook.setName(newName);
            adapter.notifyItemChanged(position);
            updateNotebookCount();

            JsonSyncManager.saveNotebooksToFile(requireContext());
            JsonSyncManager.uploadNotebooksToFirebase();
            Toast.makeText(getContext(), "Đã cập nhật sổ tay", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> {});
        builder.setCancelable(true);
        builder.show();
    }

    @Override
    public void onDeleteNotebook(Notebook notebook, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc muốn xóa sổ tay \"" + notebook.getName() + "\"? Hành động này không thể hoàn tác.")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Di chuyển vào Trash thay vì xóa hoàn toàn
                    JsonSyncManager.moveNotebookToTrash(requireContext(), notebook);

                    // Cập nhật UI
                    notebooks.remove(notebook);
                    filteredNotebooks.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateNotebookCount();

                    JsonSyncManager.saveNotebooksToFile(requireContext());
                    JsonSyncManager.uploadNotebooksToFirebase();
                    Toast.makeText(getContext(), "Sổ tay đã được chuyển vào thùng rác", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    @Override
    public void onResume() {
        super.onResume();
        notebooks = DataProvider.getNotebooks();
        filterNotebooks(searchEditText.getText().toString());
    }
}