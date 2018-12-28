package pri.weiqiang.encryption;

import android.annotation.TargetApi;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;


/**
 * 参考 https://github.com/piterwilson/MP3StreamPlayer/blob/master/src/com/piterwilson/audio/MP3RadioStreamPlayer.java
 */
public class JLayerActivity extends AppCompatActivity {
    // seed即密匙
    private static final String seed = "VoiceEncryptionActivity";
    ByteArrayOutputStream outStream = new ByteArrayOutputStream(1024 * 2);
    private String TAG = JLayerActivity.class.getSimpleName();
    private Decoder mDecoder;
    private AudioTrack mAudioTrack;
    private String rootPath = Environment.getExternalStorageDirectory().getPath();
    //放置MP3的文件夹
    private String playerPath = rootPath + File.separator + "54wall";
    // 放置mp3的位置,根据音频文件的大小，这里需要注意解密的时间也不同，在放置导盲犬xiaoQ的音乐时，要等一会，而一般的音乐可以进行即时播放
    private File oldFile = new File(playerPath, "q.mp3");

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_jlayer);
        /*To get preferred buffer size and sampling rate.http://stackoverflow.com/questions/8043387/android-audiorecord-supported-sampling-rates*/
        AudioManager audioManager = (AudioManager) this.getSystemService(this.AUDIO_SERVICE);
        /*在米1s上有这个错误，就是因为android版本不见容：java.lang.NoSuchMethodError: android.media.AudioManager.getProperty*/
        String rate = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        String size = audioManager.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
        Log.e(TAG, "Buffer Size :" + size + " & Rate: " + rate);

        final int sampleRate = 44100;

        final int minBufferSize = AudioTrack.getMinBufferSize(sampleRate,
                //MI3：CHANNEL_OUT_STEREO //[]AudioFormat.CHANNEL_OUT_STEREO
                //CHANNEL_OUT_MONO影响不大，只要是new AudioTrack构建时选择AudioFormat.CHANNEL_OUT_STEREO即可
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);
        //这里的关键词就是复制，粘贴，调参数刚刚拿了一个旧的mp2，试过，当然是错误的  
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
                sampleRate,
                AudioFormat.CHANNEL_OUT_STEREO, // CHANNEL_OUT_STEREO 声音嘈杂 ，CHANNEL_OUT_DEFAULT，CHANNEL_IN_DEFAULT，也是有噪音              
                AudioFormat.ENCODING_PCM_16BIT,//AudioFormat.CHANNEL_CONFIGURATION_DEFAULT也是有声音
                2 * minBufferSize,
                AudioTrack.MODE_STREAM);

        mDecoder = new Decoder();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    /*与正常播放未加密的mp3不同是的，首先要得到解码的mp3的byte[]数组*/
                    byte[] oldByte_track = new byte[(int) oldFile.length()];
                    FileInputStream fis = new FileInputStream(oldFile);
                    fis.read(oldByte_track);
                    byte[] newByte_track = AESUtils.decryptVoice(seed, oldByte_track);
                    fis.close();
                    InputStream in = new ByteArrayInputStream(newByte_track);
                    Bitstream bitstream = new Bitstream(in);
                    //大约需要14s，但是歌曲可以完整保存下来
//                   final int READ_THRESHOLD = 2147483647;//我试着改动了，没有变化;
                    //需要3s，但是音乐没有播放完就结束了
                    final int READ_THRESHOLD = 1024;//我试着改动了，没有变化;
                    int framesReaded = READ_THRESHOLD;

                    Header header;
                    //先比较（y--）是否大于0，再让y=y-1，如果是--y>0,就是先让y=y-1,再比较现在的y值是否大于0
                    for (; framesReaded-- > 0 && (header = bitstream.readFrame()) != null; ) {
                        SampleBuffer sampleBuffer = (SampleBuffer) mDecoder.decodeFrame(header, bitstream);
                        Log.e("header", String.valueOf(header.framesize));
                        //方法1
//                        short[] buffer = sampleBuffer.getBuffer();                        
//                        mAudioTrack.write(buffer, 0, buffer.length);
                        //方法2
                        short[] buffer = sampleBuffer.getBuffer();
                        for (short s : buffer) {
                            //& 0xff 是为了保证补码一致  http://www.cnblogs.com/think-in-java/p/5527389.html
                            outStream.write(s & 0xff);
                            outStream.write((s >> 8) & 0xff);
                        }
                        bitstream.closeFrame();
                    }
                    byte[] Byte_JLayer = outStream.toByteArray();
                    mAudioTrack.write(Byte_JLayer, 0, Byte_JLayer.length);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
        mAudioTrack.play();
        mAudioTrack.setPlaybackPositionUpdateListener(null);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioTrack.stop();
    }
}