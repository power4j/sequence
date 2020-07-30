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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@SpringBootApplication
public class SequenceMongoExampleApplication {

	@Autowired
	private Sequence<Long> sequence;

	@Autowired
	private SeqSynchronizer seqSynchronizer;

	public static void main(String[] args) {
		SpringApplication.run(SequenceMongoExampleApplication.class, args);
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

}
