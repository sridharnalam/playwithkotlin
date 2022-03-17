package com.ideahamster.playkotlin.model

class PushSuccess(var response: String): SdkResponse {
    override fun toString(): String {
        return "PushSuccess $response"
    }
}