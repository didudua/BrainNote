package Lop48K14_1.group2.brainnote.ui.adapters;

import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Lop48K14_1.group2.brainnote.R;
import Lop48K14_1.group2.brainnote.ui.models.Note;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteViewHolder> implements Filterable {

    private List<Note> notes;
    private List<Note> notesFull;
    private OnNoteClickListener listener;
    private OnNoteDeleteListener deleteListener;  // Lắng nghe sự kiện xóa
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss, dd/MM/yyyy", Locale.getDefault());

    // Giao tiếp click vào note
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public interface OnNoteDeleteListener {
        void onNoteDelete(Note note, int position);
    }

    public NoteAdapter(List<Note> notes, OnNoteClickListener listener, OnNoteDeleteListener deleteListener) {
        this.listener = listener;
        this.deleteListener = deleteListener; // Khởi tạo deleteListener
        this.notesFull = new ArrayList<>(notes);
        sortByDateDesc(notesFull);
        this.notes = new ArrayList<>(notesFull);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_note, parent, false);
        return new NoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note note = notes.get(position);
        holder.titleTextView.setText(note.getTitle());
        holder.contentTextView.setText(note.getContent());
        holder.dateTextView.setText(note.getDate());
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public Filter getFilter() {
        return noteFilter;
    }

    private final Filter noteFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Note> filteredList = new ArrayList<>();
            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(notesFull);
            } else {
                String f = constraint.toString().toLowerCase().trim();
                for (Note n : notesFull) {
                    if (n.getTitle().toLowerCase().contains(f) || n.getContent().toLowerCase().contains(f)) {
                        filteredList.add(n);
                    }
                }
            }
            sortByDateDesc(filteredList);
            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            notes.clear();
            notes.addAll((List<Note>) results.values);
            notifyDataSetChanged();
        }
    };

    private void sortByDateDesc(List<Note> list) {
        Collections.sort(list, (n1, n2) -> {
            try {
                Date d1 = sdf.parse(n1.getDate());
                Date d2 = sdf.parse(n2.getDate());
                return d2.compareTo(d1);
            } catch (ParseException e) {
                return 0;
            }
        });
    }

    public Note getNoteAt(int position) {
        return notes.get(position);
    }

    public void removeAt(int position) {
        notes.remove(position);
        notifyItemRemoved(position);
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, dateTextView;
        Button deleteNoteButton;

        public NoteViewHolder(@NonNull View v) {
            super(v);
            titleTextView = v.findViewById(R.id.tvNoteTitle);
            contentTextView = v.findViewById(R.id.tvNoteContent);
            dateTextView = v.findViewById(R.id.tvNoteTime);
            deleteNoteButton = v.findViewById(R.id.btn_delete_note);

            v.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNoteClick(notes.get(pos));
                }
            });

            deleteNoteButton.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && deleteListener != null) {
                    deleteListener.onNoteDelete(notes.get(pos), pos);
                }

            });
        }
    }
}

