package com.extropies.www.eoshackathon.BleUtil;

import static com.extropies.www.eoshackathon.BleUtil.ExBleException.ERROR_CODE_OTHER;

/**
 * Created by inst on 18-6-9.
 */

public class OtherException extends ExBleException{
    public OtherException(String description) {
        super(ERROR_CODE_OTHER, description);
    }
}
