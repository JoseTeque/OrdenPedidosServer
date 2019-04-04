package m.google.ordenpedidosserver;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.HashMap;
import java.util.Map;

import info.hoang8f.widget.FButton;
import m.google.ordenpedidosserver.common.Common;
import m.google.ordenpedidosserver.model.DataMessage;
import m.google.ordenpedidosserver.model.MyResponse;
import m.google.ordenpedidosserver.remote.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageActivity extends AppCompatActivity {

    private MaterialEditText edtxTitle,edtxMessage;
    private FButton btnsend;

    ApiService mService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        edtxTitle= findViewById(R.id.IdTitleMesaage);
        edtxMessage= findViewById(R.id.IdMessage);
        btnsend= findViewById(R.id.IdBtnMessage);

        mService= Common.getFCMservice();

        btnsend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Map<String,String> datasend= new HashMap<>();
                datasend.put("Title",edtxTitle.getText().toString());
                datasend.put("Message",edtxMessage.getText().toString()  );

                DataMessage dataMessage= new DataMessage();
                dataMessage.to = new StringBuilder("/topics/").append(Common.topicName).toString();
                dataMessage.data = datasend;

                mService.sendNotification(dataMessage).enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                        if (response.isSuccessful())
                           Toast.makeText(MessageActivity.this, "Message send", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<MyResponse> call, Throwable t) {
                        Toast.makeText(MessageActivity.this, "" + t.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                });
            }
        });


    }
}
