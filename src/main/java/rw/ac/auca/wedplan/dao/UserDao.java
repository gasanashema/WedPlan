package rw.ac.auca.wedplan.dao;

import org.hibernate.Session;
import org.hibernate.Transaction;
import rw.ac.auca.wedplan.model.User;

import java.util.List;

/**
 * Data Access Object for User entity handling database transactions.
 */
public class UserDao {

    public User save(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.save(user);
            tx.commit();
            return user;
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public User update(User user) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.update(user);
            tx.commit();
            return user;
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
            User user = session.get(User.class, id);
            if (user != null) {
                session.delete(user);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw e;
        } finally {
            session.close();
        }
    }

    public User findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.get(User.class, id);
        } finally {
            session.close();
        }
    }

    public List<User> findAll() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            return session.createQuery("SELECT u FROM User u ORDER BY u.id DESC", User.class).list();
        } finally {
            session.close();
        }
    }

    public boolean hasAssignedTasks(Long userId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Long count = session.createQuery(
                    "SELECT COUNT(t) FROM Task t WHERE t.assignedUser.id = :uid", Long.class)
                    .setParameter("uid", userId)
                    .uniqueResult();
            return count != null && count > 0;
        } finally {
            session.close();
        }
    }

    public boolean isEmailRegistered(String email, Long excludeId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            Long exclude = (excludeId == null) ? -1L : excludeId;
            Long count = session.createQuery(
                    "SELECT COUNT(u) FROM User u WHERE LOWER(u.email) = :email AND u.id != :id", Long.class)
                    .setParameter("email", email.toLowerCase().trim())
                    .setParameter("id", exclude)
                    .uniqueResult();
            return count != null && count > 0;
        } finally {
            session.close();
        }
    }
}
