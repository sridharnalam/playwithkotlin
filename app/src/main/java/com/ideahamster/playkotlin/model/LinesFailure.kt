package com.ideahamster.playkotlin.model

class LinesFailure(var response: String): SdkResponse {
    override fun toString(): String {
        return "LinesFailure $response"
    }
}