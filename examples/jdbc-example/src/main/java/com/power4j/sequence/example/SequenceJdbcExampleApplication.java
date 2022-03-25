/*
 * Copyright 2020 ChenJun (power4j@outlook.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.power4j.sequence.example;

import com.power4j.kit.seq.core.Sequence;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@SpringBootApplication
public class SequenceJdbcExampleApplication {

	private final static int MAX_SEQ_NAME = 40;

	private final Sequence<Long> sequence;

	private final SeqSynchronizer seqSynchronizer;

	private final SeqService seqService;

	public static void main(String[] args) {
		SpringApplication.run(SequenceJdbcExampleApplication.class, args);
	}

	@GetMapping("/")
	public Map<String, Object> getSequence(@RequestParam(required = false) Integer size) {
		size = (size == null || size <= 0) ? 10 : size;
		List<String> list = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			list.add(sequence.nextStr());
		}
		Map<String, Object> data = new HashMap<>();
		data.put("query_count", Long.toString(seqSynchronizer.getQueryCounter()));
		data.put("update_count", Long.toString(seqSynchronizer.getUpdateCounter()));
		data.put("seq", list);
		return data;
	}

	@GetMapping("/name/{name}")
	public List<String> getSequence(@PathVariable String name, @RequestParam(required = false) Integer size) {
		size = (size == null || size <= 0) ? 10 : size;
		if (name.length() > MAX_SEQ_NAME) {
			name = name.substring(0, 40);
		}
		List<String> data = new ArrayList<>(10);
		for (int i = 0; i < size; ++i) {
			data.add(seqService.getForName(name));
		}
		return data;
	}

}
