package com.github.tomakehurst.wiremock;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.extension.Parameters;
import com.github.tomakehurst.wiremock.http.Request;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.github.tomakehurst.wiremock.matching.RequestMatcher;
import com.github.tomakehurst.wiremock.matching.RequestMatcherExtension;
import com.github.tomakehurst.wiremock.testsupport.WireMockTestClient;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.requestMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class CustomMatchingAcceptanceTest {

    @SuppressWarnings("unchecked")
    @Rule
    public WireMockRule wmRule = new WireMockRule(wireMockConfig().dynamicPort().extensions(MyExtensionRequestMatcher.class));

    WireMockTestClient client;
    WireMock wm;

    @Before
    public void init() {
        client = new WireMockTestClient(wmRule.port());
        wm = new WireMock(wmRule.port());
    }

    @Test
    public void inlineRequestMatcherExtension() {
        wmRule.stubFor(requestMatching(new MyRequestMatcher()).willReturn(aResponse().withStatus(200)));
        assertThat(client.get("/correct").statusCode(), is(200));
        assertThat(client.get("/wrong").statusCode(), is(404));
    }

    @Test
    public void inlineRequestMatcher() {
        wmRule.stubFor(requestMatching(new RequestMatcher() {
            public boolean isMatchedBy(Request request) {
                return request.getUrl().contains("correct");
            }
        }).willReturn(aResponse().withStatus(200)));

        assertThat(client.get("/correct").statusCode(), is(200));
        assertThat(client.get("/wrong").statusCode(), is(404));
    }

    @Test
    public void requestMatcherAsExtension() {
        wm.register(requestMatching("path-contains-param", Parameters.one("path", "findthis")).willReturn(aResponse().withStatus(200)));
        assertThat(client.get("/findthis/thing").statusCode(), is(200));
    }

    public static class MyRequestMatcher extends RequestMatcherExtension {

        @Override
        public boolean isMatchedBy(Request request, Parameters parameters) {
            return request.getUrl().contains("correct");
        }
    }

    public static class MyExtensionRequestMatcher extends RequestMatcherExtension {

        @Override
        public String name() {
            return "path-contains-param";
        }

        @Override
        public boolean isMatchedBy(Request request, Parameters parameters) {
            String pathSegment = parameters.getString("path");
            return request.getUrl().contains(pathSegment);
        }
    }
}
