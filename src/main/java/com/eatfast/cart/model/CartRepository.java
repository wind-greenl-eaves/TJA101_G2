package com.eatfast.cart.model;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.eatfast.cart.model.CartEntity;

@Repository
public interface CartRepository extends JpaRepository<CartEntity, Integer> {
	
    List<CartEntity> findByMemberId(Integer memberId);
    
    Optional<CartEntity> findByMemberIdAndMealId(Integer memberId, Integer mealId);
    
    void deleteByMemberId(Integer memberId);
}
