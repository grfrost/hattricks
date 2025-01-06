package fft.cooleytukey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NoteHistogram{
  record  NoteInfo(int bin,String noteSharp,String noteFlat) {
      final static  NoteInfo[] scale = new NoteInfo[]{
              new NoteInfo(0,"A","A"),
              new NoteInfo(1,"A#","Bb"),
              new NoteInfo(2,"B","B"),
              new NoteInfo(3,"C","C"),
              new NoteInfo(4,"C#","Db"),
              new NoteInfo(5,"D","D"),
              new NoteInfo(6,"D#","Eb"),
              new NoteInfo(7,"E","E"),
              new NoteInfo(8,"F","F"),
              new NoteInfo(9,"F#","Gb"),
              new NoteInfo(10,"G","G"),
              new NoteInfo(11,"G#","Ab")
      };



      @Override public String toString() {
         return (true ? noteSharp : noteFlat);
      }
   }


    List<Frame> frames = new ArrayList<>();
    public static class NoteData{
        NoteInfo noteInfo;
        int frame;
        float value;
        public float ratio;
        NoteData(NoteInfo noteInfo) {
            this.noteInfo = noteInfo;
        }
    }

    public final static class Frame {
        final long time;
        final int frameC;
        final NoteData[] noteData = new NoteData[NoteInfo.scale.length];

        Frame(long time, int frameC, float[] re, float fftWidth, int channels, float sampleRate) {
            this.time = time;
            this.frameC = frameC;
            for (int i = 0; i < NoteInfo.scale.length; i++) {
                noteData[i] = new NoteData(NoteInfo.scale[i]);
            }
            for (int x = 0; x < fftWidth / 2; x += channels) {
                double freq = (x / channels * sampleRate) / fftWidth;
                int noteIndex = NoteHistogram.frequencyToNoteIndex(freq);
                if (noteIndex >= 0 && noteIndex < 12) {
                    for (int channel = 0; channel < channels; channel++) {
                        noteData[noteIndex].value += Math.abs(re[x + channel]);
                    }
                }
            }
            var top =Arrays.stream(noteData).max((lh,rh)-> Float.compare(lh.value, rh.value)).get();
            for (int bin = 0; bin < 12; bin++) {
                noteData[bin].ratio = noteData[bin].value / top.value;
            }

        }
        @Override public String toString() {
            FrameRenderer fr = new FrameRenderer();
            //StringBuilder stringBuilder = new StringBuilder();
            for (int bin = 0; bin < 12; bin++) {
                var n = noteData[bin].noteInfo;
                var c = Math.max(Math.min(noteData[bin].ratio-.1f, .9f),0.f);
                fr.bgnf(c,c,c,_->{
                    fr.strf("%2s ",n.noteFlat);
                });
            }
            return (fr.stringBuilder.toString());
        }
    }

   final float sampleRate;

   final int fftWidth;

   final int channels;

   NoteHistogram(float _sampleRate, int _fftWidth, int _channels) {
      fftWidth = _fftWidth;
      sampleRate = _sampleRate;
      channels = _channels;
   }

   final static double interval = Math.pow(Math.E, Math.log(2) / 12);
   final static double logInterval = Math.log(interval);
   final static double middleA = 440.0;

   public static int frequencyToNoteIndex(double f) { // frequency
      double noteval = Math.log(f / middleA) / logInterval; // log(f/440)/log(12th root of two)  or in our case 440/5 which is still 'A'
      int noteindex = (int) Math.round(noteval);
      return ((noteindex + (12 * 10)) % 12);
   }

   public Frame add(long time, int frameC, float[] re) {
       Frame frame = new Frame(time, frameC, re, fftWidth, channels, sampleRate);
       frames.add(frame);
/*
      for (int x = 0; x < fftWidth / 2; x += channels) {
         double freq = (x / channels * sampleRate) / fftWidth;
         int noteIndex = frequencyToNoteIndex(freq);
         if (noteIndex >= 0 && noteIndex < 12) {
            for (int channel = 0; channel < channels; channel++) {
               noteHistogram[noteIndex].value += Math.abs(re[x + channel]);
            }
         }
      } */
      return frame;
   }

}
