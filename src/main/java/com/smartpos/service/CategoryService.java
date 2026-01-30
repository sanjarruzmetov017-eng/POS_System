package com.smartpos.service;

import com.smartpos.model.Category;
import com.smartpos.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private com.smartpos.util.AppSession session;

    public List<Category> findAll() {
        return categoryRepository.findByTenantId(session.getCurrentTenant().getId());
    }

    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id)
                .filter(c -> c.getTenant().getId().equals(session.getCurrentTenant().getId()));
    }

    public Category save(Category category) {
        if (category.getTenant() == null) {
            category.setTenant(session.getCurrentTenant());
        }
        return categoryRepository.save(category);
    }

    public void delete(Long id) {
        findById(id).ifPresent(c -> categoryRepository.delete(c));
    }
}
