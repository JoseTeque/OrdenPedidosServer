package m.google.ordenpedidosserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.TextView;

import m.google.ordenpedidosserver.ViewHolder.OrderDetailAdapter;
import m.google.ordenpedidosserver.common.Common;

public class OrderDetailActivity extends AppCompatActivity {

     TextView order_id,order_phone,order_address,order_total,order_comment;
     String order_id_value="";
     RecyclerView recyclerView;
     RecyclerView.LayoutManager layoutManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);

        order_id= findViewById(R.id.Id_Order_id);
        order_phone= findViewById(R.id.Id_Order_phone);
        order_address= findViewById(R.id.Id_Order_address);
        order_total= findViewById(R.id.Id_Order_total);
        order_comment= findViewById(R.id.Id_Order_comment);

        recyclerView= findViewById(R.id.IdListaFoods);
        recyclerView.setHasFixedSize(true);
        layoutManager= new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        if (getIntent()!=null)
        {
            order_id_value = getIntent().getStringExtra("OrderId");
        }

        //set Value

        order_id.setText(order_id_value);
        order_phone.setText(Common.currentRequest.getPhone());
        order_address.setText(Common.currentRequest.getAddress());
        order_total.setText(Common.currentRequest.getTotal());
        order_comment.setText(Common.currentRequest.getComment());

        OrderDetailAdapter adapter= new OrderDetailAdapter(Common.currentRequest.getFoods());
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

    }
}
