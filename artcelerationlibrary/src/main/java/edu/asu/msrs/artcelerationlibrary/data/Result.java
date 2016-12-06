package edu.asu.msrs.artcelerationlibrary.data;

import android.os.Bundle;

/**
 * Created by Lei on 11/5/2016.
 * Result Data: Used to pass image back to client after transform
 */


public class Result extends Base {

    public static Result create(Bundle bundle){
        Result result = new Result();
        result.readFromBundle(bundle);
        return result;
    }

}
