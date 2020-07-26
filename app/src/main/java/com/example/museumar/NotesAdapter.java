package com.example.museumar;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.CustomViewHolder> {

    private Context mContext;
    private List<String> mUploads;
    private OnItemClickListener mListener;

    public NotesAdapter(Context context, List<String> uploads) {
        this.mContext = context;
        this.mUploads = uploads;
    }
    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.notes_activity, parent, false);
        return new CustomViewHolder(v);
    }

    // add each note on notes list on the holder
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {
        String uploadCurrent = mUploads.get(position);
        holder.textViewNotes.setText(uploadCurrent);
    }

    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener{

        private TextView textViewNotes;

        public CustomViewHolder(View view) {
            super(view);

            textViewNotes = view.findViewById(R.id.text_view_notes);
            textViewNotes.setOnClickListener(this);
            textViewNotes.setOnCreateContextMenuListener(this);

        }

        // determine position of item clicked in action menu
        @Override
        public void onClick(View view) {
            if (mListener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    mListener.onItemClickNotes(position);
                }
            }
        }

        // create action menu
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Select Action");
            MenuItem edit = contextMenu.add(Menu.NONE, 1, 1, "Edit");
            MenuItem delete = contextMenu.add(Menu.NONE, 2, 2, "Delete");

            edit.setOnMenuItemClickListener(this);
            delete.setOnMenuItemClickListener(this);
        }

        // handle action menu action
        @Override
        public boolean onMenuItemClick(MenuItem menuItem) {
            if (mListener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){

                    switch (menuItem.getItemId()) {
                        case 1:
                            mListener.onEditClickNotes(position);
                            return true;
                        case 2:
                            mListener.onDeleteClickNotes(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClickNotes(int position);

        void onEditClickNotes(int position);

        void onDeleteClickNotes(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) { mListener = listener; }
}
