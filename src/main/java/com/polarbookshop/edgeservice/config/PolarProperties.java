package com.polarbookshop.edgeservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "polar")
public class PolarProperties {
  private String logoutRedirectUri;

  public String getLogoutRedirectUri() {
    return logoutRedirectUri;
  }

  public void setLogoutRedirectUri(String greeting) {
    this.logoutRedirectUri = greeting;
  }
}
