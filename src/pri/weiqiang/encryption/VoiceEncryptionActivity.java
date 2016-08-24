package pri.weiqiang.encryption;

import java.io.File;  
import java.io.FileInputStream;  
import java.io.FileNotFoundException;  
import java.io.FileOutputStream;  
import java.io.IOException;    
import android.app.Activity;  
import android.content.Intent;
import android.media.AudioTrack;
import android.media.MediaPlayer;  
import android.os.Bundle;  
import android.os.Environment;  
import android.util.Log;  
import android.view.View;  
import android.view.View.OnClickListener;  
import android.widget.Button;  
import android.widget.Toast;  
/**
 * @author  54wall 
 * @date 创建时间：2016-5-23 上午10:40:09
 * @version 1.0
 * http://blog.csdn.net/u012964281/article/details/41787857 
 */
public class VoiceEncryptionActivity extends Activity implements  
        OnClickListener {
	private static final String TAG = "VoiceEncryptionActivity"; 
    //种子,就是密匙  ，种子不同就无法解密，可以深入了解下加密原理
    private static final String seed = "VoiceEncryptionActivity"; 
    private MediaPlayer mPlayer; 
    private Button mPlayButton;  
    private Button mEncryptionButton;  
    private Button mDecryptionButton;  
    private Button mJLayerButton;
    private FileInputStream fis = null;  
    private FileOutputStream fos = null;  
    private String rootPath = Environment.getExternalStorageDirectory().getPath();
	private String playerPath = rootPath + File.separator + "54wall";
	private File oldFile = new File(playerPath, "q.mp3"); 
	File oldFile_each;
	File oldFile_each_after;
	@Override  
    protected void onCreate(Bundle savedInstanceState) {  
        super.onCreate(savedInstanceState);  
        setContentView(R.layout.activity_voice_encryption);  
        mPlayButton = (Button) findViewById(R.id.playButton_encryp);  
        mPlayButton.setOnClickListener(this);  
        mEncryptionButton = (Button) findViewById(R.id.encryptionButton);  
        mEncryptionButton.setOnClickListener(this);  
        mDecryptionButton = (Button) findViewById(R.id.decryptionButton);  
        mDecryptionButton.setOnClickListener(this); 
        //开始忘记注册toJLayerButton了，结果出现了Timeline: Activity_launch_request time:163908273
        mJLayerButton=(Button)findViewById(R.id.toJLayerButton);
        mJLayerButton.setOnClickListener(this); 
    }  
  
    @Override  
    public void onClick(View v) {  
        switch (v.getId()) {  
        case R.id.playButton_encryp:  
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
  
        case R.id.encryptionButton:  
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
  
        case R.id.decryptionButton:  
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
            
        case R.id.toJLayerButton: 
        	Intent it = new Intent(VoiceEncryptionActivity.this,
					JLayerActivity.class);
        	VoiceEncryptionActivity.this.startActivity(it);
        	break;

        	
        default:  
            break;  
        }  
  
    }  
}  
