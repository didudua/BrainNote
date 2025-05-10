package Lop48K14_1.group2.brainnote.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.util.List;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Note;

public class TrashNoteAdapter extends RecyclerView.Adapter<TrashNoteAdapter.NoteViewHolder> {

    private List<Note> notes;
    private Context context;

    public TrashNoteAdapter(Context context, List<Note> notes, TrashNotebookAdapter.OnTrashItemClickListener listener) {
        this.context = context;
        this.notes = notes;
    }

    public interface OnTrashNoteClickListener {
        void onRestore(Note note);
        void onDeletePermanently(Note note);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_note_trash, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvContent.setText(note.getContent());

        // Gắn listener nếu cần
        holder.btnDelete.setOnClickListener(v -> {
            // Xử lý xóa vĩnh viễn
        });

        holder.btnRestore.setOnClickListener(v -> {
            // Xử lý khôi phục
        });
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    public static class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvContent;
        ImageView btnDelete, btnRestore;

        public NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvNoteTitle);
            tvContent = itemView.findViewById(R.id.tvNoteContent);
            btnDelete = itemView.findViewById(R.id.btnDeleteNote);
            btnRestore = itemView.findViewById(R.id.btnRestoreNote);
        }
    }
}