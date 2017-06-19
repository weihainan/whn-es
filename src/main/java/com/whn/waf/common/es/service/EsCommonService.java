package com.whn.waf.common.es.service;

import com.whn.waf.common.es.domain.ESCommonDomain;
import com.whn.waf.common.es.repository.ESCommonRepository;
import com.whn.waf.common.es.supprot.CustomListParam;
import com.whn.waf.common.es.supprot.ESProvider;
import com.whn.waf.common.es.supprot.EsConstants;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EsCommonService {

    public EsCommonService() {
        // empty
    }

    private static final Logger logger = LoggerFactory.getLogger(EsCommonService.class);
    @Autowired
    private ESCommonRepository eSCommonRepository;

    public Page<ESCommonDomain> query(BoolQueryBuilder queryBuilder, String index, String type, CustomListParam listParam) {
        try {
            return eSCommonRepository.queryAll(queryBuilder, index, type, listParam);
        } catch (IndexNotFoundException e) {
            logger.error("query record in es error", e);
        }
        return null;
    }

    public Object query(String[] indexes, String[] types, HashMap<String, Object> query) {
        return eSCommonRepository.queryForRest(indexes, types, query);
    }

    public void delete(String id) {
        eSCommonRepository.delete(id);
    }

    public ESCommonDomain findOne(String id) {
        return eSCommonRepository.findOne(id);
    }

    public List<ESCommonDomain> batchSaveCustom(List<ESCommonDomain> esDomains) {
        List<ESCommonDomain> esCommonDomains = new ArrayList<>();
        for (int i = 0; i < esDomains.size(); i++) {
            esCommonDomains.add(save(esDomains.get(i)));
        }
        return esCommonDomains;
    }

    public ESCommonDomain saveCustom(ESCommonDomain esDomain) {
        ESCommonDomain _esDomain = null;
        Map<String, Object> indexes = esDomain.getIndexes();
        if (indexes == null) {
            indexes = new HashMap<>();
        }
        if (StringUtils.isNotBlank(esDomain.getId())) {
            _esDomain = eSCommonRepository.findOne(esDomain.getId());
        }
        if (_esDomain == null) {
            indexes.put("create_time", System.currentTimeMillis());
            esDomain.setIndexes(indexes);
            return save(esDomain);
        } else {
            Map<String, Object> _indexes = _esDomain.getIndexes();
            _esDomain.setIndexes(indexes);
            indexes.put("create_time", _indexes.get("create_time"));
            indexes.put("application", _indexes.get("application"));
            indexes.put("update_time", System.currentTimeMillis());
            _esDomain.setData(esDomain.getData());
            return save(_esDomain);
        }
    }

    public ESCommonDomain save(ESCommonDomain esDomain) {
        // 检测是否已经手动建立过索引和mapping
        if (!EsConstants.containsKeys(ESProvider.getIndex(), ESProvider.getType())) {
            eSCommonRepository.getMappingByAlias(ESProvider.getIndex(), ESProvider.getType());
            //不报错说明有
            EsConstants.addKey(ESProvider.getIndex(), ESProvider.getType());
        }
        return eSCommonRepository.save(esDomain);
    }

    public void deleteByQuery(String index, String type, CustomListParam listParam) {
        eSCommonRepository.deleteByQuery(index, type, listParam);
    }

    public void deleteByQuery(String index, String type, BoolQueryBuilder queryBuilder) {
        eSCommonRepository.deleteByQuery(index, type, queryBuilder);
    }

    public void deleteByRawQuery(String index, String type, HashMap<String, Object> query) {
        eSCommonRepository.deleteByRawQuery(index, type, query);
    }


    public boolean putMapping(Object mappings) {
        return eSCommonRepository.putMapping(ESProvider.getIndex(), ESProvider.getType(), mappings);
    }

    public Object getMapping() {
        return eSCommonRepository.getMapping(ESProvider.getIndex(), ESProvider.getType());
    }

    public boolean createIndex(String indexName) {
        return eSCommonRepository.createIndex(indexName);
    }

    public boolean deleteIndex(String indexName) {
        boolean deleteIndex = eSCommonRepository.deleteIndex(indexName);
        EsConstants.clearIndexs();
        return deleteIndex;
    }

    public Object aliases(Object actions) {
        EsConstants.clearIndexs();
        return eSCommonRepository.aliases(actions);
    }

    public Object getAliases(String alias) {
        return eSCommonRepository.getAliases(alias);
    }
}
