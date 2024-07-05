package ua.com.valexa.etisaver.repository;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.etisaver.entity.EtiProfile;

@Repository
public interface EtiProfileRepository extends JpaRepository<EtiProfile, String> {

    EtiProfile findByIrsEin(String irsEin);
    Page<EtiProfile> findAll(Pageable pageable);

}
