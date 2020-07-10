/*
 * Copyright (c) 2020 ChenJun(power4j@outlook.com)
 * Sequence is licensed under Mulan PSL v2.
 * You can use this software according to the terms and conditions of the Mulan PSL v2.
 * You may obtain a copy of Mulan PSL v2 at:
 *          http://license.coscl.org.cn/MulanPSL2
 * THIS SOFTWARE IS PROVIDED ON AN "AS IS" BASIS, WITHOUT WARRANTIES OF ANY KIND,
 * EITHER EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO NON-INFRINGEMENT,
 * MERCHANTABILITY OR FIT FOR A PARTICULAR PURPOSE.
 * See the Mulan PSL v2 for more details.
 */

package com.power4j.sequence.example;

import com.power4j.kit.seq.core.Sequence;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@SpringBootApplication
public class SequenceExampleApplication {

	@Autowired
	private Sequence<Long> sequence;

	public static void main(String[] args) {
		SpringApplication.run(SequenceExampleApplication.class, args);
	}

	@GetMapping("/seq")
	public List<String> getSequence(@RequestParam(required = false) Integer size) {
		size = (size == null || size <= 0) ? 10 : size;
		List<String> list = new ArrayList<>(size);
		for (int i = 0; i < size; ++i) {
			list.add(sequence.nextStr());
		}
		return list;
	}

}
