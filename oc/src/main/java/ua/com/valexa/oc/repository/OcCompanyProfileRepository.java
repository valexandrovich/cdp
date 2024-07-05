package ua.com.valexa.oc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.com.valexa.oc.model.OcCompanyProfile;

import java.util.UUID;

public interface OcCompanyProfileRepository extends JpaRepository<OcCompanyProfile, UUID> {
}
