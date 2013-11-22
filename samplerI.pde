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
  out = minim.getLineOut(Minim.MONO);
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
  println("frameRate: " + frameRate);
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
