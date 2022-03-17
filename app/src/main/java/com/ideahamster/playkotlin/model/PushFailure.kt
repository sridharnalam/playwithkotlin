package com.ideahamster.playkotlin.model

class PushFailure(var response: String): SdkResponse {
    override fun toString(): String {
        return "PushFailure $response"
    }
}