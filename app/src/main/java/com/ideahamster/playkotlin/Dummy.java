package com.ideahamster.playkotlin;

import com.ideahamster.playkotlin.model.SdkResponse;

public class Dummy {

    DemoInterface demo;

    Dummy() {
        demo = new DemoInterface() {
        };
        SdkResponse sdkResponse = new SdkResponse(){};

        returnObject(sdkResponse, SdkResponse.class);
    }

    public <T extends SdkResponse> T returnObject(T sdkResponse, Class<? extends SdkResponse> clazz) {
        if(clazz.isInstance(sdkResponse)){
            clazz.cast(sdkResponse);
            return sdkResponse;
        } else {
            return null;
        }
    }
    static class DemoImpl implements DemoInterface {

    }
    interface DemoInterface {}
}
