package com.ideahamster.playkotlin.model

class ConfigureSuccess(var response: String): SdkResponse {
    override fun toString(): String {
        return "ConfigureSuccess $response"
    }
}