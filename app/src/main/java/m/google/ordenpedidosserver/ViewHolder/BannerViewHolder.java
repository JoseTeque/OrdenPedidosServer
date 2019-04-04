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

public class BannerViewHolder extends RecyclerView.ViewHolder implements  View.OnCreateContextMenuListener {

    public TextView txtNameBanner;
    public ImageView imageView;

    public BannerViewHolder(@NonNull View itemView) {
        super(itemView);

        txtNameBanner=itemView.findViewById(R.id.IdTxtNameBanner);
        imageView=itemView.findViewById(R.id.IdImageBanner);

        itemView.setOnCreateContextMenuListener(this);

    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        menu.setHeaderTitle(" Select the action ");

        menu.add(0,0,getAdapterPosition(), Common.UPDATE);
        menu.add(0,1,getAdapterPosition(), Common.DELETE);
    }
}