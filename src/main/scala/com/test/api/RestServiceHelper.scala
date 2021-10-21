package com.test.api

import org.apache.http.client.config.RequestConfig
import org.apache.http.client.methods.{HttpDelete, HttpPost}
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.{HttpClientBuilder, HttpClients}
import org.apache.http.util.EntityUtils
import play.api.libs.json.{JsValue, Json}

import scala.util.{Failure, Success, Try}

object RestServiceHelper {
  private final val VALID_HTTP_STATUS_CODE = 200
  private final val REQUEST_TYPE_POST = "POST"
  //private final val REQUEST_TYPE_DELETE = "DELETE"

  def executeRequest(url: String,
                     apiKey: String,
                     accessToken: String,
                     requestType: String,
                     jsonRequest: Option[String] = None,
                     headerType: String = "application/json"): Option[String] = {
    Try {
      val httpClient = HttpClients.createDefault()
      val httpRequest = if (requestType == REQUEST_TYPE_POST) {
        jsonRequest.fold[HttpPost](new HttpPost(url))(json => {
          val post = new HttpPost(url)
          println("json " + json.replaceAll("\"", "\\\""))
          post.setEntity(new StringEntity(json.replaceAll("\"", "\\\"")))
          post
        })
      } else new HttpDelete(url);
      httpRequest.setHeader("content-type", headerType);
      httpRequest.setHeader("Authorization", "Bearer " + accessToken)
      httpRequest.setHeader("Key", apiKey)

      println("htp: " + httpRequest.toString)
      println("htp url:" + url)
      httpClient.execute(httpRequest)
    } match {
      case Success(response) =>
        val statusCode = response.getStatusLine.getStatusCode
        println("response: " + response.toString)
        print("topic status_code: " + statusCode)
        if (statusCode == VALID_HTTP_STATUS_CODE) {
          Some(EntityUtils.toString(response.getEntity, "UTF-8"))
        } else None
      case Failure(exception) =>
        throw exception
    }
  }

  def getAuthorizedToken(url: String, authorizationType: String, headerType: String = "application/json"):
  (Option[String], Option[String]) = {
    Try {
      println("auth_url: " + url)
      val httpClient = HttpClients.createDefault()
      val post = new HttpPost(url);
      post.setHeader("content-type", headerType);
      post.setEntity(new StringEntity(s"""{\"grant_type\": \"$authorizationType\"}"""))

      httpClient.execute(post)
    } match {
      case Success(response) =>
        val statusCode = response.getStatusLine.getStatusCode
        println("====================================")
        println("status_code" + statusCode)
        if (statusCode == VALID_HTTP_STATUS_CODE) {
          val json: JsValue = Json.parse(EntityUtils.toString(response.getEntity, "UTF-8"))
          (Some(json.apply("access_token").toString().replaceAll("\"", "")), Some(json.apply("refresh_token")
            .toString().replaceAll("\"", "")))
        } else (None, None)
      case Failure(exception) =>
        throw exception
    }
  }

  def getAuthorizedRefreshToken(url: String, headerType: String = "application/json"): (Option[String], Option[String]) = {
    Try {
      val requestConfig = RequestConfig.custom().setConnectionRequestTimeout(1000).build()
      val httpClient = HttpClientBuilder.create().setDefaultRequestConfig(requestConfig).build()
      val post = new HttpPost(url);
      post.setHeader("content-type", headerType);
      //post.setEntity(new StringEntity(s"""{\"grant_type\": \"refresh_token\"}"""))

      println("url: " + url)
      httpClient.execute(post)
    } match {
      case Success(response) =>
        val statusCode = response.getStatusLine.getStatusCode
        println("status code: " + statusCode)
        if (statusCode == VALID_HTTP_STATUS_CODE) {
          val json: JsValue = Json.parse(EntityUtils.toString(response.getEntity, "UTF-8"))
          (Some(json.apply("access_token").toString().replaceAll("\"", "")), Some(json.apply("refresh_token")
            .toString().replaceAll("\"", "")))
        } else (None, None)
      case Failure(exception) =>
        throw exception
    }
  }

  def updateSecret(url: String, authorizationType: String, headerType: String = "application/json")= {
    Try {
      println("auth_url: " + url)
      val httpClient = HttpClients.createDefault()
      val post = new HttpPost(url);
      post.setHeader("content-type", headerType);

      httpClient.execute(post)
    } match {
      case Success(response) =>
        val statusCode = response.getStatusLine.getStatusCode
        println("====================================")
        println("SECRET UPDATED WITH STATUS CODE" + statusCode)
      case Failure(exception) =>
        throw exception
    }
  }
}


