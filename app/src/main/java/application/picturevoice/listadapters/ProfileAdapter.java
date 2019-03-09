package application.picturevoice.listadapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import application.picturevoice.R;
import application.picturevoice.classes.CloudFile;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.MyViewHolder> {
    private CloudFile[] mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView textViewFileName, textViewFileSize;


        public MyViewHolder(View v) {
            super(v);
            textViewFileName = v.findViewById(R.id.textViewFileName);
            textViewFileSize = v.findViewById(R.id.textViewFileSize);
        }
    }

    //provide a suitable constructor (depends on the kind of dataset)
    public ProfileAdapter(CloudFile[] myDataset) {
        mDataset = myDataset;
    }

    //create new views (invoked by the layout manager)
    @Override
    public ProfileAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
                                                          int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cloud_storage_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    //replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //get element from your dataset at this position
        //replace the contents of the view with that element
        holder.textViewFileSize.setText(mDataset[position].getFileSize());
        holder.textViewFileName.setText(mDataset[position].getFileName());
    }

    //return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}