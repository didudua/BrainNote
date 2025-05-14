package Lop48K14_1.group2.brainnote.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Notebook;

public class NotebookAdapter extends RecyclerView.Adapter<NotebookAdapter.NotebookViewHolder> {

    private List<Notebook> notebooks;
    private OnNotebookClickListener listener;

    public interface OnNotebookClickListener {
        void onNotebookClick(int position);
        void onEditNotebook(Notebook notebook, int position);
        void onDeleteNotebook(Notebook notebook, int position);
    }

    public NotebookAdapter(List<Notebook> notebooks, OnNotebookClickListener listener) {
        this.notebooks = notebooks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotebookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notebook, parent, false);
        return new NotebookViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotebookViewHolder holder, int position) {
        Notebook notebook = notebooks.get(position);
        holder.nameTextView.setText(notebook.getName());
        holder.countTextView.setText(String.valueOf(notebook.getNotes().size()));

        // Đặt lại trạng thái dịch chuyển ban đầu
        holder.itemView.setTranslationX(0);

        // Sự kiện nhấn vào mục
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                int pos = holder.getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION) {
                    listener.onNotebookClick(pos);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return notebooks.size();
    }
    public void updateNotebooks(List<Notebook> newNotebooks) {
        this.notebooks = newNotebooks != null ? new ArrayList<>(newNotebooks) : new ArrayList<>();
        notifyDataSetChanged();
    }

    // Phương thức để lấy danh sách sổ tay
    public List<Notebook> getNotebooks() {
        return new ArrayList<>(notebooks);
    }
    public class NotebookViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView nameTextView;
        TextView countTextView;

        public NotebookViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.iconImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            countTextView = itemView.findViewById(R.id.countTextView);
        }
    }
}