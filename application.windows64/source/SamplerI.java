import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class SamplerI extends PApplet {

//  written by Benjamin Van Citters

//this program allows you to record a sound and then play a continuously 
//looping sample from that sound with a high degree of control of the loop

//controls: press r to start recording audio and press again to stop
// after that, move the mouse to scrub the audio.
// press space to activate random mode, where chunks are played randomly 


Minim minim;
AudioInput in;
AudioOutput out;
SoundChunks soundChunks;

boolean randPosMode = false;

public void setup()
{
  size(1000, 400, P3D);

  minim = new Minim(this);
  out = minim.getLineOut(Minim.MONO, 1024);
  in = minim.getLineIn(Minim.MONO, 1024);
  
  soundChunks = new SoundChunks();
  in.addListener(soundChunks);
  out.addSignal(soundChunks);
}

public void draw()
{
  if(!soundChunks.recording)
  background(0,0,0,10);
  else
  background(255,0,0,10); 
//  rect(0,0,width,height);
  if(randPosMode)
  {
    soundChunks.pct = random(1);
  }
  else
  {
    soundChunks.pct = mouseX*1.f/width;
  }
  
  float maxEnvelopeScale = 16.f;
  soundChunks.playbackEnvelopeSize = mouseY*maxEnvelopeScale/height;
  soundChunks.draw();
  drawText();
  
  println("frameRate: " + frameRate);
}

public void drawText()
{
  stroke(255);
  fill(255);
  if(!soundChunks.recording)
    text("press 'r' to record audio.",0,10);
  else
    text("MAKE SOME NOISE!!!! press 'r' to stop recording!",0,10);
  
  String randString = " and hit space to enable random mode";
  if(randPosMode)
  {
    randString = " and hit space to disable random mode";
  }
    text("press 'c' to clear the recording"+randString,0,20);

}

public void keyReleased()
{
  if ( key == 'r' ) 
  {
    soundChunks.recording = !soundChunks.recording; 
  }
  
  if ( key == 'c' ) 
  {
    soundChunks.clearSamples(); 
  }
  
  if(key == ' ')
  {
    randPosMode = !randPosMode;
  }
}

public void stop()
{
  minim.stop();
  super.stop();
}
//  written by Benjamin Van Citters


class SoundChunks implements AudioListener, AudioSignal
{
  private ArrayList<float[]> samps;
  private float[] lastSignal;
  private float[] lastSample;
  //size relative to the signal sample size
  float playbackEnvelopeSize = 1.f;
  int curEnvelopeSampleCount = 2048;
  int playbackEnvelopeIndex;
  
  boolean recording = false;
  float pct = 0.f;
  long totalSamples = 0;
  private long endSampIndex;// = (long)min(curIndex + signal.length/2, totalSamples);
  private long startSampIndex;// = (long)max(curIndex - signal.length/2, 0);
    
  public SoundChunks()
  {
    samps = new ArrayList<float[]>();
  }
  
  public void samples(float[] sampL, float[] sampR) 
  {}

  public void samples(float[] samp) 
  {    
    if(recording)
    {
      samps.add(samp);
      totalSamples += samp.length;
      lastSample = java.util.Arrays.copyOf(samp,samp.length);
    }
  }
  
  //method for AudioSignal interface
  public void generate(float[] left, float[] right) 
  {
    generate(left);
    generate(right);
    
  }

  //method for AudioSignal interface
  public void generate(float[] signal) 
  {
    if(recording)
      return;
    //force that playbackEnvelopeSize stays larger than or equal to zero
    playbackEnvelopeSize = max(playbackEnvelopeSize,0);
    curEnvelopeSampleCount = (int)(playbackEnvelopeSize*signal.length);
    
    if(curEnvelopeSampleCount<1)
      return;
    long curIndex = (long)(pct*totalSamples);
    endSampIndex = (long)min(curIndex + curEnvelopeSampleCount/2, totalSamples);
    startSampIndex = (long)max(curIndex - curEnvelopeSampleCount/2, 0);
    float [] envelope = new float[curEnvelopeSampleCount];
    long currentIndex = 0;
    int fIndex = 0;
    
    //fill envelope
    for(float[] sampArray : samps)
    {
      for(int i = 0; i < sampArray.length; i++)
      {
        if(currentIndex >= startSampIndex && currentIndex < endSampIndex)
        {
          envelope[fIndex] = sampArray[i];
          fIndex++;
        }
        currentIndex++;
      }
    }
   
   playbackEnvelopeIndex = playbackEnvelopeIndex % curEnvelopeSampleCount;
    for(int i = 0; i < signal.length; i++)
    {
      signal[i] = envelope[playbackEnvelopeIndex];
      playbackEnvelopeIndex = (playbackEnvelopeIndex+1)%curEnvelopeSampleCount;
    }
    lastSignal = java.util.Arrays.copyOf(signal,signal.length);
//    println("sig len: " + signal.length + " last len = " + lastSignal.length + " diff: " + (endSampIndex-startSampIndex));
  }
  
  public void draw()
  {
    if(totalSamples < 1)
      return;
    
    if(recording)
    {
      drawLastSample();
      return;
    }
      
    drawCurrentSignal();
    float spacing = 1;//width*1.f/totalSamples;
    int stride = (int)(totalSamples/width);
    stride = (int)max(stride,1);
    
    stroke(255);
    float rectW = (endSampIndex-startSampIndex)*width*1.f/totalSamples;
    fill(255,0,0);
    rect(startSampIndex*width*1.f /totalSamples, 0, rectW,height);
    noFill();
    long curIndex = 0;
    
    pushMatrix();
    translate(0,height/2.f);
    beginShape();
    for(float[] sampArray : samps)
    {
      for(int i = 0; i < sampArray.length; i+=stride)
      {
        vertex(curIndex*spacing,sampArray[i]*height);
        curIndex++;
      }
    }
    endShape();
    popMatrix(); 
  }
  
  public void drawLastSample()
  {
    noFill();
    float spacing = width*1.f/(lastSample.length);
    pushMatrix();
    stroke(255,255,0);
    translate(0,height/2.f);
    beginShape();

      for(int i = 0; i < lastSample.length; i++)
      {
        vertex(i*spacing,lastSample[i]*height);
      }
    
    endShape();
    popMatrix(); 
  }
  
  public void drawCurrentSignal()
  {
    noFill();
    int repititions = 3;
    float spacing = width*1.f/(lastSignal.length*repititions);
    pushMatrix();
    stroke(0,255,0);
    translate(0,height/4.f);
    beginShape();
    for(int j = 0; j < repititions; j++)
    {
      for(int i = 0; i < lastSignal.length; i++)
      {
        vertex((j*lastSignal.length+i)*spacing,lastSignal[i]*height);
      }
    }
    endShape();
    popMatrix(); 
   
  }
  
  public void clearSamples()
  {
    totalSamples = 0;
    samps.clear();
  }
}
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "SamplerI" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
