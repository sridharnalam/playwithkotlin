package com.ideahamster.playkotlin.model

class ConfigureFailure(var response: String): SdkResponse {
    override fun toString(): String {
        return "ConfigureFailure $response"
    }
}