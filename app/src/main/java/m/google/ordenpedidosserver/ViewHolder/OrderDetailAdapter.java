package m.google.ordenpedidosserver.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import m.google.ordenpedidosserver.R;
import m.google.ordenpedidosserver.model.Order;

class MyViewHolder extends RecyclerView.ViewHolder{

    public TextView name,price,quantity,discount;

    public MyViewHolder(@NonNull View itemView) {
        super(itemView);

        name=itemView.findViewById(R.id.Id_product_name);
        price=itemView.findViewById(R.id.Id_price_product);
        quantity=itemView.findViewById(R.id.Id_product_quantity);
        discount=itemView.findViewById(R.id.Id_product_discount);
    }
}

public class OrderDetailAdapter extends RecyclerView.Adapter<MyViewHolder> {

    List<Order> myOrders;

    public OrderDetailAdapter(List<Order> myOrders) {
        this.myOrders = myOrders;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view= LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_detail_layout,viewGroup,false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder myViewHolder, int i) {
        Order order= myOrders.get(i);
        myViewHolder.name.setText(String.format("Name: %s",order.getNombreProducto()));
        myViewHolder.price.setText(String.format("Precio: %s",order.getPrecio()));
        myViewHolder.quantity.setText(String.format("Quantity: %s",order.getCantidad()));
        myViewHolder.discount.setText(String.format("Descuento: %s",order.getDescuento()));

    }

    @Override
    public int getItemCount() {
        return myOrders.size();
    }
}
