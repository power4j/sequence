package com.power4j.sequence.example;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2022/6/1
 * @since 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SequenceJdbcExampleApplicationTest {

	@LocalServerPort
	private int port;

	@Autowired
	private TestRestTemplate restTemplate;

	@Test
	public void shouldReturnSeq() {
		List<String> contents = Arrays.asList("seq", "query_count", "update_count");
		String rsp = restTemplate.getForObject("http://localhost:" + port + "/", String.class);
		System.out.printf("payload: %s\n", rsp);
		assertThat(rsp).contains(contents);
	}

}
