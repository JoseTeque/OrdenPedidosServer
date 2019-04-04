package m.google.ordenpedidosserver.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import info.hoang8f.widget.FButton;
import m.google.ordenpedidosserver.R;
import m.google.ordenpedidosserver.interfaz.InterfaceClickListener;

public class ShipperViewHolder  extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView txtName,txtPhone;
    public FButton btnEditar,btnRemover;
    private InterfaceClickListener clickListener;

    public ShipperViewHolder(@NonNull View itemView) {
        super(itemView);

        txtName= itemView.findViewById(R.id.Id_shipper_name);
        txtPhone= itemView.findViewById(R.id.Id_phone_shipper);
        btnEditar= itemView.findViewById(R.id.IdBtnEditshipper);
        btnRemover= itemView.findViewById(R.id.IdBtnRemoverhipper);
    }

    public void setClickListener(InterfaceClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        clickListener.onClick(v,getAdapterPosition(),false);

    }
}
