/*
 * Copyright 2002-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.http.server.reactive;

import java.time.Duration;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;

import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.ResponseCookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link ServletServerHttpResponse}.
 */
class ServletServerHttpResponseTests {

	@ParameterizedTest
	@ValueSource(longs = {
			36_500L * 24 * 60 * 60,  // ~100 years
			1L << 32,                // 2^32 seconds
			Long.MAX_VALUE
	})
	void applyCookiesWithMaxAgeExceedingIntegerRange(long maxAgeSeconds) throws Exception {
		HttpServletResponse servletResponse = mock();
		when(servletResponse.getOutputStream()).thenReturn(mock(ServletOutputStream.class));
		ServletServerHttpResponse response = new ServletServerHttpResponse(servletResponse,
				mock(AsyncContext.class), DefaultDataBufferFactory.sharedInstance,
				mock(ServletServerHttpRequest.class));

		response.getCookies().add("id", ResponseCookie.from("id", "1")
				.maxAge(Duration.ofSeconds(maxAgeSeconds)).build());
		response.applyCookies();

		ArgumentCaptor<Cookie> captor = ArgumentCaptor.forClass(Cookie.class);
		verify(servletResponse).addCookie(captor.capture());
		assertThat(captor.getValue().getMaxAge()).isPositive();
	}

}
