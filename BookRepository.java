package com.library.repository;

import com.library.entity.Book;
import com.library.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    Page<Book> findByCategoryIdAndStatus(Long categoryId, BookStatus status, Pageable pageable);

    Page<Book> findByTypeIdAndStatus(Long typeId, BookStatus status, Pageable pageable);

    Page<Book> findByCategoryIdAndTypeIdAndStatus(Long categoryId, Long typeId, BookStatus status, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = :status AND " +
           "(LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           " LOWER(b.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Book> searchBooks(@Param("keyword") String keyword, @Param("status") BookStatus status, Pageable pageable);

    @Query("SELECT b.title FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) AND b.status = 'AVAILABLE'")
    List<String> findTitleSuggestions(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT b.author FROM Book b WHERE LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) AND b.status = 'AVAILABLE'")
    List<String> findAuthorSuggestions(@Param("query") String query, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.status = :status ORDER BY b.downloadCount DESC, b.viewCount DESC")
    Page<Book> findPopularBooks(@Param("status") BookStatus status, Pageable pageable);

    @Query("SELECT b FROM Book b WHERE b.isFeatured = true AND b.status = :status ORDER BY b.createdAt DESC")
    Page<Book> findFeaturedBooks(@Param("status") BookStatus status, Pageable pageable);

    long countByStatus(BookStatus status);

    long countByCategoryId(Long categoryId);
}

