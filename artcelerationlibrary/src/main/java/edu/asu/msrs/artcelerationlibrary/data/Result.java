package edu.asu.msrs.artcelerationlibrary.data;

import android.os.Bundle;

/**
 * Created by Lei on 11/5/2016.
 */

public class Result extends Base {

    public static Result create(Bundle bundle){
        Result result = new Result();
        result.readFromBundle(bundle);
        return result;
    }

}
