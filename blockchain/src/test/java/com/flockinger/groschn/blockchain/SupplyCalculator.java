package com.flockinger.groschn.blockchain;

import org.junit.Test;

public class SupplyCalculator {
                                        //650.000.000l;
  public final static Long TOTAL_SUPPLY = 650000000l;
  
  public final static Long BLOCKS_PER_MINUTE = 2l;
  public final static Long BLOCKS_PER_DAY = BLOCKS_PER_MINUTE * 60 * 24;
  public final static Long BLOCKS_PER_YEAR = BLOCKS_PER_DAY * 365;
  
  public final static Long ROUGH_BLOCK_PER_YEAR = 1000000l;
  
  public final static Double MINIMUM_GROSCHN_VALUE = 1 * Math.pow(10, -6);
  
  @Test
  public void testBla() {
    double initialBlockReward = 100.00;
                       //250.000
    long blockStepSize = 250000; 
    
    double stepDivisor = 1.39794001314;
    /*
     Used as such that once each block step the reward is recalculated like this:
     
     1 / (1 + 10^(-1 * stepDivisor))
     
     
     * */
    
    int yearCount = 0;
    double generatedGroschn = 0.00;
    
    final double blockSizeIncreasingFactor = 1.05;
    final double stepDivisorAdder = 0.00000000001;
    
    Integer yearsUntilDone = 0;
    
    double blockReward = initialBlockReward;
    double lastBlockReward = initialBlockReward;
    long blockCount = 0l;
    
    
    
    /*while(generatedGroschn < TOTAL_SUPPLY) {
      blockCount = 0;
      lastBlockReward = initialBlockReward;
      blockReward = initialBlockReward;
      generatedGroschn = 0;
      yearCount = 0;*/
      
      while(blockReward >= MINIMUM_GROSCHN_VALUE) {
        generatedGroschn += blockReward;
        blockCount++;
        
        if(blockCount%blockStepSize==0) {
          lastBlockReward = blockReward;
          
          blockReward = lastBlockReward / (1 + Math.pow(10, (-1 * stepDivisor)));
        }
        
        if(blockCount%ROUGH_BLOCK_PER_YEAR == 0) {
          yearCount++;
          System.out.println("year: " + yearCount);
          System.out.printf("blk: %,d\t\t reward: %.7f\t\t groschn mined: %,.3f %n",blockCount,blockReward,generatedGroschn);
        }
      }
      
      
      System.out.println("year: " + yearCount);
      System.out.printf("blk: %,d\t\t reward: %.7f\t\t groschn mined: %,.3f %n",blockCount,blockReward,generatedGroschn);
      
      
      
     // blockStepSize *= blockSizeIncreasingFactor;
      stepDivisor += stepDivisorAdder;
    //}
    
    
    System.out.println("best blockStepSize: " + blockStepSize);
    System.out.println("best stepDivisor: " + stepDivisor);
    
    
    
    // with multiplier 0.99:  199,999,990.055
    
    
    // step 100000l with multiplier 0.9:    19,999,999
    // step  50000l with multiplier 0.9:     9,999,999
    
    
    System.out.printf("blk: %,d\t\t reward: %.7f\t\t groschn mined: %,.3f %n",blockCount,blockReward,generatedGroschn);
  }
  
  
}
