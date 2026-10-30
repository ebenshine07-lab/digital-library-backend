package com.library.repository;

import com.library.entity.BookType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookTypeRepository extends JpaRepository<BookType, Long> {

    Optional<BookType> findByNameIgnoreCase(String name);

    List<BookType> findByIsActiveTrueOrderByNameAsc();
}

