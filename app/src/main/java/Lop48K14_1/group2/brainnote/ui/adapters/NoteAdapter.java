package Lop48K14_1.group2.brainnote.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
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

public class NoteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Filterable {

    // View type constants
    public static final int VIEW_TYPE_TITLE = 0;
    public static final int VIEW_TYPE_SMALL = 1;
    public static final int VIEW_TYPE_LARGE = 2;
    public static final int VIEW_TYPE_GRID = 3;

    private List<Note> notes;
    private List<Note> notesFull;
    private OnNoteClickListener listener;
    private OnNoteDeleteListener deleteListener;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss, dd/MM/yyyy", Locale.getDefault());

    private int currentViewType = VIEW_TYPE_LARGE; // Default to large view (your original layout)

    // Giao tiếp click vào note
    public interface OnNoteClickListener {
        void onNoteClick(Note note);
    }

    public interface OnNoteDeleteListener {
        void onNoteDelete(Note note, int position);
    }

    public NoteAdapter(List<Note> notes, OnNoteClickListener listener, OnNoteDeleteListener deleteListener) {
        this.listener = listener;
        this.deleteListener = deleteListener;
        this.notesFull = new ArrayList<>(notes);
        sortByDateDesc(notesFull);
        this.notes = new ArrayList<>(notesFull);
    }

    public void setViewType(int viewType) {
        if (this.currentViewType != viewType) {
            this.currentViewType = viewType;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemViewType(int position) {
        return currentViewType;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_TITLE:
                View titleView = inflater.inflate(R.layout.item_note_title, parent, false);
                return new TitleViewHolder(titleView);

            case VIEW_TYPE_SMALL:
                View smallView = inflater.inflate(R.layout.item_note_small, parent, false);
                return new SmallViewHolder(smallView);

            case VIEW_TYPE_GRID:
                View gridView = inflater.inflate(R.layout.item_note_grid, parent, false);
                return new GridViewHolder(gridView);

            case VIEW_TYPE_LARGE:
            default:
                // Use your existing layout for large view
                View largeView = inflater.inflate(R.layout.item_note_large, parent, false);
                return new LargeViewHolder(largeView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Note note = notes.get(position);

        try {
            switch (holder.getItemViewType()) {
                case VIEW_TYPE_TITLE:
                    TitleViewHolder titleHolder = (TitleViewHolder) holder;
                    titleHolder.titleTextView.setText(note.getTitle());
                    titleHolder.dateTextView.setText(note.getDate());
                    break;

                case VIEW_TYPE_SMALL:
                    SmallViewHolder smallHolder = (SmallViewHolder) holder;
                    smallHolder.titleTextView.setText(note.getTitle());
                    // Get first 50 chars of content for preview
                    String preview = note.getContent();
                    if (preview.length() > 50) {
                        preview = preview.substring(0, 50) + "...";
                    }
                    smallHolder.contentPreviewTextView.setText(preview);
                    smallHolder.dateTextView.setText(note.getDate());
                    break;

                case VIEW_TYPE_GRID:
                    GridViewHolder gridHolder = (GridViewHolder) holder;
                    gridHolder.titleTextView.setText(note.getTitle());
                    // Get first 100 chars of content for preview in grid
                    String gridPreview = note.getContent();
                    if (gridPreview.length() > 100) {
                        gridPreview = gridPreview.substring(0, 100) + "...";
                    }
                    gridHolder.contentPreviewTextView.setText(gridPreview);
                    break;

                case VIEW_TYPE_LARGE:
                default:
                    LargeViewHolder largeHolder = (LargeViewHolder) holder;
                    largeHolder.titleTextView.setText(note.getTitle());
                    largeHolder.contentTextView.setText(note.getContent());
                    largeHolder.dateTextView.setText(note.getDate());
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        notifyItemRangeChanged(position, notes.size());
    }

    // Original ViewHolder for large view - using your existing IDs
    public class LargeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentTextView, dateTextView;
        Button deleteNoteButton;

        public LargeViewHolder(@NonNull View v) {
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

    // New ViewHolder for title-only view
    public class TitleViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dateTextView;
        View deleteButton;

        public TitleViewHolder(@NonNull View v) {
            super(v);
            titleTextView = v.findViewById(R.id.tvNoteTitle);
            dateTextView = v.findViewById(R.id.tvNoteTime);
            deleteButton = v.findViewById(R.id.btn_delete_note);

            v.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNoteClick(notes.get(pos));
                }
            });

            if (deleteButton != null) {
                deleteButton.setOnClickListener(view -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && deleteListener != null) {
                        deleteListener.onNoteDelete(notes.get(pos), pos);
                    }
                });
            }
        }
    }

    // New ViewHolder for small view
    public class SmallViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentPreviewTextView, dateTextView;

        public SmallViewHolder(@NonNull View v) {
            super(v);
            titleTextView = v.findViewById(R.id.tvNoteTitle);
            contentPreviewTextView = v.findViewById(R.id.tvNoteContent);
            dateTextView = v.findViewById(R.id.tvNoteTime);

            v.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNoteClick(notes.get(pos));
                }
            });

        }
    }

    // New ViewHolder for grid view
    public class GridViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, contentPreviewTextView;
        View deleteButton;

        public GridViewHolder(@NonNull View v) {
            super(v);
            titleTextView = v.findViewById(R.id.tvNoteTitle);
            contentPreviewTextView = v.findViewById(R.id.tvNoteContent);
            deleteButton = v.findViewById(R.id.btn_delete_note);

            v.setOnClickListener(view -> {
                int pos = getAdapterPosition();
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    listener.onNoteClick(notes.get(pos));
                }
            });

            if (deleteButton != null) {
                deleteButton.setOnClickListener(view -> {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION && deleteListener != null) {
                        deleteListener.onNoteDelete(notes.get(pos), pos);
                    }
                });
            }
        }
    }
}
