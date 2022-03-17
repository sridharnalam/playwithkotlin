package com.ideahamster.playkotlin.model

class LinesSuccess(var response: String): SdkResponse {
    override fun toString(): String {
        return "LinesSuccess $response"
    }
}