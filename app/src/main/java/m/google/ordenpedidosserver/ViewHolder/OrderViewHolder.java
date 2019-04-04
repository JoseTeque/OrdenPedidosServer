package m.google.ordenpedidosserver.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import m.google.ordenpedidosserver.R;
import m.google.ordenpedidosserver.common.Common;
import m.google.ordenpedidosserver.interfaz.InterfaceClickListener;

public class OrderViewHolder extends RecyclerView.ViewHolder  {

    public TextView OrderName, OrderPhone, OrderStatus, OrderAddress,txtDate;
    public Button btnEditar,btnRemover,btnDetalles,btnDireccion;

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        OrderName= itemView.findViewById(R.id.Id_Order_name);
        OrderStatus= itemView.findViewById(R.id.Id_Order_status);
        OrderPhone= itemView.findViewById(R.id.Id_Order_phone);
        OrderAddress= itemView.findViewById(R.id.Id_Order_address);
        txtDate= itemView.findViewById(R.id.Id_Date_Order);

        btnEditar=itemView.findViewById(R.id.IdBtnEdit);
        btnRemover=itemView.findViewById(R.id.IdBtnRemove);
        btnDetalles=itemView.findViewById(R.id.IdBtnDetalle);
        btnDireccion=itemView.findViewById(R.id.IdBtnDirection);


    }

}

