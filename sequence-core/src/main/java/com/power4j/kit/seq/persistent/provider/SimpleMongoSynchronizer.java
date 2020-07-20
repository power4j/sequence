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

package com.power4j.kit.seq.persistent.provider;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.*;
import com.mongodb.client.result.UpdateResult;
import com.power4j.kit.seq.persistent.AddState;
import com.power4j.kit.seq.persistent.SeqSynchronizer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author CJ (power4j@outlook.com)
 * @date 2020/7/17
 * @since 1.0
 */
@Slf4j
@AllArgsConstructor
public class SimpleMongoSynchronizer implements SeqSynchronizer {

	private final AtomicLong queryCount = new AtomicLong();

	private final AtomicLong updateCount = new AtomicLong();

	private final AtomicReference<MongoCollection<Document>> collectionRef = new AtomicReference<>();

	private final String dataBaseName;

	private final String collectionName;

	private final MongoClient mongoClient;

	/**
	 * MongoDB creates new collections when you first store data for the collections.
	 */
	public void createCollection() {
		collectionRef.compareAndSet(null, doCreateCollection());
	}

	public void dropCollection() {
		Optional.ofNullable(collectionRef.get()).ifPresent(col -> col.drop());
	}

	private MongoCollection<Document> doCreateCollection() {
		MongoCollection<Document> col = mongoClient.getDatabase(dataBaseName).getCollection(collectionName);
		String idxName = col.createIndex(
				Indexes.compoundIndex(Indexes.ascending(DocKeys.KEY_SEQ_NAME, DocKeys.KEY_SEQ_PARTITION)),
				new IndexOptions().unique(true));
		log.info("Index created :{}", idxName);
		return col;
	}

	private MongoCollection<Document> ensureCollection() {
		return collectionRef.updateAndGet(col -> (col != null ? col : doCreateCollection()));
	}

	protected Bson getSeqSelector(String name, String partition) {
		return Filters.and(Filters.eq(DocKeys.KEY_SEQ_NAME, name), Filters.eq(DocKeys.KEY_SEQ_PARTITION, partition));
	}

	protected Bson getValueSelector(String name, String partition, Long value) {
		return Filters.and(Filters.eq(DocKeys.KEY_SEQ_NAME, name), Filters.eq(DocKeys.KEY_SEQ_PARTITION, partition),
				Filters.eq(DocKeys.KEY_SEQ_VALUE, value));
	}

	@Override
	public boolean tryCreate(String name, String partition, long nextValue) {
		MongoCollection<Document> col = ensureCollection();
		Bson query = getSeqSelector(name, partition);
		Bson op = Updates.combine(Updates.setOnInsert(DocKeys.KEY_SEQ_VALUE, nextValue),
				Updates.setOnInsert(DocKeys.KEY_SEQ_CREATE_AT, LocalDateTime.now()),
				Updates.setOnInsert(DocKeys.KEY_SEQ_UPDATE_AT, null));
		UpdateResult result = col.updateOne(query, op, new UpdateOptions().upsert(true));
		return result.getUpsertedId() != null;
	}

	@Override
	public boolean tryUpdate(String name, String partition, long nextValueOld, long nextValueNew) {
		updateCount.incrementAndGet();
		MongoCollection<Document> col = ensureCollection();
		Bson query = getValueSelector(name, partition, nextValueOld);
		Bson op = Updates.combine(Updates.set(DocKeys.KEY_SEQ_VALUE, nextValueNew),
				Updates.set(DocKeys.KEY_SEQ_UPDATE_AT, LocalDateTime.now()));
		UpdateResult result = col.updateOne(query, op);
		return result.getModifiedCount() == 1;
	}

	@Override
	public AddState tryAddAndGet(String name, String partition, int delta, int maxReTry) {
		updateCount.incrementAndGet();
		MongoCollection<Document> col = ensureCollection();
		Bson query = getSeqSelector(name, partition);
		Bson op = Updates.combine(Updates.inc(DocKeys.KEY_SEQ_VALUE, delta),
				Updates.set(DocKeys.KEY_SEQ_UPDATE_AT, LocalDateTime.now()));
		Document doc = col.findOneAndUpdate(query, op,
				new FindOneAndUpdateOptions().returnDocument(ReturnDocument.BEFORE));
		if (doc == null) {
			return AddState.fail(1);
		}
		return AddState.success(doc.getLong(DocKeys.KEY_SEQ_VALUE), doc.getLong(DocKeys.KEY_SEQ_VALUE) + delta, 1);
	}

	@Override
	public Optional<Long> getNextValue(String name, String partition) {
		queryCount.incrementAndGet();
		MongoCollection<Document> col = ensureCollection();
		Bson query = getSeqSelector(name, partition);
		FindIterable<Document> itr = col.find(query);
		Document doc = itr.first();
		return Optional.ofNullable(doc == null ? null : doc.getLong(DocKeys.KEY_SEQ_VALUE));
	}

	@Override
	public void init() {
		createCollection();
	}

	@Override
	public void shutdown() {
		// nothing to do
	}

	@Override
	public long getQueryCounter() {
		return queryCount.get();
	}

	@Override
	public long getUpdateCounter() {
		return updateCount.get();
	}

	interface DocKeys {

		// @formatter:off

		String KEY_SEQ_NAME = "seqName";
		String KEY_SEQ_PARTITION = "seqPartition";
		String KEY_SEQ_VALUE = "seqNextValue";
		String KEY_SEQ_CREATE_AT = "seqCreateTime";
		String KEY_SEQ_UPDATE_AT = "seqUpdateTime";

		// @formatter:on

	}

}
