package com.aep.backend.domain.abstraction;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;

public abstract class DefaultCrudService<R extends DefaultCrudRepository<E>, E extends DefaultEntity> {

    protected abstract R getRepository();

    public E salvar(E entity) {
        return getRepository().save(entity);
    }

    public Optional<E> buscarPorId(String id) {
        return getRepository().findById(id);
    }

    public List<E> listarTodos() {
        return getRepository().findAll();
    }

    public E atualizar(E entity) {
        E existente = getRepository().findById(entity.getId())
                .orElseThrow(() -> new NoSuchElementException("Registro não encontrado: " + entity.getId()));
        BeanUtils.copyProperties(entity, existente, nomesDePropriedadesNulas(entity));
        return getRepository().save(existente);
    }

    private static String[] nomesDePropriedadesNulas(Object origem) {
        BeanWrapper wrapper = new BeanWrapperImpl(origem);
        Set<String> nulas = new HashSet<>();
        for (PropertyDescriptor pd : wrapper.getPropertyDescriptors()) {
            if (wrapper.getPropertyValue(pd.getName()) == null) {
                nulas.add(pd.getName());
            }
        }
        return nulas.toArray(new String[0]);
    }

    public void deletar(String id) {
        getRepository().findById(id).ifPresent(e -> {
            if (e instanceof Ativavel a) {
                a.setAtivo(false);
                getRepository().save(e);
            } else {
                getRepository().deleteById(id);
            }
        });
    }
}
