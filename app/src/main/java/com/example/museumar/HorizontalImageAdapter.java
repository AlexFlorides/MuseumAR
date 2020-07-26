package com.example.museumar;

import android.content.Context;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class HorizontalImageAdapter extends RecyclerView.Adapter<HorizontalImageAdapter.CustomViewHolder> {

    private Context mContext;
    private List<String> mUploads;
    private HorizontalImageAdapter.OnItemClickListener mListener;

    public HorizontalImageAdapter(Context context, List<String> uploads) {
        this.mContext = context;
        this.mUploads = uploads;
    }
    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(mContext).inflate(R.layout.horizontal_image, parent, false);
        return new CustomViewHolder(v);
    }
    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder holder, int position) {

        String uploadCurrent = mUploads.get(position);

        // handle each photo of album using picasso library
        Picasso.with(mContext)
                .load(uploadCurrent)
                .placeholder(R.mipmap.ic_launcher)
                .fit()
                .centerCrop()
                .rotate(90)
                .into(holder.itemImage);
    }
    @Override
    public int getItemCount() {
        return mUploads.size();
    }

    public class CustomViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener{
        private ImageView itemImage;

        public CustomViewHolder(View view) {
            super(view);
            itemImage = view.findViewById(R.id.horizontal_image);
            itemImage.setOnClickListener(this);
            itemImage.setOnCreateContextMenuListener(this);
        }

        // determine position of item clicked in action menu
        @Override
        public void onClick(View view) {
            if (mListener != null){
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION){
                    mListener.onItemClickHorizontal(position);
                }
            }
        }

        // create action menu
        @Override
        public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
            contextMenu.setHeaderTitle("Select Action");
            MenuItem delete = contextMenu.add(Menu.NONE, 1, 1, "Delete");

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
                            mListener.onDeleteClickHorizontal(position);
                            return true;
                    }
                }
            }
            return false;
        }
    }

    public interface OnItemClickListener {
        void onItemClickHorizontal(int position);
        void onDeleteClickHorizontal(int position);
    }

    public void setOnItemClickListener(HorizontalImageAdapter.OnItemClickListener listener) { mListener = listener; }

}
