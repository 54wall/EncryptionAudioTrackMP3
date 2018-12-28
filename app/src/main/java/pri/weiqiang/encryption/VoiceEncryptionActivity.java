package pri.weiqiang.encryption;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author 54wall
 * @version 1.0
 * 参考 http://blog.csdn.net/u012964281/article/details/41787857
 * @date 创建时间：2016-5-23 上午10:40:09
 */
public class VoiceEncryptionActivity extends AppCompatActivity implements
        OnClickListener {
    private static final String TAG = VoiceEncryptionActivity.class.getSimpleName();
    //种子即密匙
    private static final String seed = "VoiceEncryptionActivity";
    private MediaPlayer mPlayer;
    private Button mBtnPlay;
    private Button mBtnEncrypt;
    private Button mBtnDecrypt;
    private Button mBtnJLayer;
    private FileInputStream fis = null;
    private FileOutputStream fos = null;
    private String rootPath = Environment.getExternalStorageDirectory().getPath();
    private String playerPath = rootPath + File.separator + "54wall";
    private File oldFile = new File(playerPath, "q.mp3");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_encryption);
        mBtnPlay = findViewById(R.id.btn_play);
        mBtnPlay.setOnClickListener(this);
        mBtnEncrypt = findViewById(R.id.btn_encrypt);
        mBtnEncrypt.setOnClickListener(this);
        mBtnDecrypt = findViewById(R.id.btn_decrypt);
        mBtnDecrypt.setOnClickListener(this);
        mBtnJLayer = findViewById(R.id.btn_jlayer);
        mBtnJLayer.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                if (mPlayer != null) {
                    mPlayer.release();
                    mPlayer = null;
                }
                // mPlayer = MediaPlayer.create(this, R.raw.recording_old);
                boolean isSuccess = true;

                try {
                    fis = new FileInputStream(oldFile);
                    mPlayer = new MediaPlayer();
                    mPlayer.setDataSource(fis.getFD());
                    mPlayer.prepare();
                    mPlayer.start();
                } catch (FileNotFoundException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IllegalArgumentException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IllegalStateException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } finally {
                    try {
                        fis.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (!isSuccess)
                    Toast.makeText(this, "播放失败", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_encrypt:
                // 加密保存
                isSuccess = true;
                try {
                    fis = new FileInputStream(oldFile);
                    byte[] oldByte = new byte[(int) oldFile.length()];
                    fis.read(oldByte); // 读取
                    byte[] newByte = AESUtils.encryptVoice(seed, oldByte);
                    // 加密
                    fos = new FileOutputStream(oldFile);
                    fos.write(newByte);

                } catch (FileNotFoundException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (Exception e) {
                    isSuccess = false;
                    e.printStackTrace();
                } finally {
                    try {
                        fis.close();
                        fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (isSuccess)
                    Toast.makeText(this, "加密成功", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "加密失败", Toast.LENGTH_SHORT).show();
                Log.i(TAG, "保存成功");
                break;

            case R.id.btn_decrypt:
                // 解密保存
                isSuccess = true;
                byte[] oldByte = new byte[(int) oldFile.length()];
                try {
                    fis = new FileInputStream(oldFile);
                    fis.read(oldByte);
                    byte[] newByte = AESUtils.decryptVoice(seed, oldByte);
                    // 解密
                    fos = new FileOutputStream(oldFile);
                    fos.write(newByte);

                } catch (FileNotFoundException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (IOException e) {
                    isSuccess = false;
                    e.printStackTrace();
                } catch (Exception e) {
                    isSuccess = false;
                    e.printStackTrace();
                }
                try {
                    fis.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (isSuccess)
                    Toast.makeText(this, "解密成功", Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(this, "解密失败", Toast.LENGTH_SHORT).show();
                break;

            case R.id.btn_jlayer:
                Intent it = new Intent(VoiceEncryptionActivity.this,
                        JLayerActivity.class);
                VoiceEncryptionActivity.this.startActivity(it);
                break;


            default:
                break;
        }

    }
}  
