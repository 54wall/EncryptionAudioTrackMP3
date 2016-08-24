#使用TrackAudio播放AES加密的mp3
####问题背景
最近项目可能需要对声音进行加密，解密，进行识别认证，在网上有一篇文章介绍了如何使用AES算法在java中对音频进行加密<a href="http://blog.csdn.net/u012964281/article/details/41787857">Android 加密/解密音频文件(AES)</a>。
其具体的做法就是将加密文件和解密文件全部保存到存储器上，在未来项目中需要频繁的读写硬盘可不是一个好的建议，所以寻找能够保存的是加密的音频，然后直接对加密音频进行解密，之后直接在内存中进行播放。
于是又找到了一篇关于实时播放wav，但是我们需要的mp3，最好还是解决实时播放wav文章的最初原作者同样给出了实时解码mp3的解决方法-<a href="http://mindtherobot.com/blog/624/android-audio-play-an-mp3-file-on-an-audiotrack">Android Audio: Play an MP3 file on an AudioTrack</a>
下面的代码就是混合了他们所有人的产物代码以上传至github，部分内容也是直接翻译过来
####这里的逻辑
为什么加密和TrackAudio能够混在一起用，讲下这里的逻辑：因为播放mp3使用TrackAudio，它使用的是音频流，即输入的是byte[],而AES解密后输出也是byte[],所以AES解密后直接输出byte的，再传递给TrackAudio进行音频播放也就顺理成章了。
而具体的流程就是：使用AES机密mp3文件，并将解密后的文件newByte_track（byte[]类型），直接交给ByteArrayInputStream变成InputStream in；
```
InputStream in = new ByteArrayInputStream(newByte_track); 
Bitstream bitstream = new Bitstream(in);
```

变为Bitstream bitstream ，而Jlayer正好可以将bitstream 作为输入，然后输出SampleBuffer 
```
SampleBuffer sampleBuffer = (SampleBuffer) mDecoder.decodeFrame(header, bitstream);
```
最后把SampleBuffer 转换为byte[],我们的TrackAudio是可以直接播放byte[]。

####首先不谈加密，直接播放mp3
以下来自翻译
在Android中能够播放mp3的接口只有MediaPlayer，沉重，慢，并且仅提供高级接口，如果你需要修改或者混合音频流的话，你就得自己动手，AudioTrack就能帮到你，我建议先阅读(<a href="http://mindtherobot.com/blog/580/android-audio-play-a-wav-file-on-an-audiotrack/">the article about playing a WAV</a>) 这篇文章，它包含了一些关于PCM基本常识。
因为WAV文件基本可以算无损，而MP3就不同了，它已经经过复杂的算法解码过，所以我们需要借助第三方代码，允许我们将MP3数据转换为raw PCM数据，之后就可以按部就班的让AudioTrack播放了。
经过漫长的搜索，终于找到了一款mp3解码器<a href="http://www.javazoom.net/javalayer/javalayer.html">Jlayer</a>。它可以轻松的解码MP3（当然从Jlayer的界面也能看出，它是为Java SE 平台专设的，时间也是许久之前，不过Android也基于java，所以索性就试试），Jlayer的授权协议类LGPL 协议，对商用app也非常友好。
在使用Jlayer之前，首先把它导入进来到你的项目中来。
下边是调用Jlayer代码

```
InputStream in = new ByteArrayInputStream(newByte_track); 
        Bitstream bitstream = new Bitstream(in);
                    final int READ_THRESHOLD = 2147483647;
                    int framesReaded = READ_THRESHOLD;
                    Header header;
                    for(; framesReaded-- > 0 && (header = bitstream.readFrame()) != null;) {
                        SampleBuffer sampleBuffer = (SampleBuffer) mDecoder.decodeFrame(header, bitstream);
                        Log.e("header",String.valueOf(header.framesize ));
                        short[] buffer = sampleBuffer.getBuffer();
                        for (short s : buffer) {
                          outStream.write(s & 0xff);
                          outStream.write((s >> 8 ) & 0xff);
                          }                                                                       
                        bitstream.closeFrame();
                    }
                    byte[] Byte_JLayer=outStream.toByteArray();
```
这里参数修改如下时，播放的音乐的完整性和播放开始的速度都是不一样的。
```
    				//大约需要14s，但是歌曲可以完整保存下来
//                   final int READ_THRESHOLD = 2147483647;//我试着改动了，没有变化;
                    //需要3s，但是音乐没有播放完就结束了
    				final int READ_THRESHOLD = 1024;//我试着改动了，没有变化;
                    int framesReaded = READ_THRESHOLD;

```
其中


```
mDecoder = new Decoder();
```
这里Decoder类的完整类名javazoom.jl.decoder.Decoder.Decoder()，当然代码时死的，不需要记忆，但是你要知道它的原理，然后实现它。
当然也可以走原生路线，找一个用C写的MP3解码器（这里也有一个连接，但是我没有实现，需要JNI和NDK的知识），在此不提，
如果你需要在你的app中解码mp3，这篇文章可以帮助到你。
在使用Jlayer成功获得到数据  byte[] Byte_JLayer，剩下的工作就交给AudioTrack了，当然在将byte[] Byte_JLayer存入AudioTrack，首先是配置AudioTrack：

``` 
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
                2*minBufferSize,
                AudioTrack.MODE_STREAM);
```


####AES加密解密具体流程
高级加密标准（英语：Advanced Encryption Standard，缩写：AES），在密码学中又称Rijndael加密法，是美国联邦政府采用的一种区块加密标准。这个标准用来替代原先的DES，已经被多方分析且广为全世界所使用。（来自百度百科）

####加密的过程 
我直接使用<a href="http://blog.csdn.net/u012964281/article/details/41787857">Android 加密/解密音频文件(AES)</a>中的加密方法。加密的过程简单来说就是读取文件转换为byte[],解密，他的思路就是保存为音乐文件，然后播放，而我的思路是直接交给AudioTrack和Jlayer去运行byte，这样每次就不用保存到存储空间上，而对外，mp3一直处于加密状态，在PC端也同样无法播放。
####备注
我在加密过程中注意，使用mp3（通常下载的歌曲没有问题），但是注意如果使用Adobe Audition时就要注意如果输出的格式进行了设置，有时Jlayer和AudioTrack也是无法满足的，有时会有噪音的输出，有时就是没有声音，例如我在使用Adobe Audition导出mp2后，修改后缀为mp3，依然还是无法播放，因为本教程的解码就是面向mp3而存在。

下边的这些参数实际也是跟随android设备进行设置的，可以通过调整细微的参数，看AudioTrack播放音乐时会有不同的表现。
####效果图
将代码保重的54wall拷贝到手机的内部存储器或者SD卡上，54wall文件夹中有一个q.mp3,是导盲犬小Q的一个音乐，然后运行项目后就是下边的效果，此时的q.mp3是没有加密过的，直接点击play，就可以播放，点击encryp是进行加密保存到存储器上，再进行play是不能播放的，这时点击toJLayer则Activity进行跳转，使用Jlayer进行播放，注意，这时需要消耗一定的时间，进行解码，开始是没有声音的。
![encryption.jpg](http://upload-images.jianshu.io/upload_images/2467798-9cbf058f6082e06e.jpg?imageMogr2/auto-orient/strip%7CimageView2/2/w/1240)