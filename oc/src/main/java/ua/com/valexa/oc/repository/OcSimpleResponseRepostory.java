package ua.com.valexa.oc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ua.com.valexa.oc.model.OcSimpleResponse;

import java.util.UUID;

@Repository
public interface OcSimpleResponseRepostory extends JpaRepository<OcSimpleResponse, UUID> {
}
