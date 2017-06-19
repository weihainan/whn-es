package com.whn.waf.common.es.repository;


import com.whn.waf.common.es.domain.ESCommonDomain;
import org.springframework.stereotype.Repository;

@Repository
public interface ESCommonRepository extends ESBaseRepository<ESCommonDomain>, ESCommonRepositoryEnhance {
}
