package com.Pf.product_service.repository;

import com.Pf.product_service.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByProductId(Long productId);

    // Atomic decrement: chi thanh cong neu du hang, tra ve so dong bi anh huong (0 hoac 1)
    @Modifying
    @Query("UPDATE ProductVariant v SET v.quantity = v.quantity - :qty " +
           "WHERE v.id = :id AND v.quantity >= :qty")
    int decreaseStock(@Param("id") Long id, @Param("qty") Integer qty);

    // Dung khi nhap kho / hoan hang, khong can dieu kien so sanh
    @Modifying
    @Query("UPDATE ProductVariant v SET v.quantity = v.quantity + :qty WHERE v.id = :id")
    int increaseStock(@Param("id") Long id, @Param("qty") Integer qty);
}