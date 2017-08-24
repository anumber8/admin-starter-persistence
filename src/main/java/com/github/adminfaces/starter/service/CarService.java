/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.github.adminfaces.starter.service;

import com.github.adminfaces.starter.infra.model.Filter;
import com.github.adminfaces.starter.infra.service.CrudService;
import com.github.adminfaces.starter.model.Car;
import com.github.adminfaces.starter.model.Car_;
import com.github.adminfaces.template.exception.BusinessException;
import org.apache.deltaspike.data.api.criteria.Criteria;

import javax.ejb.Stateless;
import java.io.Serializable;
import java.util.List;

import static com.github.adminfaces.template.util.Assert.has;

/**
 * @author rmpestano
 */
@Stateless
public class CarService extends CrudService<Car, Integer> implements Serializable {


    protected Criteria<Car, Car> configRestrictions(Filter<Car> filter) {

        Criteria<Car, Car> criteria = criteria();

        //create restrictions based on parameters map
        if (filter.hasParam("id")) {
            criteria.eq(Car_.id, filter.getIntParam("id"));
        }

        if (filter.hasParam("minPrice") && filter.hasParam("maxPrice")) {
            criteria.between(Car_.price, filter.getDoubleParam("minPrice"), filter.getDoubleParam("maxPrice"));
        } else if (filter.hasParam("minPrice")) {
            criteria.gtOrEq(Car_.price, filter.getDoubleParam("minPrice"));
        } else if (filter.hasParam("maxPrice")) {
            criteria.ltOrEq(Car_.price, filter.getDoubleParam("maxPrice"));
        }

        //create restrictions based on filter entity
        if (has(filter.getEntity())) {
            Car filterEntity = filter.getEntity();
            if (has(filterEntity.getModel())) {
                criteria.likeIgnoreCase(Car_.model, filterEntity.getModel());
            }

            if (has(filterEntity.getPrice())) {
                criteria.eq(Car_.price, filterEntity.getPrice());
            }

            if (has(filterEntity.getName())) {
                criteria.likeIgnoreCase(Car_.name, filterEntity.getName());
            }
        }
        return criteria;
    }

    public List<Car> listByModel(String model) {
        return criteria()
                .eqIgnoreCase(Car_.model, model)
                .getResultList();
    }

    public List<String> getModels(String query) {
        return criteria()
                .select(String.class, attribute(Car_.model))
                .likeIgnoreCase(Car_.model, "%"+query+"%")
                .getResultList();
    }

    public void beforeInsert(Car car) {
        validate(car);
    }

    public void beforeUpdate(Car car) {
        validate(car);
    }

    public void validate(Car car) {
        BusinessException be = new BusinessException();
        if (!car.hasModel()) {
            be.addException(new BusinessException("Car model cannot be empty"));
        }
        if (!car.hasName()) {
            be.addException(new BusinessException("Car name cannot be empty"));
        }

        if (!has(car.getPrice())) {
            be.addException(new BusinessException("Car price cannot be empty"));
        }

        if (criteria()
                .eqIgnoreCase(Car_.name, car.getName())
                .eq(Car_.id, car.getId())
                .select(Long.class, count(Car_.id))
                .getSingleResult() > 0) {

            be.addException(new BusinessException("Car name must be unique"));
        }

        if (has(be.getExceptionList())) {
            throw be;
        }
    }
}