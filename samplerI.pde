//  written by Benjamin Van Citters

//this program allows you to record a sound and then play a continuously 
//looping sample from that sound with a high degree of control of the loop

//controls: press r to start recording audio and press again to stop
// after that, move the mouse to scrub the audio.
// press space to activate random mode, where chunks are played randomly 
import ddf.minim.*;

Minim minim;
AudioInput in;
AudioOutput out;
SoundChunks soundChunks;

boolean randPosMode = false;

void setup()
{
  size(1000, 400, P3D);

  minim = new Minim(this);
  out = minim.getLineOut(Minim.MONO, 1024);
  in = minim.getLineIn(Minim.MONO, 1024);
  
  soundChunks = new SoundChunks();
  in.addListener(soundChunks);
  out.addSignal(soundChunks);
}

void draw()
{
  
  background(0);
  if(randPosMode)
  {
    soundChunks.pct = random(1);
  }
  else
  {
    soundChunks.pct = mouseX*1.f/width;
  }
  soundChunks.playbackEnvelopeSize = mouseY*8.f/height;
  soundChunks.draw();
  drawText();
  
  println("frameRate: " + frameRate);
}

public void drawText()
{
  stroke(255);
  if(!soundChunks.recording)
    text("press 'r' to record.",0,10);
  else
    text("press 'r' to stop recording!",0,10);
  
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

void stop()
{
  minim.stop();
  super.stop();
}
