package ceri.serial.mlx90640;

public class MlxParameters {
    int kVdd; // int16_t kVdd
    int vdd25; // int16_t vdd25
    double KvPtat; // float KvPTAT
    double KtPtat; // float KtPTAT
    int vPtat25; // uint16_t vPTAT25
    double alphaPtat; // float alphaPTAT
    int gainEe; // int16_t gainEE
    double tgc; // float tgc
    double cpKv; // float cpKv
    double cpKta; // float cpKta
    int resolutionEe; // uint8_t resolutionEE
    int calibrationModeEe; // uint8_t calibrationModeEE
    double ksTa; // float KsTa
    double[] ksTo; // float ksTo[5]
    int[] ct; // int16_t ct[5]
    int[] alpha; // uint16_t alpha[768]    
    int alphaScale; // uint8_t alphaScale
    int[] offset; // int16_t offset[768]    
    byte[] kta; // int8_t kta[768]
    int ktaScale; // uint8_t ktaScale    
    byte[] kv; // int8_t kv[768]
    int kvScale; // uint8_t kvScale
    double[] cpAlpha; // float cpAlpha[2]
    int[] cpOffset; // int16_t cpOffset[2]
    double[] ilChessC; // float ilChessC[3] 
    int[] brokenPixels; // uint16_t brokenPixels[5]
    int[] outlierPixels; // uint16_t outlierPixels[5]  
}
