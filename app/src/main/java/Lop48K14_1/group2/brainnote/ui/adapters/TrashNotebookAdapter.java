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
import Lop48K14_1.group2.brainnote.ui.models.Notebook;

public class TrashNotebookAdapter extends RecyclerView.Adapter<TrashNotebookAdapter.ViewHolder> {

    private Context context;
    private List<Notebook> deletedNotebooks;
    private OnTrashItemClickListener listener;

    // Constructor
    public TrashNotebookAdapter(Context context, List<Notebook> deletedNotebooks, OnTrashItemClickListener listener) {
        this.context = context;
        this.deletedNotebooks = deletedNotebooks;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for the item
        View view = LayoutInflater.from(context).inflate(R.layout.item_notebook_trash, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Notebook notebook = deletedNotebooks.get(position);
        holder.nameTextView.setText(notebook.getName());


        List<?> notes = notebook.getNotes();
        int count = (notes != null) ? notes.size() : 0;
        holder.countTextView.setText(String.valueOf(count));

        holder.recoverButton.setOnClickListener(v -> listener.onRestore(notebook));
        holder.deleteButton.setOnClickListener(v -> listener.onDeletePermanently(notebook));
    }

    @Override
    public int getItemCount() {
        return deletedNotebooks.size();
    }

    // ViewHolder class to hold the item views
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView;
        TextView countTextView;
        ImageView recoverButton;
        ImageView deleteButton;

        public ViewHolder(View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            countTextView = itemView.findViewById(R.id.countTextView);
            recoverButton = itemView.findViewById(R.id.recover);
            deleteButton = itemView.findViewById(R.id.delete);
        }
    }

    // Interface for handling click events (recover and delete)
    public interface OnTrashItemClickListener {
        void onRestore(Notebook notebook);
        void onDeletePermanently(Notebook notebook);
    }
}
