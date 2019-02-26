package application.picturevoice.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import application.picturevoice.R;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.MyViewHolder> {
    private String[] mDataset;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView textView;

        public MyViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.itemTitle);
        }
    }

    //provide a suitable constructor (depends on the kind of dataset)
    public ProfileAdapter(String[] myDataset) {
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
        holder.textView.setText(mDataset[position]);

    }

    //return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}