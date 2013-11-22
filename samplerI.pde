import ddf.minim.analysis.*;
import ddf.minim.*;

Minim minim;
AudioInput in;
AudioOutput out;
SoundChunks soundChunks;
void setup()
{
  size(512, 200, P3D);

  minim = new Minim(this);
  out = minim.getLineOut(Minim.MONO, 512);
  in = minim.getLineIn(Minim.MONO, 512);
  
  soundChunks = new SoundChunks();
  in.addListener(soundChunks);
  out.addSignal(soundChunks);
}

void draw()
{
  
  background(0);
  soundChunks.pct = mouseX*1.f/width;//random(1);//
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
  
  text("press 'c' to clear the recording",0,20);
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
}

void stop()
{
  minim.stop();
  super.stop();
}
