package com.demo.repository;

import com.demo.model.entity.BorrowingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {
    List<BorrowingRecord> findByStatus(String status);

    @Query("""
        select e.name, sum(d.quantity)
        from BorrowingDetail d
        join d.equipment e
        join d.borrowingRecord r
        where r.status = 'EXPORTED'
        group by e.id, e.name
        order by sum(d.quantity) desc
    """)
    List<Object[]> getBorrowedEquipmentStats();
}
