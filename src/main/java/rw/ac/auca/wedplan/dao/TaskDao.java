package rw.ac.auca.wedplan.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.wedplan.model.Task;

import java.util.List;

public class TaskDao {

    public Task save(Task task) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(task);
            tx.commit();
            return task;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public Task update(Task task) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(task);
            tx.commit();
            return task;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public void delete(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            Task task = session.get(Task.class, id);
            if (task != null) {
                session.delete(task);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public Task findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.get(Task.class, id);
        } finally {
            session.close();
        }
    }

    public List<Task> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("SELECT t FROM Task t LEFT JOIN FETCH t.assignedUser ORDER BY t.id DESC", Task.class).list();
        } finally {
            session.close();
        }
    }

    public List<Task> findByUserId(Long userId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery(
                    "SELECT t FROM Task t LEFT JOIN FETCH t.assignedUser WHERE t.assignedUser.id = :uid ORDER BY t.deadline ASC", Task.class)
                    .setParameter("uid", userId)
                    .list();
        } finally {
            session.close();
        }
    }
}
