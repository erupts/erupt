package xyz.erupt.flow.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import xyz.erupt.flow.bean.entity.OaForms;

public interface OaFormsRepository extends JpaRepository<OaForms, Long> {
}
