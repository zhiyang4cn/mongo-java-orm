package com.googlecode.mjorm.query;

import com.googlecode.mjorm.MongoDao;
import com.googlecode.mjorm.query.modifiers.AbstractQueryModifiers;
import com.mongodb.DBCollection;
import com.mongodb.DBEncoder;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

public class DaoModifier
	extends AbstractQueryModifiers<DaoModifier> {

	private DaoQuery query;
	private boolean atomic = false;

	/**
	 * Creates the {@link DaoModifier}.
	 * @param mongoDao the {@link MongoDao}
	 */
	public DaoModifier() {
		this.clear();
	}

	/**
	 * Creates the {@link DaoModifier}.
	 * @param mongoDao the {@link MongoDao}
	 */
	public DaoModifier(DaoQuery query) {
		this.query = query;
		this.clear();
	}

	/**
	 * Asserts that the {@link DaoModifier} is valid.
	 * Throws an exception if not.
	 */
	public void assertValid() {
		if (query==null) {
			throw new IllegalStateException("query must be specified");
		}
		query.assertValid();
	}

	/**
	 * Clears this modifier query.
	 */
	public void clear() {
		super.clear();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected DaoModifier self() {
		return this;
	}

	/**
	 * Returns the {@link DaoQuery} tha this modifier will use.
	 * @return
	 */
	public DaoQuery getQuery() {
		return query;
	}

	/**
	 * Sets the {@link DaoQuery} that this modifier will use.
	 * @param query
	 * @return
	 */
	public DaoModifier setQuery(DaoQuery query) {
		this.query = query;
		return self();
	}

	/**
	 * @param atomic the atomic to set
	 */
	public DaoModifier setAtomic(boolean atomic) {
		this.atomic = atomic;
		return self();
	}

	/**
	 * Removes the objects matched by this query.
	 * @return the {@link WriteResult}
	 */
	public WriteResult deleteObjects() {
		assertValid();
		return query.getDB().getCollection(query.getCollection())
			.remove(query.toQueryObject());
	}

	/**
	 * Performs a findAndDelete for the current query.
	 * @return the object found and modified
	 */
	public <T> T findAndDelete(Class<T> clazz) {
		assertValid();
		DBObject object = query.getDB().getCollection(query.getCollection())
			.findAndModify(query.toQueryObject(), null, query.getSortDBObject(),
				true, null, false, false);
		return query.getObjectMapper().mapFromDBObject(object, clazz);
	}

	/**
	 * Performs a findAndDelete for the current query.
	 * @return the object found and modified
	 * @param fields the fields to populate on the return object
	 */
	public DBObject findAndDelete(DBObject fields) {
		assertValid();
		return query.getDB().getCollection(query.getCollection())
			.findAndModify(query.toQueryObject(), fields, query.getSortDBObject(),
				true, null, false, false);
	}

	/**
	 * Performs a findAndDelete for the current query.
	 * @return the object found and modified
	 * @param fields the fields to populate on the return object
	 */
	public DBObject findAndDelete() {
		assertValid();
		return findAndDelete((DBObject)null);
	}

	/**
	 * Performs a findAndModify for the current query.
	 * @param returnNew whether or not to return the new or old object
	 * @param upsert create new if it doesn't exist
	 * @param clazz the type of object
	 * @return the object
	 */
	public <T> T findAndModify(boolean returnNew, boolean upsert, Class<T> clazz) {
		assertValid();
		DBObject object = query.getDB().getCollection(query.getCollection())
			.findAndModify(query.toQueryObject(), null, query.getSortDBObject(),
				false, toModifierObject(), returnNew, upsert);
		return query.getObjectMapper().mapFromDBObject(object, clazz);
	}

	/**
	 * Performs a findAndModify for the current query.
	 * @param returnNew whether or not to return the new or old object
	 * @param upsert create new if it doesn't exist
	 * @param fields the fields to return
	 * @return the object
	 */
	public DBObject findAndModify(boolean returnNew, boolean upsert, DBObject fields) {
		assertValid();
		return query.getDB().getCollection(query.getCollection())
			.findAndModify(query.toQueryObject(), fields, query.getSortDBObject(),
				false, toModifierObject(), returnNew, upsert);
	}

	/**
	 * Performs a findAndModify for the current query.
	 * @param returnNew whether or not to return the new or old object
	 * @param upsert create new if it doesn't exist
	 * @param fields the fields to return
	 * @return the object
	 */
	public DBObject findAndModify(boolean returnNew, boolean upsert) {
		assertValid();
		return findAndModify(returnNew, upsert, (DBObject)null);
	}

	/**
	 * Performs an update with the current modifier object.
	 * @param upsert
	 * @param multi
	 * @param concern
	 * @param encoder
	 */
	public WriteResult update(boolean upsert, boolean multi, WriteConcern concern, DBEncoder encoder) {
		assertValid();
		DBCollection collection = query.getDB().getCollection(query.getCollection());
		if (concern==null) {
			concern = collection.getWriteConcern();
		}
		WriteResult result = collection.update(
			query.toQueryObject(), toModifierObject(),
			upsert, multi, concern, encoder);
		if (result.getError()!=null) {
			throw new MongoException(result.getError());
		}
		return result;
	}

	/**
	 * Performs an update with the current modifier object.
	 * @param upsert
	 * @param multi
	 * @param concern
	 */
	public WriteResult update(boolean upsert, boolean multi, WriteConcern concern) {
		return update(upsert, multi, concern, null);
	}

	/**
	 * Performs an update with the current modifier object.
	 * @param upsert
	 * @param multi
	 */
	public WriteResult update(boolean upsert, boolean multi) {
		return update(upsert, multi, null, null);
	}

	/**
	 * Performs a single update.
	 */
	public WriteResult update() {
		return update(false, false);
	}

	/**
	 * Performs a multi update.
	 */
	public WriteResult updateMulti() {
		return update(false, true);
	}

	/**
	 * Performs a single upsert.
	 */
	public WriteResult upsert() {
		return update(true, false);
	}

	/**
	 * Performs a multi upsert.
	 */
	public WriteResult upsertMulti() {
		return update(true, true);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DBObject toModifierObject() {
		DBObject ret = super.toModifierObject();
		if (this.atomic) {
			ret.put("$atomic", true);
		}
		return ret;
	}

	

}
