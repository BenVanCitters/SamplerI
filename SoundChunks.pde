class SoundChunks implements AudioListener, AudioSignal
{
  private ArrayList<float[]> samps;
  private float[] lastSignal;
  boolean recording = false;
  float pct = 0.f;
  long totalSamples = 0;;
  private long endSampIndex;// = (long)min(curIndex + signal.length/2, totalSamples);
  private long startSampIndex;// = (long)max(curIndex - signal.length/2, 0);
    
  public SoundChunks()
  {
    samps = new ArrayList<float[]>();;
  }
  
  void samples(float[] sampL, float[] sampR) 
  {}

  void samples(float[] samp) 
  {    
    if(recording)
    {
      samps.add(samp);
      totalSamples += samp.length;
    }
  }
  
  //method for AudioSignal interface
  void generate(float[] left, float[] right) 
  {
    generate(left);
    generate(right);
    
  }

  //method for AudioSignal interface
  void generate(float[] signal) 
  {
    if(recording)
      return;
    
    
    long curIndex = (long)(pct*totalSamples);
    endSampIndex = (long)min(curIndex + signal.length/2, totalSamples);
    startSampIndex = (long)max(curIndex - signal.length/2, 0);
    long currentIndex = 0;
    int signalIndex = 0;
    
    for(float[] sampArray : samps)
    {
      for(int i = 0; i < sampArray.length; i++)
      {
        if(currentIndex >= startSampIndex && currentIndex < endSampIndex)
        {
          signal[signalIndex] = sampArray[i];
          signalIndex++;
        }
        currentIndex++;
      }
    }
    
    lastSignal = java.util.Arrays.copyOf(signal,signal.length);
    println("sig len: " + signal.length + " last len = " + lastSignal.length + " diff: " + (endSampIndex-startSampIndex));
  }
  
  void draw()
  {
    if(recording || totalSamples < 1)
      return;
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
  
  void drawCurrentSignal()
  {
    int repititions = 5;
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
  
  void clearSamples()
  {
    totalSamples = 0;
    samps.clear();
  }
}
