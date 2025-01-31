package fft.cooleytukey;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class Main {

   static class MPEGAudioData{
      public final float[] data;

      public final AudioFormat audioFormat;

      public final File file;

      MPEGAudioData(File _file) throws UnsupportedAudioFileException, IOException {

         final int bytesPerChannel = 2;
         file = _file;
         List<float[]> blocks = new ArrayList<float[]>();

         AudioInputStream in =AudioSystem.getAudioInputStream(file);
         audioFormat = in.getFormat();
         System.out.println("got sample in bits" + audioFormat.getSampleSizeInBits());
         AudioInputStream din = AudioSystem.getAudioInputStream(audioFormat, in);

         int nBytesRead;

         System.out.println("sample in bits" + audioFormat.getSampleSizeInBits());
         int frames = 0;
         final byte[] byteBuffer = new byte[8192];
         while ((nBytesRead = din.read(byteBuffer, 0, byteBuffer.length)) != -1) {
            int blockSize = nBytesRead / bytesPerChannel; // enough floats for all channels

            float[] block = new float[blockSize];

            for (int frame = 0; frame < blockSize; frame++) {
               short value = (short) ((0xff & byteBuffer[frame * bytesPerChannel + 1]) << 8);
               value |= (short) ((0xff & byteBuffer[frame * bytesPerChannel]));
               block[frame] = (float) value;
            }
            System.out.print(".");
            blocks.add(block);
            frames += blockSize;
         }
         System.out.println();
         din.close();

         data = new float[(int) frames];

         int pos = 0;
         for (float[] block : blocks) {
            System.arraycopy(block, 0, data, pos, block.length);
            pos += block.length;
         }

         blocks.clear();
         blocks = null;
      }

   }

   public static class CooleyTukey2Channel{
      int n;

      int m;

      final int[] mapFrom;

      final int[] mapTo;

      int mapLen;

      final float[] imag;

      final float[] real;

      final float c[];

      final float s[];

      final float[] ca;

      final float[] sa;

      final int[] k;

      final int[] k_n1;

      final int len;

      final int channels;

      public class Line{
         public Line(int _a, int _k, int _k_n1, int _n) {
            a = _a;
            k = _k;
            k_n1 = _k_n1;
            ca = c[a];
            sa = s[a];
            n = _n;
         }

         int k, k_n1, n, a;

         float ca, sa;

         String toBin(int value, int width) {
            StringBuilder b = new StringBuilder();

            for (int bit = 1 << (width - 1); bit != 0; bit >>= 1) {
               b.append(((bit & value) == bit) ? '1' : '0');
            }
            return (b.toString());
         }

         @Override public String toString() {
            return (String.format("%02x: (%02x,%02x)  %s %s %s", n, k, k_n1, toBin(n, 8), toBin(k, 8), toBin(k_n1, 8)));
         }
      }

      private List<Line> list;

      public CooleyTukey2Channel(float[] _real, float[] _imag, int _channels) {
         channels = _channels;
         real = _real;
         imag = _imag;
         n = real.length / channels;
         mapFrom = new int[n];
         mapTo = new int[n];

         m = (int) (Math.log(n) / Math.log(2)); // 1<<m == n
         for (int bits = 1; bits < n; bits++) {
            int reverseBits = Integer.reverse(bits) >>> (32 - m); // xxxxx(1010) -> xxxxx(0101) where m == 4 
            if (bits < reverseBits) {
               mapFrom[mapLen] = bits;
               mapTo[mapLen] = reverseBits;
               mapLen++;
            }
         }
         c = new float[n];
         s = new float[n];
         for (int i = 0; i < n; i++) {
            double value = Math.PI * 2 * i;
            c[i] = (float) Math.cos(value / n);
            s[i] = (float) Math.sin(-value / n);
         }
         int count = 0;
         list = new ArrayList<Line>(); // FFT
         for (int i = 0; i < m; i++) {
            final int n1 = 1 << i;
            int a = 0;
            for (int j = 0; j < n1; j++) {
               for (int k = j; k < n; k = k + (1 << (i + 1))) {
                  list.add(new Line(a, k, k + n1, count++));
               }
               a += 1 << (m - i - 1);
            }
         }
         len = list.size();
         ca = new float[len];
         sa = new float[len];
         k = new int[len];
         k_n1 = new int[len];

         count = 0;
         for (Line triple : list) {
            ca[count] = triple.ca;
            sa[count] = triple.sa;
            k[count] = triple.k;
            k_n1[count] = triple.k_n1;
            count++;
         }

      }

      public void fwd(int _channel) {
         // Swap entries based on bit reverse mappings
         for (int i = 0; i < mapLen; i++) {
            float temp = imag[mapFrom[i] * channels + _channel];
            imag[mapFrom[i] * channels + _channel] = imag[mapTo[i] * channels + _channel];
            imag[mapTo[i] * channels + _channel] = temp;
            temp = real[mapFrom[i] * channels + _channel];
            real[mapFrom[i] * channels + _channel] = real[mapTo[i] * channels + _channel];
            real[mapTo[i] * channels + _channel] = temp;
         }

         for (int i = 0; i < len; i++) {
            int k_n1_i = k_n1[i];
            int k_i = k[i];
            float sa_i = sa[i];
            float ca_i = ca[i];
            float imag_k_n1_i = imag[k_n1_i * channels + _channel];
            float real_k_n1_i = real[k_n1_i * channels + _channel];
            float imag_k_i = imag[k_i * channels + _channel];
            float real_k_i = real[k_i * channels + _channel];
            float t1 = ca_i * imag_k_n1_i - sa_i * real_k_n1_i;
            float t2 = sa_i * imag_k_n1_i + ca_i * real_k_n1_i;

            imag[k_n1_i * channels + _channel] = imag_k_i - t1;
            real[k_n1_i * channels + _channel] = real_k_i - t2;
            imag[k_i * channels + _channel] = imag_k_i + t1;
            real[k_i * channels + _channel] = real_k_i + t2;
         }

      }

      public void fwd() {
         for (int channel = 0; channel < channels; channel++) {
            fwd(channel);
         }
      }

      public void inv(int _channel) {

         for (int k = 0; k < n; k++) {
            imag[k * channels + _channel] = -imag[k * channels + _channel];
         }
         fwd(_channel);
         for (int k = 0; k < n; k++) {
            real[k * channels + _channel] = real[k * channels + _channel] / n;
            imag[k * channels + _channel] = -imag[k * channels + _channel] / n;
         }

      }

      public void inv() {

         for (int channel = 0; channel < channels; channel++) {
            inv(channel);
         }

      }
   }

   static public void main(String[] args) throws IOException, UnsupportedAudioFileException, LineUnavailableException,
         InterruptedException {

      JFileChooser fileChooser = new JFileChooser();
      File file = null;
      fileChooser.setCurrentDirectory(
              new File("/Users/grfrost/github/hattricks/fft/src/main/resources")
              //new File("G:\\Data\\music\\iTunes Music\\Alphabetically")
      );

      switch (fileChooser.showOpenDialog(null)) {
         case JFileChooser.APPROVE_OPTION:
            file = fileChooser.getSelectedFile();
            break;
         case JFileChooser.CANCEL_OPTION:
            System.exit(1);

      }
      MPEGAudioData audioData = new MPEGAudioData(file);

      String[] opts = {"512","1024", "2048", "4096", "8192", "16384", "32768", "65536"};
      int radix2 = Integer.parseInt((String)JOptionPane.showInputDialog(
              null, "Choose Radix2 Buf size", "Radix2 Buf", JOptionPane.PLAIN_MESSAGE, null, opts, opts[0]));


      DataLine.Info outInfo = new DataLine.Info(SourceDataLine.class, audioData.audioFormat);
      SourceDataLine outLine = (SourceDataLine) AudioSystem.getLine(outInfo);
      outLine.open(audioData.audioFormat);
      outLine.start();

      int sampleSizeInBytes = audioData.audioFormat.getSampleSizeInBits() / 8;
      final byte[] byteBuffer = new byte[radix2 * sampleSizeInBytes];
      float[] real = new float[radix2];
      float[] imaginary = new float[radix2];
      CooleyTukey2Channel fft = new CooleyTukey2Channel(real, imaginary, 2);

      NoteHistogram histo = new NoteHistogram(audioData.audioFormat.getFrameRate(), radix2 / 2, 2);

      int totalFrameCount = audioData.data.length / byteBuffer.length;
      System.out.println("frames " + totalFrameCount);
      for (int frameIndex = 0; frameIndex < totalFrameCount; frameIndex++) {
         // Copy audio pcm info into real
         System.arraycopy(audioData.data, frameIndex * (byteBuffer.length / sampleSizeInBytes), real, 0, real.length);
         // clean imaginary
         Arrays.fill(imaginary, 0f);
         long start = System.currentTimeMillis();
         fft.fwd();
         NoteHistogram.Frame frame = histo.add(start, frameIndex,real);
         System.out.println(frameIndex + " " + (System.currentTimeMillis() - start) + "ms " + frame);

         for (int i = 0; i < byteBuffer.length / sampleSizeInBytes; i++) {
            short value = (short) audioData.data[frameIndex * (byteBuffer.length / sampleSizeInBytes) + i];
            byteBuffer[i * sampleSizeInBytes + 1] = (byte) ((value >> 8) % 0xff);
            byteBuffer[i * sampleSizeInBytes + 0] = (byte) (value % 0xff);
         }
         outLine.write(byteBuffer, 0, byteBuffer.length);
      }
      outLine.drain();
      outLine.stop();
      outLine.close();
   }

}
