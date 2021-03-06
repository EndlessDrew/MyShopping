package zhouqi.com.myshopping.Presentation.main.myself;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import org.hybridsquad.android.library.CropHandler;
import org.hybridsquad.android.library.CropHelper;
import org.hybridsquad.android.library.CropParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import zhouqi.com.myshopping.Model.enity.UserEntity;
import zhouqi.com.myshopping.Network.ShopApi;
import zhouqi.com.myshopping.R;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    /**
     * 注册界面内容
     */
    //邮箱或者手机号
    private EditText email;
    //第一次输入密码
    private EditText firstPsd;
    //第二次输入密码
    private EditText secondPsd;
    //注册按钮
    private Button bt_register;
    //上传图片
    private ImageView icon;
    //昵称
    private EditText nickname;

    private Handler handler;
    private View v;
    PopupWindow pp;
    Button popupTake,popupPhoto,popupCancel;
    private CropParams mCropParams;
    private CropHandler mCropHandler;
    private Context mContext;
    private Bitmap bitmap = null;
    private File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mContext= this;
        intiView();
        pp = new PopupWindow(this);
        initPopupWindow();
        popupTake = (Button) v.findViewById(R.id.btn_personalinfo_popup_take);
        popupPhoto = (Button) v.findViewById(R.id.btn_personalinfo_popup_photo);
        popupCancel = (Button) v.findViewById(R.id.btn_personalinfo_popup_cancel);
        CheckResult();
        mCropParams = new CropParams();
        setListern();
        initCropHelper();
    }

    private void intiView() {
        email = (EditText) findViewById(R.id.et_login_email);
        firstPsd = (EditText) findViewById(R.id.et_login_first_passwrod);
        secondPsd = (EditText) findViewById(R.id.et_login_second_password);
        bt_register = (Button) findViewById(R.id.bt_register);
        icon = (ImageView) findViewById(R.id.iv_login_icon);
        icon.setOnClickListener(this);
        nickname = (EditText) findViewById(R.id.et_login_nickname);

    }

    public void setListern() {
        icon.setOnClickListener(this);
        bt_register.setOnClickListener(this);
        popupTake.setOnClickListener(this);
        popupPhoto.setOnClickListener(this);
        popupCancel.setOnClickListener(this);
    }

    /**
     * handler 更新UI
     */
    private void CheckResult() {
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        Toast.makeText(LoginActivity.this, "注册成功，请妥善保存账号密码", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, LandActivity.class));
                        break;
                    case 2:
                        Toast.makeText(LoginActivity.this, "用户名已经被注册", Toast.LENGTH_SHORT).show();
                }
            }
        };
    }


    /**
     * 注册账号
     */

    private void registerUser() {
        OkHttpClient okHttpClient = new OkHttpClient();
        String path = "http://wx.feicuiedu.com:9094/yitao/UserWeb?method=register&username=" +
                email.getText().toString().trim() + "&password=" + firstPsd.getText().toString().trim();
        final Request request = new Request.Builder().url(path).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                try {
                    JSONObject jsonObject = new JSONObject(result);
                    int s = jsonObject.getInt("code");
                    Message message = handler.obtainMessage();
                    message.what = s;
                    handler.sendMessage(message);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
/**
 * 初始化PopupWindow
 *
 */
    private  void initPopupWindow(){
        v = getLayoutInflater().inflate(R.layout.popup_register,null);
        pp.setContentView(v);
        pp.setWindowLayoutMode(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        pp.setFocusable(true);
        pp.setTouchable(true);
        pp.setOutsideTouchable(true);
        pp.setBackgroundDrawable(new BitmapDrawable());
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case  R.id.iv_login_icon:
                if (pp != null && pp.isShowing()) {
                    pp.dismiss();
                } else {
                  pp.showAsDropDown(bt_register);
                }
                break;
            case  R.id.bt_register:
                registerUser();
                break;
            case R.id.btn_personalinfo_popup_take:
                CropHelper.clearCachedCropFile(mCropParams.uri);
                Intent intent = CropHelper.buildCaptureIntent(mCropParams.uri);
                startActivityForResult(intent,CropHelper.REQUEST_CAMERA);
                break;
            case R.id.btn_personalinfo_popup_photo:
                CropHelper.clearCachedCropFile(mCropParams.uri);
                Intent intent1 = CropHelper.buildCropFromGalleryIntent(mCropParams);
                startActivityForResult(intent1, CropHelper.REQUEST_CROP);
                break;
            case R.id.btn_personalinfo_popup_cancel:
                pp.dismiss();
                break;
        }
    }



    /**
     * 初始化裁剪图片
     */
    private void initCropHelper(){
        mCropHandler = new CropHandler() {
            @Override
            public void onPhotoCropped(Uri uri) {
                try {
                    InputStream input = getContentResolver().openInputStream(uri);
                    bitmap = BitmapFactory.decodeStream(input);
                    icon.setImageBitmap(bitmap);
                    String imagePath = uri.getPath();
                    file = new File(imagePath);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onCropCancel() {

            }

            @Override
            public void onCropFailed(String message) {

            }

            @Override
            public CropParams getCropParams() {

                return mCropParams;
            }

            @Override
            public Activity getContext() {
                return (Activity) mContext;
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropHelper.REQUEST_CAMERA){
            CropHelper.handleResult(mCropHandler,requestCode,resultCode,data);
        }
        if (requestCode == CropHelper.REQUEST_CROP){
            CropHelper.handleResult(mCropHandler,requestCode,resultCode,data);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mCropHandler.getCropParams() != null)
            CropHelper.clearCachedCropFile(mCropHandler.getCropParams().uri);
    }
}
