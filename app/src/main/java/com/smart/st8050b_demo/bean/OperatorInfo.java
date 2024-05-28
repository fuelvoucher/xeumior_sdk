package com.smart.st8050b_demo.bean;

/**
 * Created by Administrator on 2018/2/2.
 */

public class OperatorInfo {
    private String _OperatorName;
    private String _OperatorValue;

    public OperatorInfo(String _OperatorName,String _OperatorValue)
    {
        set_OperatorName(_OperatorName);
        set_OperatorValue(_OperatorValue);
    }
    public String get_OperatorName() {
        return _OperatorName;
    }

    public void set_OperatorName(String _OperatorName) {
        this._OperatorName = _OperatorName;
    }



    public String get_OperatorValue() {
        return _OperatorValue;
    }

    public void set_OperatorValue(String _OperatorValue) {
        this._OperatorValue = _OperatorValue;
    }
}
