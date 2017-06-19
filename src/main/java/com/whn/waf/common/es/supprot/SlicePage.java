package com.whn.waf.common.es.supprot;

import com.whn.waf.common.base.constant.ErrorCode;
import com.whn.waf.common.exception.WafBizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.List;

/**
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
public class SlicePage implements Pageable{
    /**
     * 偏移量
     */
    private int offset = 0;

    /**
     * 每页限制
     */
    private int limit = 10;

    /**
     * 排序
     */
    private Sort sort;

    /**
     * 总数统计标志
     */
    private boolean count = false;

    public SlicePage addSort(Sort sort) {
        if (sort == null) {
            return this;
        }
        if (this.sort == null) {
            this.sort = sort;
        } else {
            this.sort.and(sort);
        }
        return this;
    }

    public SlicePage addDefaultSort(Sort sort) {
        if (this.sort == null) {
            this.sort = sort;
        }
        return this;
    }

    public SlicePage forceSort(Sort sort) {
        this.sort = sort;
        return this;
    }

    public SlicePage parseOrderBys(String orderBy) {
        if (!StringUtils.isBlank(orderBy)) {
            String[] sortArray = orderBy.split("\\s+and\\s+");
            List<Sort.Order> orders = new ArrayList<>(sortArray.length);
            for (String sortStr : sortArray) {
                orders.add(parseOrderBy(sortStr));
            }
            sort = new Sort(orders);
        }
        return this;
    }

    private Sort.Order parseOrderBy(String orderBy) {
        String[] sortValues = orderBy.trim().split("\\s+");
        String sortField;
        Sort.Direction direction;
        if (sortValues.length == 2) {
            sortField = sortValues[0];
            direction = Sort.Direction.fromStringOrNull(sortValues[1]);
            if (direction == null) {
                throw WafBizException.of("不合法的排序" + sortValues[1], ErrorCode.INVALID_QUERY);
            }
        } else if (sortValues.length == 1) {
            sortField = sortValues[0];
            direction = Sort.Direction.ASC;
        } else {
            throw WafBizException.of("排序语句格式不符合要求", ErrorCode.INVALID_QUERY);
        }
        return new Sort.Order(direction, fieldMapping(sortField));
    }

    @Override
    public int getPageNumber() {
        return offset / limit;
    }

    @Override
    public int getPageSize() {
        return limit;
    }

    @Override
    public int getOffset() {
        return offset;
    }

    @Override
    public Sort getSort() {
        return sort;
    }

    public void setOffset(int offset) {
        if (offset < 0) {
            return;
        }
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit < 1 || limit > 100) {
            return;
        }
        this.limit = limit;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public boolean isCount() {
        return count;
    }

    public void setCount(boolean count) {
        this.count = count;
    }

    @Override
    public Pageable next() {
        SlicePage sp = new SlicePage();
        sp.setCount(this.count);
        sp.setOffset(offset + limit);
        sp.setLimit(limit);
        sp.setSort(sort);
        return sp;
    }

    @Override
    public Pageable previousOrFirst() {
        if (offset < limit) {
            return first();
        }
        SlicePage sp = new SlicePage();
        sp.setCount(this.count);
        sp.setOffset(offset - limit);
        sp.setLimit(limit);
        sp.setSort(sort);
        return sp;
    }

    @Override
    public Pageable first() {
        SlicePage sp = new SlicePage();
        sp.setCount(this.count);
        sp.setOffset(0);
        sp.setLimit(limit);
        sp.setSort(sort);
        return sp;
    }

    @Override
    public boolean hasPrevious() {
        return offset > limit;
    }
    /**
     * 配置字段映射情况
     * @return
     */
    public String fieldMapping(String fieldName){
        return fieldName;
    };
}

