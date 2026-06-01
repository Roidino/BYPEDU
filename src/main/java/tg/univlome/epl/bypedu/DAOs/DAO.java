/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package tg.univlome.epl.bypedu.DAOs;

import java.util.List;

/**
 *
 * @author Savastano
 */
public interface DAO<T> {
    public List<T> getAll();
    public T getById(int id);
    public boolean delete(int id);
    public boolean ajoute(T t);
    public boolean update(T t);
}