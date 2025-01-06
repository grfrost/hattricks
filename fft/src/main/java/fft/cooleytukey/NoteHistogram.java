package fft.cooleytukey;

import java.util.Arrays;

public class NoteHistogram{
   class NoteData{
      NoteValue noteValue;
      int frame;
      float value;
      public float ratio;
   }

  public static class NoteValue{
     final int bin;
     final String noteSharp;
     final  String noteFlat;
     final  boolean sharp = false;

     float value;
    public float ratio;

      public NoteValue(int _bin, String _noteSharp, String _noteFlat) {
         this.bin = _bin;
         this.noteFlat = _noteFlat;
         this.noteSharp = _noteSharp;
      }



      @Override public String toString() {
         return (sharp ? noteSharp : noteFlat);
      }
   }
   final NoteValue[] noteHistogram = new NoteValue[]{
           new NoteValue(0,"A","A"),
           new NoteValue(1,"A#","Bb"),
           new NoteValue(2,"B","B"),
           new NoteValue(3,"C","C"),
           new NoteValue(4,"C#","Db"),
           new NoteValue(5,"D","D"),
           new NoteValue(6,"D#","Eb"),
           new NoteValue(7,"E","E"),
           new NoteValue(8,"F","F"),
           new NoteValue(9,"F#","Gb"),
           new NoteValue(10,"G","G"),
           new NoteValue(11,"G#","Ab")
   };

   final float sampleRate;

   final float fftWidth;

   final int channels;

   NoteHistogram(float _sampleRate, float _fftWidth, int _channels) {
      fftWidth = _fftWidth;
      sampleRate = _sampleRate;
      channels = _channels;
   }

   final double interval = Math.pow(Math.E, Math.log(2) / 12);
   final double logInterval = Math.log(interval);
   final double middleA = 440.0;

   public int frequencyToNoteIndex(double f) { // frequency
      double noteval = Math.log(f / middleA) / logInterval; // log(f/440)/log(12th root of two)  or in our case 440/5 which is still 'A'
      int noteindex = (int) Math.round(noteval);
      return ((noteindex + (12 * 10)) % 12);
   }

   public void add(float[] re) {
      for (int x = 0; x < fftWidth / 2; x += channels) {
         double freq = (x / channels * sampleRate) / fftWidth;
         int noteIndex = frequencyToNoteIndex(freq);
         if (noteIndex >= 0 && noteIndex < 12) {
            for (int channel = 0; channel < channels; channel++) {
               noteHistogram[noteIndex].value += Math.abs(re[x + channel]);
            }
         }
      }
   }

   public void clean() {
      for (int bin = 0; bin < 12; bin++) {
         noteHistogram[bin].value = 0;
         noteHistogram[bin].ratio = 0f;
      }
   }

   public NoteValue[] get() {
      NoteValue[] noteHistogramCopy = Arrays.copyOf(noteHistogram, noteHistogram.length);
      var top =Arrays.stream(noteHistogram).max((lh,rh)-> Float.compare(lh.value, rh.value)).get();
      for (int bin = 0; bin < 12; bin++) {
         noteHistogramCopy[bin].ratio = noteHistogramCopy[bin].value / top.value;
      }
      return noteHistogramCopy;
   }

   @Override public String toString() {
      StringBuilder stringBuilder = new StringBuilder();
      NoteValue[] noteHistogramCopy = get();

      for (int bin = 0; bin < 12; bin++) {
         stringBuilder.append(String.format("%2s %5.2f ", noteHistogramCopy[bin], noteHistogramCopy[bin].ratio));
      }
      return (stringBuilder.toString());
   }
}
