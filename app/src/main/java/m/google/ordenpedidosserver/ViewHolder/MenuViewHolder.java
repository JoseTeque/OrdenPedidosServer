package m.google.ordenpedidosserver.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import m.google.ordenpedidosserver.R;
import m.google.ordenpedidosserver.common.Common;
import m.google.ordenpedidosserver.interfaz.InterfaceClickListener;

public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnCreateContextMenuListener {

    public TextView txtNameMenu;
    public ImageView imageView;

    private InterfaceClickListener clickListener;

    public MenuViewHolder(@NonNull View itemView) {
        super(itemView);

        txtNameMenu=itemView.findViewById(R.id.IdTxtNameMenu);
        imageView=itemView.findViewById(R.id.IdImage);

        itemView.setOnCreateContextMenuListener(this);
        itemView.setOnClickListener(this);
    }

    public void setClickListener(InterfaceClickListener clickListener) {
        this.clickListener = clickListener;

    }

    @Override
    public void onClick(View view) {

        clickListener.onClick(view,getAdapterPosition(),false);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(" Select the action ");

        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(), Common.DELETE);
    }
}

