package ch.rodano.core.dao;

import java.util.List;
import java.util.UUID;

public interface BaseProjectDAO<T> {

	List<T> findByProject(UUID projectId);

	T findByProjectAndCode(UUID projectId, String code);

	T findById(UUID id);

	T save(T entity);

	void delete(UUID id);
}
