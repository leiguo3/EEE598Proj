package edu.asu.msrs.artcelerationlibrary;

/**
 * Created by rlikamwa on 10/24/2016.
 * This class is used to describe input parameters of a transform. - YZ
 */

public class TransformTest {
    public int transformType;
    public int[] intArgs;
    public float[] floatArgs;
    TransformTest(int type, int[] args1, float[] args2){
        transformType = type;
        intArgs = args1;
        floatArgs = args2;
    }
}
