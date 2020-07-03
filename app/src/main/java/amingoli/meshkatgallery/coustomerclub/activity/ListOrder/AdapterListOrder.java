package amingoli.meshkatgallery.coustomerclub.activity.ListOrder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import amingoli.meshkatgallery.coustomerclub.R;

public class AdapterListOrder extends RecyclerView.Adapter<AdapterListOrder.ViewHolder> implements View.OnClickListener {


    private List<ListOrderModel> itemList;
    private listener listener;
    Context context;

    public AdapterListOrder(List<ListOrderModel> itemIntroList, AdapterListOrder.listener listener, Context context) {
        this.itemList = itemIntroList;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public AdapterListOrder.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_order, parent, false);
        return new AdapterListOrder.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdapterListOrder.ViewHolder holder, final int position) {
        ListOrderModel item = itemList.get(position);
        holder.no.setText(item.getNo());
        holder.date.setText(item.getDate());
        holder.price.setText(item.getPrice());

        holder.desc.setOnClickListener(this);
        holder.edit.setOnClickListener(this);
        holder.delete.setOnClickListener(this);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.desc:

                break;
            case R.id.edit:

                break;

            case R.id.delete:

                break;
        }
    }

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView no,price,date;
        ImageView desc,edit,delete;
        ViewHolder(View itemView) {
            super(itemView);
            no = itemView.findViewById(R.id.no);
            price = itemView.findViewById(R.id.price);
            date = itemView.findViewById(R.id.date);
            desc = itemView.findViewById(R.id.desc);
            edit = itemView.findViewById(R.id.edit);
            delete = itemView.findViewById(R.id.delete);
        }

    }

    public interface listener{
        void result(int pos);
    }
}