package application.picturevoice.listadapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;
import application.picturevoice.R;
import application.picturevoice.classes.CloudFile;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.MyViewHolder> {
    private ArrayList<CloudFile> mDataset;
    private View.OnClickListener onItemClickListener;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder

    public class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView textViewFileName, textViewFileSize;

        public MyViewHolder(View v) {
            super(v);
            textViewFileName = v.findViewById(R.id.textViewFileName);
            textViewFileSize = v.findViewById(R.id.textViewFileSize);
            v.setTag(this);
            v.setOnClickListener(onItemClickListener);
        }
    }


    public void setOnItemClickListener(View.OnClickListener clickListener) {
        onItemClickListener = clickListener;
    }

    //provide a suitable constructor (depends on the kind of dataset)
    public ProfileAdapter(ArrayList<CloudFile> myDataset) {
        mDataset = myDataset;
    }

    //create new views (invoked by the layout manager)
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.cloud_storage_item, parent, false);

        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    //replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        //get element from your dataset at this position
        //replace the contents of the view with that element
        holder.textViewFileSize.setText(mDataset.get(position).getFileSize());
        holder.textViewFileName.setText(mDataset.get(position).getFileName());
    }

    //return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }
}
