package com.example.service;

import com.example.dao.IDao;
import com.example.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.util.List;

public abstract class AbstractFacade<T> implements IDao<T> {

    private final Class<T> entityClass;

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public boolean create(T o) {
        Session session = null;
        Transaction tx = null;
        boolean etat = false;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.save(o);

            tx.commit();
            etat = true;
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return etat;
    }

    @Override
    public boolean delete(T o) {
        Session session = null;
        Transaction tx = null;
        boolean etat = false;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.delete(o);

            tx.commit();
            etat = true;
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return etat;
    }

    @Override
    public boolean update(T o) {
        Session session = null;
        Transaction tx = null;
        boolean etat = false;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            session.update(o);

            tx.commit();
            etat = true;
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return etat;
    }

    @Override
    public T findById(long id) {
        Session session = null;
        Transaction tx = null;
        T obj = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            obj = (T) session.get(entityClass, id);

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return obj;
    }

    @Override
    public List<T> findAll() {
        Session session = null;
        Transaction tx = null;
        List<T> list = null;

        try {
            session = HibernateUtil.getSessionFactory().openSession();
            tx = session.beginTransaction();

            list = session.createQuery("from " + entityClass.getSimpleName()).list();

            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            if (session != null) {
                session.close();
            }
        }

        return list;
    }
}