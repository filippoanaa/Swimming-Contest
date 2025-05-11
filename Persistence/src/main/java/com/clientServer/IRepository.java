package com.clientServer;

import com.clientServer.entities.Entity;

public interface IRepository<ID, E extends Entity<ID>> {
    E findOne(ID id);
    Iterable<E> findAll();
    void save(E entity);
    void delete(ID id);
    void update(E entity);

}
