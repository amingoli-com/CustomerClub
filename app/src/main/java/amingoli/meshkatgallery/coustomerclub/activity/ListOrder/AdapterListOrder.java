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
import amingoli.meshkatgallery.coustomerclub.util.FaNum;
import amingoli.meshkatgallery.coustomerclub.util.Tools;

public class AdapterListOrder extends RecyclerView.Adapter<AdapterListOrder.ViewHolder> {

    private List<ModelListOrder> itemList;
    private listener listener;
    Context context;

    public AdapterListOrder(List<ModelListOrder> itemIntroList, AdapterListOrder.listener listener, Context context) {
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
        final ModelListOrder item = itemList.get(position);
        holder.no.setText(FaNum.convert(item.getNo()));
        holder.date.setText(Tools.getFormattedDateSimple2(Long.valueOf(item.getDate())));
        holder.price.setText(Tools.getForamtPrice(Integer.parseInt(item.getPrice())));

        holder.desc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.result(item.getId(), "view_desc",item.getDate(),item.getPrice(),item.getDesc());
            }
        });
        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.result(item.getId(), "edit",item.getDate(),item.getPrice(),item.getDesc());
            }
        });
        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                listener.result(item.getId(), "delete",item.getDate(),item.getPrice(),item.getDesc());
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
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
        void result(int id,String MODEL,String date,String price,String desc);
    }
}